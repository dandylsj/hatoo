package com.hatoo.common.exception;

import com.hatoo.common.model.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j(topic = "CustomExceptionHandler")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException 발생 : {} ", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(GlobalResponse.exception(false));
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<GlobalResponse> handlerCustomException(CustomException ex) {
        log.error("CustomException 발생 : {} ", ex.getMessage());
        // HTTP 상태코드는 ErrorMessage 기준, 응답 body는 data: false 만 반환
        return ResponseEntity.status(ex.getErrorMessage().getStatus()).body(GlobalResponse.exception(false));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GlobalResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("파일 업로드 용량 초과 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.exception(false));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<GlobalResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error("필수 요청 헤더 누락 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.exception(false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse> handleException(Exception e) {
        log.error("알 수 없는 에러 발생 : ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponse.exception(false));
    }
}
