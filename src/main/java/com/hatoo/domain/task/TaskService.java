package com.hatoo.domain.task;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.dto.TaskAddTodoRequest;
import com.hatoo.domain.task.dto.TaskAddTodoResponse;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final JwtUtil jwtUtil;

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

        // 6. 응답 생성 (task에 연결된 담당자 전체를 nickname 리스트로 반환)
        List<TaskAddTodoResponse.AssigneeDto> assigneeDtos = task.getAssignees().stream()
                .map(u -> new TaskAddTodoResponse.AssigneeDto(u.getNickname()))
                .collect(Collectors.toList());

        return new TaskAddTodoResponse(
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
}
