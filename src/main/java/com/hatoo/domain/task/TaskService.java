package com.hatoo.domain.task;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.dto.*;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import com.hatoo.domain.alarm.FcmService;
import com.hatoo.domain.weeklyStats.WeeklyStats;
import com.hatoo.domain.weeklyStats.WeeklyStatsRepository;
import com.hatoo.domain.weeklyStats.WeeklyStatsResponse;
import com.hatoo.domain.weeklyStats.WeeklyStatsWrapperResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TaskService")
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final WeeklyStatsRepository weeklyStatsRepository;
    private final FcmService fcmService;
    private final JwtUtil jwtUtil;

    // 할일 생성
    @Transactional
    public TaskListResponse taskAddTodoResponse(String accessToken, TaskAddTodoRequest request) {

        jwtUtil.validateToken(accessToken);

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        List<User> assignees = userRepository.findAllById(request.getAssigneeIds());

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getFrequency(),
                request.getDueFrom(),
                request.getDueTo(),
                request.getDeadLine(),
                request.getStarter(),
                request.getInterval()
        );

        task.addGroup(group);
        taskRepository.save(task);

        // 담당자 등록 (task 저장 후 ID가 생성된 뒤 TaskAssignee 생성)
        assignees.forEach(user -> {
            TaskAssignee ta = new TaskAssignee(task, user);
            taskAssigneeRepository.save(ta);
        });

        if (task.getFrequency() != null && task.getFrequency() != Frequency.NONE) {
            task.setRecurringTaskId(task.getId().toString());
        }

        // 할일 생성자 조회 (토큰에서 추출)
        String creatorLoginId = jwtUtil.extractLoginId(accessToken);
        User creator = userRepository.findByLoginId(creatorLoginId).orElse(null);
        String creatorNickname = creator != null ? creator.getNickname() : "누군가";

        // 생성자 ID 저장
        if (creator != null) {
            task.setCreatorId(creator.getId());
        }

        // 배정된 유저 ID 목록
        List<UUID> assigneeIds = assignees.stream().map(User::getId).collect(Collectors.toList());

        // 새 집안일 등록 알림 (배정받은 사람 + 생성자 제외 그룹 전체에게)
        fcmService.sendTaskCreated(group.getId(), group.getName(), creatorNickname, task.getTitle(), task.getId(), assigneeIds, task.getCreatorId());

        // 집안일 배정 알림 (담당자 각각에게, 본인이 만든 경우 제외)
        final UUID creatorId = creator != null ? creator.getId() : null;
        assignees.forEach(assignee -> {
            if (creatorId == null || !creatorId.equals(assignee.getId())) {
                fcmService.sendTaskAssigned(assignee.getId(), creatorNickname, assignee.getNickname(), task.getId(), group.getId(), group.getName());
            }
        });

        List<TaskListResponse.AssigneeDto> assigneeDtos = assignees.stream()
                .map(a -> new TaskListResponse.AssigneeDto(a.getId(), a.getNickname(), false))
                .collect(Collectors.toList());

        return new TaskListResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                group.getId(),
                task.getDueFrom(),
                task.getDueTo(),
                false,
                task.getRecurringTaskId(),
                assigneeDtos,
                task.getCreatorId()
        );
    }

    // 그룹의 모든 할일 조회
    @Transactional(readOnly = true)
    public TaskAllGroupListResponse getTasksByGroupListApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // fetch join 으로 taskAssignees + user 한 번에 로드 (N+1 방지)
        List<Task> tasks = taskRepository.findByGroupsIdOrderByDueToDesc(groupId);

        List<TaskAllGroupListResponse.TaskList> taskItems = tasks.stream()
                .filter(task -> !Boolean.TRUE.equals(task.getFinished()))
                .map(task -> {
                    List<TaskAllGroupListResponse.TaskList.AssigneeDto> assigneeDtos =
                            task.getTaskAssignees().stream()
                                    .map(ta -> new TaskAllGroupListResponse.TaskList.AssigneeDto(
                                            ta.getUser().getId(),
                                            ta.getUser().getNickname(),
                                            Boolean.TRUE.equals(ta.getFinished()),
                                            ta.getFinishedAt()))
                                    .collect(Collectors.toList());
                    return new TaskAllGroupListResponse.TaskList(
                            task.getCreatedAt(),
                            task.getUpdatedAt(),
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            groupId,
                            task.getDueFrom(),
                            task.getDueTo(),
                            false,
                            task.getFinishedAt(),
                            task.getFrequency(),
                            task.getInterval(),
                            task.getStarter(),
                            task.getDeadLine(),
                            assigneeDtos,
                            task.getRecurringTaskId(),
                            task.getCreatorId()
                    );
                })
                .collect(Collectors.toList());

        List<TaskAllGroupListResponse.FinishedTaskList> finishedTaskItems = tasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getFinished()))
                .map(task -> {
                    List<TaskAllGroupListResponse.TaskList.AssigneeDto> assigneeDtos =
                            task.getTaskAssignees().stream()
                                    .map(ta -> new TaskAllGroupListResponse.TaskList.AssigneeDto(
                                            ta.getUser().getId(),
                                            ta.getUser().getNickname(),
                                            Boolean.TRUE.equals(ta.getFinished()),
                                            ta.getFinishedAt()))
                                    .collect(Collectors.toList());
                    return new TaskAllGroupListResponse.FinishedTaskList(
                            task.getCreatedAt(),
                            task.getUpdatedAt(),
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            groupId,
                            task.getDueFrom(),
                            task.getDueTo(),
                            true,
                            task.getFinishedAt(),
                            task.getFrequency(),
                            task.getInterval(),
                            task.getStarter(),
                            task.getDeadLine(),
                            assigneeDtos,
                            task.getRecurringTaskId(),
                            task.getCreatorId()
                    );
                })
                .collect(Collectors.toList());

        return new TaskAllGroupListResponse(taskItems, finishedTaskItems, tasks.size(), finishedTaskItems.size());
    }

    // 할일 단건 조회
    @Transactional(readOnly = true)
    public TaskDetailResponse getTaskDetailApi(String accessToken, UUID taskId) {

        jwtUtil.validateToken(accessToken);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TASK_NOT_FOUND));

        List<TaskAssignee> taskAssignees = taskAssigneeRepository.findByTaskId(taskId);

        List<TaskDetailResponse.AssigneeDto> assigneeDtos = taskAssignees.stream()
                .map(ta -> new TaskDetailResponse.AssigneeDto(
                        ta.getUser().getId(),
                        ta.getUser().getNickname(),
                        Boolean.TRUE.equals(ta.getFinished()),
                        ta.getFinishedAt()))
                .collect(Collectors.toList());

        return new TaskDetailResponse(
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getGroupId(),
                task.getDueFrom(),
                task.getDueTo(),
                Boolean.TRUE.equals(task.getFinished()),
                task.getFinishedAt(),
                task.getFrequency(),
                task.getInterval(),
                task.getStarter(),
                task.getDeadLine(),
                assigneeDtos,
                task.getRecurringTaskId(),
                task.getCreatorId()
        );
    }

    // 할일 삭제
    @Transactional
    public Boolean deleteTaskApi(String accessToken, UUID taskId) {

        jwtUtil.validateToken(accessToken);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TASK_NOT_FOUND));

        taskRepository.delete(task);
        return true;
    }

    // 할일 수정
    @Transactional
    public TaskListResponse taskModificationApi(String accessToken, UUID taskId, TaskAddTodoRequest request) {

        jwtUtil.validateToken(accessToken);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TASK_NOT_FOUND));

        task.updateTask(
                request.getTitle(),
                request.getDescription(),
                request.getFrequency(),
                request.getInterval(),
                request.getDueFrom(),
                request.getDueTo(),
                request.getDeadLine(),
                request.getStarter()
        );

        // 기존 담당자 목록
        List<TaskAssignee> existingAssignees = taskAssigneeRepository.findByTaskId(taskId);
        List<UUID> newAssigneeIds = request.getAssigneeIds();

        // 새 담당자 목록이 있을 때만 담당자 변경 처리
        if (newAssigneeIds != null) {
            // 기존 담당자 ID → TaskAssignee 맵 (완료 상태 보존용)
            Map<UUID, TaskAssignee> existingMap = existingAssignees.stream()
                    .collect(Collectors.toMap(ta -> ta.getUser().getId(), ta -> ta));

            // 제거된 담당자 삭제 (완료되지 않은 담당자만 삭제, 완료된 담당자는 유지)
            existingAssignees.stream()
                    .filter(ta -> !newAssigneeIds.contains(ta.getUser().getId()))
                    .filter(ta -> !Boolean.TRUE.equals(ta.getFinished()))
                    .forEach(taskAssigneeRepository::delete);

            // 새로 추가된 담당자 등록
            List<User> newAssignees = userRepository.findAllById(newAssigneeIds);
            newAssignees.stream()
                    .filter(user -> !existingMap.containsKey(user.getId()))
                    .forEach(user -> taskAssigneeRepository.save(new TaskAssignee(task, user)));

            // 모든 담당자가 완료됐는지 재계산
            List<TaskAssignee> updatedAssignees = taskAssigneeRepository.findByTaskId(taskId);
            boolean allDone = !updatedAssignees.isEmpty() &&
                    updatedAssignees.stream().allMatch(ta -> Boolean.TRUE.equals(ta.getFinished()));
            task.setFinished(allDone);

            existingAssignees = updatedAssignees;
        }

        log.info("[Task 수정] taskId={}, task.finished={}, assignees={}",
                taskId,
                task.getFinished(),
                existingAssignees.stream()
                        .map(ta -> ta.getUser().getId() + "=" + ta.getFinished())
                        .collect(Collectors.toList()));

        List<TaskListResponse.AssigneeDto> assigneeDtos = existingAssignees.stream()
                .map(ta -> new TaskListResponse.AssigneeDto(
                        ta.getUser().getId(),
                        ta.getUser().getNickname(),
                        Boolean.TRUE.equals(ta.getFinished())))
                .collect(Collectors.toList());
        boolean allFinished = Boolean.TRUE.equals(task.getFinished());

        return new TaskListResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getGroupId(),
                task.getDueFrom(),
                task.getDueTo(),
                allFinished,
                task.getRecurringTaskId(),
                assigneeDtos,
                task.getCreatorId()
        );
    }

    // 할일 완료 처리 (담당자 개인별 완료 토글)
    @Transactional
    public TaskStatusUpdateResponse taskFinishApi(String accessToken, UUID taskId, TaskStatusUpdateRequest request) {

        jwtUtil.validateToken(accessToken);

        String loginId = jwtUtil.extractLoginId(accessToken);
        User currentUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TASK_NOT_FOUND));

        List<TaskAssignee> taskAssignees = taskAssigneeRepository.findByTaskId(taskId);

        // 담당자로 지정된 경우 → 개인 완료 상태 토글
        TaskAssignee myAssignee = taskAssignees.stream()
                .filter(ta -> ta.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        boolean myFinished;
        java.time.LocalDateTime myFinishedAt;

        if (myAssignee != null) {
            myAssignee.toggleFinished();
            taskAssigneeRepository.save(myAssignee);

            myFinished = Boolean.TRUE.equals(myAssignee.getFinished());
            myFinishedAt = myAssignee.getFinishedAt();

            // 모든 담당자가 완료했는지 확인 → Task.finished 동기화
            boolean allDone = taskAssignees.stream()
                    .allMatch(ta -> Boolean.TRUE.equals(ta.getFinished()));
            task.setFinished(allDone);

        } else {
            // 담당자 없는 태스크 → 기존 방식으로 태스크 자체를 토글
            task.setFinished(!request.getTaskStatus());
            myFinished = task.getFinished();
            myFinishedAt = task.getFinishedAt();
        }

        return new TaskStatusUpdateResponse(myFinished, myFinishedAt, task.getFinished());
    }

    // 완료된 할일 일괄 삭제 (allFinished = true 인 할일만)
    @Transactional
    public void taskBatchDeleteApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        List<Task> finishedTasks = taskRepository.findAllByGroupsContainingAndFinishedTrue(group);
        taskRepository.deleteAll(finishedTasks);
    }

    // 그룹 내 이번 주 기여도 순위 실시간 조회
    @Transactional(readOnly = true)
    public List<TaskRankingResponse> getGroupRankingApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Object[]> results = taskRepository.countFinishedTasksByGroupIdThisWeek(groupId, weekStart, weekEnd);

        Map<UUID, Integer> myFinishedMap = new HashMap<>();
        int groupTotalFinished = 0;

        for (Object[] row : results) {
            UUID userId = (UUID) row[0];
            int myFinished = row[3] != null ? ((Number) row[3]).intValue() : 0;
            groupTotalFinished = row[4] != null ? ((Number) row[4]).intValue() : 0;
            myFinishedMap.put(userId, myFinished);
        }

        final int finalGroupTotal = groupTotalFinished;
        List<GroupMember> allMembers = groupMemberRepository.findByGroupId(groupId);

        AtomicInteger rankCounter = new AtomicInteger(1);
        return allMembers.stream()
                .map(gm -> {
                    int myFinished = myFinishedMap.getOrDefault(gm.getUser().getId(), 0);
                    int percent = finalGroupTotal > 0 ? (int) Math.round(myFinished * 100.0 / finalGroupTotal) : 0;
                    return new TaskRankingResponse(0, gm.getUser().getId(), gm.getUser().getNickname(), gm.getProfileImg(), finalGroupTotal, myFinished, percent);
                })
                .sorted((a, b) -> b.getPercent() - a.getPercent())
                .map(r -> new TaskRankingResponse(rankCounter.getAndIncrement(), r.getUserId(), r.getNickname(), r.getProfileImg(), r.getTotalCount(), r.getFinishedCount(), r.getPercent()))
                .collect(Collectors.toList());
    }

    // 저장된 주차 통계 조회
    @Transactional(readOnly = true)
    public WeeklyStatsWrapperResponse getWeeklyStatsApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String thisWeekMondayStr = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);

        List<String> weekStarts = weeklyStatsRepository.findDistinctWeekStartsByGroupId(groupId)
                .stream()
                .filter(ws -> ws.compareTo(thisWeekMondayStr) < 0)
                .collect(Collectors.toList());

        if (weekStarts.isEmpty()) {
            return WeeklyStatsWrapperResponse.empty();
        }

        String targetWeek = weekStarts.get(0);

        List<WeeklyStats> statsList = weeklyStatsRepository
                .findByGroupIdAndWeekStartOrderByPercentDesc(groupId, targetWeek);

        if (statsList.isEmpty()) {
            return WeeklyStatsWrapperResponse.of(targetWeek, List.of());
        }

        AtomicInteger rankCounter = new AtomicInteger(1);
        List<WeeklyStatsResponse> ranks = statsList.stream()
                .map(s -> WeeklyStatsResponse.from(s, rankCounter.getAndIncrement()))
                .collect(Collectors.toList());

        return WeeklyStatsWrapperResponse.of(targetWeek, ranks);
    }
}
