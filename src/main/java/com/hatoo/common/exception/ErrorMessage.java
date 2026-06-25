package com.hatoo.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    /* --- 400 Bad Request --- */
    INVALID_PASSWORD(HttpStatus.CONFLICT, "비밀번호가 일치하지 않습니다"),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    INVALID_TIME_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증시간이 초과했습니다."),

    /* --- 401 Unauthorized --- */
    INVALID_AUTH_INFO(HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),

    /* --- 403 Forbidden --- */
    NO_DELETE_PERMISSION(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다."),

    /* --- 404 Not Found --- */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 그룹입니다"),
    USER_NOT_IN_GROUP(HttpStatus.NOT_FOUND, "유저가 속한 그룹이 없습니다"),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알람을 찾을 수 없습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 할일입니다."),

    /* --- 409 Conflict --- */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),

    /* --- 422 Unprocessable Entity --- */
    SOCIAL_LOGIN_ACCOUNT(HttpStatus.UNPROCESSABLE_ENTITY, "소셜 로그인으로 가입된 계정입니다."),
    NOT_SAME_INVITED(HttpStatus.CONFLICT, "초대코드가 맞지 않습니다."),
    ALREADY_JOINED_GROUP(HttpStatus.CONFLICT, "이미 해당 그룹에 가입되어 있습니다."),
    GROUP_FULL(HttpStatus.CONFLICT, "그룹 인원이 가득 찼습니다. (최대 5명)"),
    DUPLICATE_PROFILE_IMG(HttpStatus.CONFLICT, "이미 다른 멤버가 선택한 프로필입니다."),
    TASK_CONFLICT(HttpStatus.CONFLICT, "다른 사용자가 이미 수정했습니다. 다시 시도해주세요."),

    /* --- 423 Locked --- */
    GROUP_JOIN_LOCK_FAILED(HttpStatus.LOCKED, "잠시 후 다시 시도해주세요. (그룹 참여 처리 중)"),

    /* --- 429 Too Many Requests --- */
    EMAIL_SEND_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "10초 후에 다시 시도해주세요."),
    EMAIL_SEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "이메일 전송 횟수를 초과했습니다. 5분 후에 다시 시도해주세요."),

    /* --- 502 Bad Gateway --- */
    KAKAO_LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "카카오 로그인에 실패했습니다."),
    NAVER_LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "네이버 로그인에 실패했습니다."),
    GOOGLE_LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "구글 로그인에 실패했습니다."),
    APPLE_LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "애플 로그인에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}