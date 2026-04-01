package com.hatoo.domain.task;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.task.dto.TaskAddTodoRequest;
import com.hatoo.domain.task.dto.TaskAddTodoResponse;
import com.hatoo.domain.task.dto.TaskAllGroupListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Task", description = "Task 관련 API")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskContoroller {

    private final TaskService taskService;

    @Operation(summary = "할 일 추가", description = "할 일을 추가 합니다.")
    @PostMapping
    public ResponseEntity<GlobalResponse<TaskAddTodoResponse>> addToDo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody TaskAddTodoRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        TaskAddTodoResponse response = taskService.taskAddTodoResponse(token, request);
        return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @Operation(summary = "그룹의 모든 할일 조회", description = "그룹에 속한 할일 목록을 조회합니다.")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<GlobalResponse<TaskAllGroupListResponse>> getTasksByGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        TaskAllGroupListResponse response = taskService.getTasksByGroupListApi(token, groupId);
        return ResponseEntity.ok(GlobalResponse.success(response));
    }
}
