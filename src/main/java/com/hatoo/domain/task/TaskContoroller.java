package com.hatoo.domain.task;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.task.dto.TaskAddTodoRequest;
import com.hatoo.domain.task.dto.TaskAddTodoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task", description = "Task 관련 API")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskContoroller {

    private final TaskService taskService;

    @Operation(summary = "할 일 추가", description = "할 일을 추가 합니다.")
    @PostMapping
    public ResponseEntity<GlobalResponse> addToDo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody TaskAddTodoRequest request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskAddTodoResponse response = taskService.taskAddTodoResponse(token, request);

        return ResponseEntity.ok(GlobalResponse.success(response));

    }
}