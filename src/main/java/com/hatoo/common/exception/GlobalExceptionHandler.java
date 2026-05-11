package com.hatoo.common.exception;

import com.hatoo.common.model.response.GlobalResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j(topic = "CustomExceptionHandler")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("MethodArgumentNotValidException 발생 : {}", message);
        return ResponseEntity.status(ex.getStatusCode()).body(GlobalResponse.fail(message));
    }

    // CustomException → ErrorMessage에 정의된 메시지를 프론트로 전달
    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<GlobalResponse<Void>> handlerCustomException(CustomException ex) {
        log.error("CustomException 발생 : {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorMessage().getStatus())
                .body(GlobalResponse.fail(ex.getErrorMessage().getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GlobalResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("파일 업로드 용량 초과 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.fail("파일 용량을 초과했습니다."));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<GlobalResponse<Void>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error("필수 요청 헤더 누락 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.fail("필수 헤더가 누락되었습니다."));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<GlobalResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("[JWT] 토큰이 만료되었습니다 - 만료시각: {}", e.getClaims().getExpiration());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.fail("토큰이 만료되었습니다."));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<GlobalResponse<Void>> handleMalformedJwtException(MalformedJwtException e) {
        log.error("[JWT] 잘못된 형식의 토큰입니다 - 메시지: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.fail("유효하지 않은 토큰입니다."));
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<GlobalResponse<Void>> handleUnsupportedJwtException(UnsupportedJwtException e) {
        log.error("[JWT] 지원하지 않는 토큰입니다 - 메시지: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.fail("지원하지 않는 토큰입니다."));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<GlobalResponse<Void>> handleSignatureException(SignatureException e) {
        log.error("[JWT] 토큰 서명이 유효하지 않습니다 - 메시지: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.fail("토큰 서명이 유효하지 않습니다."));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<GlobalResponse<Void>> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e) {
        log.warn("낙관적 락 충돌 발생 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(GlobalResponse.fail(ErrorMessage.TASK_CONFLICT.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Void>> handleException(Exception e) {
        log.error("알 수 없는 에러 발생 : ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponse.fail("서버 오류가 발생했습니다."));
    }
}
