package com.hatoo.common.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {

    // 200 OK
    AUTH_SIGNUP_SUCCESS("회원가입 성공"),
    AUTH_LOGIN_SUCCESS("로그인 성공"),
    MY_READ_SUCCESS("내 정보 조회 성공"),
    USER_UPDATE_SUCCESS("내 정보 수정 성공"),
    USER_ALL_READ_SUCCESS("회원 전체 조회 성공"),
    USER_READ_SUCCESS("회원 정보 조회 성공"),
    AUTH_LOGOUT_SUCCESS("로그아웃 성공"),
    AUTH_REISSUE_SUCCESS("토큰 재발급 성공"),
    SEND_AUTHENTICATION_CODE("인증 코드 발송했습니다."),
    EMAIL_VERIFICATION_SUCCESSFUL("이메일 인증에 성공했습니다."),
    EMAIL_VERIFICATION_DUPLICATED("중복된 이메일 입니다."),

    QNA_CREATE_SUCCESS("질문이 생성되었습니다."),
    QNA_READ_SUCCESS("질문 단건 조회 성공"),
    QNA_UPDATE_SUCCESS("질문이 수정되었습니다"),
    QNA_LIST_READ_SUCCESS("질문 목록 조회 성공"),
    NOTICE_READ_SUCCESS("공지사항 상세 조회 성공"),
    NOTICE_UPDATE_SUCCESS("공지가 수정되었습니다."),
    LOG_READ_SUCCESS("로그 조회 성공"),
    FILE_UPLOAD_SUCCESS("파일 업로드 성공"),

    CHATROOM_READ_SUCCESS("채팅방 조회 성공"),
    CHATROOM_DELETE_SUCCESS("채팅방 초기화 성공"),
    CHAT_MESSAGE_CREATE_SUCCESS("채팅메세지 전송 성공"),

    // 201 CREATED
    NOTICE_CREATE_SUCCESS("공지사항이 등록되었습니다."),
    USER_CREATE_SUCCESS("회원가입이 완료되었습니다."),
    PRODUCT_CREATE_SUCCESS("상품 등록 완료"),
    ORDER_CREATE_SUCCESS("주문이 생성되었습니다."),
    PAYMENT_CREATE_SUCCESS("결제 요청 생성 완료"),
    USER_CHECK_LOGIN_ID_SUCCESS("아이디 중복 확인 완료."),
    USER_CHECK_NICKNAME_SUCCESS("닉네임 중복 확인 완료."),
    USER_INFO_MODIFY_SUCCESS("유저 정보 수정 완료"),
    PRE_PASSWORD_VERIFICATION_SUCCESS("이전 비밀번호 확인 완료."),
    CHANGE_PASSWORD_SUCCESS("비밀번호가 변경 되었습니다."),
    GROUP_INFO_SUCCESS("내 그룹이 조회 되었습니다."),
    GROUP_CREATE_SUCCESS("그룹이 생성 되었습니다."),
    GROUP_JOIN_SUCCESS("그룹에 참여 되었습니다.");

    private final String message;

}