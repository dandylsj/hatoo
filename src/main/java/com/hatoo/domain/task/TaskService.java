package com.hatoo.domain.task;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.dto.TaskAddTodoRequest;
import com.hatoo.domain.task.dto.TaskAddTodoResponse;
import com.hatoo.domain.task.dto.TaskAllGroupListResponse;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 할일 추가
    @Transactional
    public TaskAddTodoResponse taskAddTodoResponse(String accessToken, TaskAddTodoRequest request) {

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
                request.getStarter()
        );

        // 5. 담당자와 그룹 연결
        task.addAssignee(assignee);
        task.addGroup(group);
        taskRepository.save(task);

        return new TaskAddTodoResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                group.getId(),
                task.getDueFrom(),
                task.getDueTo(),
                false,
                task.getRecurringTaskId(),
                new TaskAddTodoResponse.AssigneeDto(assignee.getNickname())
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

        // 3. 그룹에 속한 할일 목록 조회
        List<Task> tasks = taskRepository.findByGroupsId(groupId);

        // 4. 응답 변환
        List<TaskAllGroupListResponse.TaskList> taskItems = tasks.stream()
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
                            task.getFinished() != null,
                            firstAssignee != null ? firstAssignee.getId().toString() : null,
                            task.getRecurringTaskId(),
                            firstAssignee != null
                                    ? new TaskAllGroupListResponse.TaskList.AssigneeDto(firstAssignee.getNickname())
                                    : null
                    );
                })
                .collect(Collectors.toList());

        return new TaskAllGroupListResponse(taskItems);
    }
}
