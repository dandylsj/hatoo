package com.hatoo.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "할 일 완료 상태 변경 응답")
public class TaskStatusUpdateResponse {

    @Schema(description = "요청한 유저 본인의 완료 여부")
    private Boolean myFinished;

    @Schema(description = "요청한 유저 본인의 완료 시각 (미완료 시 null)")
    private LocalDateTime myFinishedAt;

    @Schema(description = "모든 담당자가 완료했는지 여부")
    private Boolean allFinished;
}
