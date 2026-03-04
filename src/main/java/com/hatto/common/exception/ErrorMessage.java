package com.hatto.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    /* --- 400 Bad Request --- */
    // 공통/입력값 오류
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "필수 필드값이 누락되었습니다."),

    // 유저 관련
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다."),
    MISSING_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 누락되었습니다."),


    /* --- 401 Unauthorized --- */
    // 인증 실패 (로그인 필요)
    INVALID_AUTH_INFO(HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),
    ADMIN_PERMISSION_REQUIRED(HttpStatus.UNAUTHORIZED, "관리자 권한이 필요합니다."),
    INVALID_DEFERMENT_STATUS(HttpStatus.UNAUTHORIZED, "없는 연기 상태 입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),

    /* --- 403 Forbidden --- */
    // 인가 실패 (권한 부족 - 작성자가 아님 등)
    NO_MODIFY_PERMISSION(HttpStatus.FORBIDDEN, "수정 권한이 없습니다."),
    NO_DELETE_PERMISSION(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다."),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "탈퇴한 회원입니다."),
    REJECT_PAYMENT(HttpStatus.FORBIDDEN, "결제 승인이 거절되었습니다."),
    INVALID_ADMIN_TOKEN(HttpStatus.BAD_REQUEST, "관리자 토큰값이 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "토큰값이 일치하지 않습니다."),
    USER_LOGOUT(HttpStatus.BAD_REQUEST, "로그아웃된 계정입니다."),
    NO_CHECK_PERMISSION(HttpStatus.INTERNAL_SERVER_ERROR, "권한이 없습니다."),


    /* --- 404 Not Found --- */
    // 리소스 없음
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "입영 신청 내역이 없습니다."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "입영 일정이 존재하지 않습니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다."),
    DEFERMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "연기 일정을 찾을 수 없습니다"),
    USER_DELETED_NOT_FOUND(HttpStatus.NOT_FOUND, "이미 탈퇴한 사용자입니다."),
    NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보 입니다."),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메세지입니다."),

    /* --- 409 Conflict --- */
    // 데이터 충돌
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");


    private final HttpStatus status;
    private final String message;
}
