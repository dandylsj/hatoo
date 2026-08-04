package com.hatoo.common.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "공통 응답 래퍼")
public class GlobalResponse<T> {

    @Schema(description = "성공 여부")
    private final boolean success;

    @Schema(description = "실제 응답 데이터 (실패 시 null)")
    private final T data;

    @Schema(description = "실패 메시지 (성공 시 null)")
    private final String message;

    private GlobalResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // 성공 - 데이터 있을 때
    public static <T> GlobalResponse<T> success(T data) {
        return new GlobalResponse<>(true, data, null);
    }

    // 실패 - 메시지 포함
    public static <T> GlobalResponse<T> fail(String message) {
        return new GlobalResponse<>(false, null, message);
    }

    // 실패 - 메시지 없을 때 (기존 호환)
    public static <T> GlobalResponse<T> fail() {
        return new GlobalResponse<>(false, null, null);
    }
}
