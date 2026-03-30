package com.hatoo.common.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GlobalResponse {

    private final Object data;

    // 성공 - 데이터 있을 때
    public static GlobalResponse success(Object data) {
        return new GlobalResponse(data);
    }

    // 성공 - 데이터 없을 때
    public static GlobalResponse successNodata() {
        return new GlobalResponse(null);
    }

    // 실패 - 항상 false 반환
    public static GlobalResponse exception(boolean b) {
        return new GlobalResponse(Boolean.FALSE);
    }
}
