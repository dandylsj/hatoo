package com.hatoo.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GlobalResponse<T> {

    private final T data;

    // 성공 - 데이터 있을 때
    public static <T> GlobalResponse<T> success(T data) {
        return new GlobalResponse<>(data);
    }

    // 실패 - 항상 false 반환 (제네릭으로 어느 타입 컨텍스트에서도 사용 가능)
    @SuppressWarnings("unchecked")
    public static <T> GlobalResponse<T> exception() {
        return (GlobalResponse<T>) new GlobalResponse<>(Boolean.FALSE);
    }
}
