package com.hatoo.domain.task;

import com.hatoo.domain.task.dto.*;
import com.hatoo.domain.weeklyStats.WeeklyStatsResponse;
import com.hatoo.domain.weeklyStats.WeeklyStatsWrapperResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Task", description = "Task 관련 API")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskContoroller {

    private final TaskService taskService;

    @Operation(summary = "할 일 생성", description = "할 일을 생성 합니다.")
    @PostMapping
    public ResponseEntity<TaskListResponse> addToDo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @RequestBody TaskAddTodoRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskListResponse response = taskService.taskAddTodoResponse(token, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹의 모든 할일 조회", description = "그룹에 속한 할일 목록을 조회합니다.")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<TaskAllGroupListResponse> getTasksByGroup(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskAllGroupListResponse response = taskService.getTasksByGroupListApi(token, groupId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할 일 단건 조회", description = "할 일 하나를 상세 조회합니다.")
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDetailResponse> getTaskDetail(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID taskId) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskDetailResponse response = taskService.getTaskDetailApi(token, taskId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할 일 삭제", description = "할 일을 삭제합니다.")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Boolean> deleteTask(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID taskId) {
       String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

       Boolean response = taskService.deleteTaskApi(token, taskId);

       return ResponseEntity.ok(response);
    }

    @Operation(summary = "할 일 수정", description = "할 일을 수정 합니다.")
    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskListResponse> taskModification(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID taskId, @RequestBody TaskAddTodoRequest request) {
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskListResponse response = taskService.taskModificationApi(token, taskId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할 일 상태 변경", description = "할 일 상태를 완료,미완료 로 변경합니다.")
    @PatchMapping("/{taskId}/task-status")
    public ResponseEntity<TaskStatusUpdateResponse> taskFinish(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID taskId, @RequestBody TaskStatusUpdateRequest request) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        TaskStatusUpdateResponse response = taskService.taskFinishApi(token, taskId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할 일 일괄 삭제", description = "완료된 할 일을 일괄 삭제합니다.")
    @DeleteMapping("/{groupId}/finish")
    public ResponseEntity<Boolean> taskBatchDelete(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        taskService.taskBatchDeleteApi(token, groupId);

        return ResponseEntity.ok(true);
    }

    @Operation(summary = "그룹 완료 할일 순위 조회 (실시간)", description = "이번 주 기준 그룹 내 멤버별 완료율을 실시간으로 조회합니다.")
    @GetMapping("/group/{groupId}/ranking")
    public ResponseEntity<List<TaskRankingResponse>> getGroupRanking(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        List<TaskRankingResponse> response = taskService.getGroupRankingApi(token, groupId);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "주차별 통계 조회 (스냅샷)",
        description = "매주 월요일 오전 8시에 저장된 지난 주 기여도 결과를 자동으로 반환합니다.\n\n" +
                      "별도 파라미터 없이 호출하면 가장 최근에 저장된 지난 주차 통계를 반환합니다.\n\n" +
                      "신규 그룹처럼 아직 저장된 통계가 없으면 빈 배열 `[]`을 반환합니다."
    )
    @GetMapping("/group/{groupId}/weekly-stats")
    public ResponseEntity<WeeklyStatsWrapperResponse> getWeeklyStats(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            @PathVariable UUID groupId) {

        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

        WeeklyStatsWrapperResponse response = taskService.getWeeklyStatsApi(token, groupId);

        return ResponseEntity.ok(response);
    }

}
