package com.hatoo.common.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "성공 여부 (에러 시 항상 false)", example = "false")
    private boolean success;

    @Schema(description = "에러 메시지", example = "존재하지 않는 유저입니다.")
    private String message;
}
