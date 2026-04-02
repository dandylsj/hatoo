package com.hatoo.common.exception;

import com.hatoo.common.model.response.GlobalResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
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
    public ResponseEntity<GlobalResponse<Boolean>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException 발생 : {} ", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(GlobalResponse.exception());
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<GlobalResponse<Boolean>> handlerCustomException(CustomException ex) {
        log.error("CustomException 발생 : {} ", ex.getMessage());
        return ResponseEntity.status(ex.getErrorMessage().getStatus()).body(GlobalResponse.exception());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GlobalResponse<Boolean>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("파일 업로드 용량 초과 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.exception());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<GlobalResponse<Boolean>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.error("필수 요청 헤더 누락 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.exception());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<GlobalResponse<Boolean>> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("[JWT] 토큰이 만료되었습니다 - 만료시각: {}, 메시지: {}", e.getClaims().getExpiration(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.exception());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<GlobalResponse<Boolean>> handleMalformedJwtException(MalformedJwtException e) {
        log.error("[JWT] 잘못된 형식의 토큰입니다 - 메시지: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.exception());
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<GlobalResponse<Boolean>> handleUnsupportedJwtException(UnsupportedJwtException e) {
        log.error("[JWT] 지원하지 않는 토큰입니다 - 메시지: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.exception());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<GlobalResponse<Boolean>> handleSignatureException(SignatureException e) {
        log.error("[JWT] 토큰 서명이 유효하지 않습니다 - 메시지: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.exception());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Boolean>> handleException(Exception e) {
        log.error("알 수 없는 에러 발생 : ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponse.exception());
    }
}
