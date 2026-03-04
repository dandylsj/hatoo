package com.hatto.common.exception;


import com.hatto.common.model.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Objects;

@RestControllerAdvice
@Slf4j(topic = "CustomExceptionHandler")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        log.error("MethodArgumentNotValidException 발생 : {} ", ex.getMessage());

        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();

        return ResponseEntity.status(ex.getStatusCode()).body(GlobalResponse.exception(message));
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<GlobalResponse> handlerCustomException(CustomException ex) {

        log.error("CustomException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getErrorMessage().getStatus()).body(GlobalResponse.exception(ex.getErrorMessage().getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GlobalResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("파일 업로드 용량 초과 : {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.exception("파일 크기가 너무 큽니다. 최대 5MB까지 업로드 가능합니다."));
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse> handleException(Exception e) {
        log.error("알 수 없는 에러 발생 : ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalResponse.exception("서버 내부 오류 발생 : " + e.getMessage()));
    }

}
