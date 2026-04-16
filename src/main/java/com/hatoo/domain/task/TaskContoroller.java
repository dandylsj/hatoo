package com.hatoo.domain.task;

import com.hatoo.common.model.response.GlobalResponse;
import com.hatoo.domain.task.dto.*;
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

    @Operation(summary = "할 일 생성", description = "할 일을 생성 합니다.")
    @PostMapping
    public ResponseEntity<GlobalResponse<TaskListResponse>> addToDo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody TaskAddTodoRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskListResponse response = taskService.taskAddTodoResponse(token, request);

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

    @Operation(summary = "할 일 삭제", description = "할 일을 삭제합니다.")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<GlobalResponse<Boolean>> deleteTask(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID taskId) {
       String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

       Boolean response = taskService.deleteTaskApi(token, taskId);

       return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @Operation(summary = "할 일 수정", description = "할 일을 수정 합니다.")
    @PatchMapping("/{taskId}")
    public ResponseEntity<GlobalResponse<TaskListResponse>> taskModification(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID taskId, @RequestBody TaskAddTodoRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskListResponse response = taskService.taskModificationApi(token, taskId, request);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @Operation(summary = "할 일 상태 변경", description = "할 일 상태를 완료,미완료 로 변경합니다.")
    @PatchMapping("/{taskId}/task-status")
    public ResponseEntity<GlobalResponse<TaskStatusUpdateResponse>> taskFinish(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID taskId, @RequestBody TaskStatusUpdateRequest request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskStatusUpdateResponse response = taskService.taskFinishApi(token, taskId, request);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @Operation(summary = "할 일 일괄 삭제", description = "완료된 할 일을 일괄 삭제합니다.")
    @DeleteMapping("/{groupId}/finish")
    public ResponseEntity<GlobalResponse<Boolean>> taskBatchDelete(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        taskService.taskBatchDeleteApi(token, groupId);

        return ResponseEntity.ok(GlobalResponse.success(true));
    }

    @Operation(summary = "그룹 완료 할일 순위 조회", description = "그룹 내 완료한 할일 수를 기준으로 멤버 순위를 조회합니다.")
    @GetMapping("/group/{groupId}/ranking")
    public ResponseEntity<GlobalResponse<TaskRankingResponse>> getGroupRanking(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskRankingResponse response = taskService.getGroupRankingApi(token, groupId);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }

}
