package com.hatoo.common.model.response;

import com.hatoo.common.model.enums.SuccessMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GlobalResponse {

    private final boolean success;
    private final String message;
    private final Object data;
    private final LocalDateTime timeStamp;


    // 성공 시 (사용 예시: GlobalResponse.success(SuccessMessage 이넘, 반환 DTO)
    public static GlobalResponse success(SuccessMessage successMessage, Object data) {
        return new GlobalResponse(true, successMessage.getMessage(), data, LocalDateTime.now());
    }

    // 성공 했는데 응답 데이터는 없을 시 (사용 예시: GlobalResponse.successNodata(SuccessMessage 이넘)
    public static GlobalResponse successNodata(SuccessMessage successMessage) {
        return new GlobalResponse(true, successMessage.getMessage(), null, LocalDateTime.now());
    }

    // 예외 처리 시
    public static GlobalResponse exception(String errorMessage) {
        return new GlobalResponse(false, errorMessage, null, LocalDateTime.now());
    }
}
