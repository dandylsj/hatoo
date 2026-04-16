package com.hatoo.domain.task;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.dto.*;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final JwtUtil jwtUtil;

    // 할일 생성
    @Transactional
    public TaskListResponse taskAddTodoResponse(String accessToken, TaskAddTodoRequest request) {

        // 1. 토큰 검증
        jwtUtil.validateToken(accessToken);

        // 2. 그룹 조회
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 3. 담당자 조회
        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 4. 할일 생성
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

        // 5. 담당자와 그룹 연결
        task.addAssignee(assignee);
        task.addGroup(group);
        taskRepository.save(task);

        // 6. 반복 설정이 있으면 본인 id를 recurringTaskId로 저장
        if (task.getFrequency() != null && task.getFrequency() != Frequency.NONE) {
            task.setRecurringTaskId(task.getId().toString());
        }

        return new TaskListResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                group.getId(),
                task.getDueFrom(),
                task.getDueTo(),
                false,
                task.getRecurringTaskId(),
                new TaskListResponse.AssigneeDto(assignee.getNickname())
        );
    }

    // 그룹의 모든 할일 조회
    @Transactional(readOnly = true)
    public TaskAllGroupListResponse getTasksByGroupListApi(String accessToken, UUID groupId) {

        // 1. 토큰 검증
        jwtUtil.validateToken(accessToken);

        // 2. 그룹 존재 확인
        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 3. 그룹에 속한 할일 중 finished가 false 인 할일 조회
        List<Task> tasks = taskRepository.findByGroupsId(groupId);

        // 4. 응답 변환
        List<TaskAllGroupListResponse.TaskList> taskItems = tasks.stream()
                .filter(task -> task.getFinished() == false)
                .map(task -> {
                    User firstAssignee = task.getAssignees().isEmpty() ? null : task.getAssignees().get(0);
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
                            task.getFrequency(),
                            task.getInterval(),
                            task.getStarter(),
                            task.getDeadLine(),
                            firstAssignee != null ? firstAssignee.getId().toString() : null,
                            task.getRecurringTaskId(),
                            firstAssignee != null
                                    ? new TaskAllGroupListResponse.TaskList.AssigneeDto(firstAssignee.getNickname())
                                    : null
                    );
                })
                .collect(Collectors.toList());

        List<TaskAllGroupListResponse.FinishedTaskList> finishedTaskItems = tasks.stream()
                .filter(task -> task.getFinished() == true)
                .map(task -> {
                    User firstAssignee = task.getAssignees().isEmpty() ? null : task.getAssignees().get(0);
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
                            task.getFrequency(),
                            task.getInterval(),
                            task.getStarter(),
                            task.getDeadLine(),
                            firstAssignee != null ? firstAssignee.getId().toString() : null,
                            task.getRecurringTaskId(),
                            firstAssignee != null
                                    ? new TaskAllGroupListResponse.TaskList.AssigneeDto(firstAssignee.getNickname())
                                    : null
                    );
                })
                .collect(Collectors.toList());

        return new TaskAllGroupListResponse(taskItems, finishedTaskItems, tasks.size(), finishedTaskItems.size());
    }

    //할 일 삭제
    @Transactional
    public Boolean deleteTaskApi(String accessToken, UUID taskId) {

        jwtUtil.validateToken(accessToken);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TASK_NOT_FOUND));

        taskRepository.delete(task);

        return true;
    }

    //할 일 수정
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

        return new TaskListResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getGroupId(),
                task.getDueFrom(),
                task.getDueTo(),
                false,
                task.getRecurringTaskId(),
                new TaskListResponse.AssigneeDto(task.getAssigneeId().toString())
        );
    }

    //할 일 완료 처리
    @Transactional
    public TaskStatusUpdateResponse taskFinishApi(String accessToken, UUID taskId, TaskStatusUpdateRequest request) {

        jwtUtil.validateToken(accessToken);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorMessage.TASK_NOT_FOUND));

        if(!request.getTaskStatus()) {
            task.setFinished(true);
        }else {
            task.setFinished(false);
        }

        return new TaskStatusUpdateResponse(task.getFinished());
    }

    //완료된 할 일 일괄 삭제
    @Transactional
    public void taskBatchDeleteApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        List<Task> finishedTasks = taskRepository.findAllByGroupsContainingAndFinishedTrue(group);

        taskRepository.deleteAll(finishedTasks);
    }

    // 그룹 내 완료 할일 순위 조회 (완료율 기준)
    @Transactional(readOnly = true)
    public List<TaskRankingResponse> getGroupRankingApi(String accessToken, UUID groupId) {

        jwtUtil.validateToken(accessToken);

        groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        List<Object[]> results = taskRepository.countFinishedTasksByGroupId(groupId);

        List<TaskRankingResponse> rankings = new ArrayList<>();
        for (Object[] row : results) {
            UUID userId = (UUID) row[0];
            String nickname = (String) row[1];
            String profileImg = (String) row[2];
            long total = ((Number) row[3]).longValue();
            long finished = row[4] != null ? ((Number) row[4]).longValue() : 0L;

            int percent = total > 0 ? (int) (finished * 100 / total) : 0;

            rankings.add(new TaskRankingResponse(
                    userId,
                    nickname,
                    percent,
                    profileImg
            ));
        }

        return rankings;
    }

}