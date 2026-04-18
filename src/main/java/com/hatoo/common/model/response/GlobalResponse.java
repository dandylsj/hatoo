package com.hatoo.common.model.response;

import lombok.Getter;

@Getter
public class GlobalResponse<T> {

    private final boolean success;
    private final T data;
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
