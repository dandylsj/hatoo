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
public class TaskService {

    private final TaskRepository taskRepository;
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

        assignees.forEach(task::addAssignee);
        task.addGroup(group);
        taskRepository.save(task);

        if (task.getFrequency() != null && task.getFrequency() != Frequency.NONE) {
            task.setRecurringTaskId(task.getId().toString());
        }

        // 할일 생성자 조회 (토큰에서 추출)
        String creatorLoginId = jwtUtil.extractLoginId(accessToken);
        User creator = userRepository.findByLoginId(creatorLoginId).orElse(null);
        String creatorNickname = creator != null ? creator.getNickname() : "누군가";

        // 배정된 유저 ID 목록
        List<UUID> assigneeIds = assignees.stream().map(User::getId).collect(Collectors.toList());

        // 새 집안일 등록 알림 (배정받은 사람 제외 그룹 전체에게)
        fcmService.sendTaskCreated(group.getId(), creatorNickname, task.getTitle(), task.getId(), assigneeIds);

        // 집안일 배정 알림 (담당자 각각에게, 본인이 만든 경우 제외)
        final UUID creatorId = creator != null ? creator.getId() : null;
        assignees.forEach(assignee -> {
            if (creatorId == null || !creatorId.equals(assignee.getId())) {
                fcmService.sendTaskAssigned(assignee.getId(), creatorNickname, assignee.getNickname(), task.getId());
            }
        });

        List<TaskListResponse.AssigneeDto> assigneeDtos = assignees.stream()
                .map(a -> new TaskListResponse.AssigneeDto(a.getNickname()))
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
                assigneeDtos
        );
    }

    // 그룹의 모든 할일 조회
    @Transactional(readOnly = true)
    public TaskAllGroupListResponse getTasksByGroupListApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        List<Task> tasks = taskRepository.findByGroupsIdOrderByDueToAsc(groupId);

        List<TaskAllGroupListResponse.TaskList> taskItems = tasks.stream()
                .filter(task -> !Boolean.TRUE.equals(task.getFinished()))
                .map(task -> {
                    List<TaskAllGroupListResponse.TaskList.AssigneeDto> assigneeDtos = task.getAssignees().stream()
                            .map(a -> new TaskAllGroupListResponse.TaskList.AssigneeDto(a.getId(), a.getNickname()))
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
                            task.getRecurringTaskId()
                    );
                })
                .collect(Collectors.toList());

        List<TaskAllGroupListResponse.FinishedTaskList> finishedTaskItems = tasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getFinished()))
                .map(task -> {
                    List<TaskAllGroupListResponse.TaskList.AssigneeDto> assigneeDtos = task.getAssignees().stream()
                            .map(a -> new TaskAllGroupListResponse.TaskList.AssigneeDto(a.getId(), a.getNickname()))
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
                            task.getRecurringTaskId()
                    );
                })
                .collect(Collectors.toList());

        return new TaskAllGroupListResponse(taskItems, finishedTaskItems, tasks.size(), finishedTaskItems.size());
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
                request.getDueFrom(),
                request.getDueTo(),
                request.getDeadLine(),
                request.getStarter()
        );

        List<TaskListResponse.AssigneeDto> assigneeDtos = task.getAssignees().stream()
                .map(a -> new TaskListResponse.AssigneeDto(a.getNickname()))
                .collect(Collectors.toList());

        return new TaskListResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getGroupId(),
                task.getDueFrom(),
                task.getDueTo(),
                false,
                task.getRecurringTaskId(),
                assigneeDtos
        );
    }

    // 할일 완료 처리
    @Transactional
    public TaskStatusUpdateResponse taskFinishApi(String accessToken, UUID taskId, TaskStatusUpdateRequest request) {

        jwtUtil.validateToken(accessToken);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TASK_NOT_FOUND));

        task.setFinished(!request.getTaskStatus());

        return new TaskStatusUpdateResponse(task.getFinished(), task.getFinishedAt());
    }

    // 완료된 할일 일괄 삭제
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

    // 저장된 주차 통계 조회 (항상 가장 최근 저장된 지난 주차 반환)
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
