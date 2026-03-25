package com.hatoo.common.exception;

public final class ValidationMessage {

    private ValidationMessage() {
    }

    // --- 공통 메시지 ---
    public static final String NOT_BLANK_DEFAULT = "값은 필수입니다.";

    // --- User 관련 ---
    public static final String USERNAME_NOT_BLANK = "닉네임은 필수입니다.";
    public static final String USERNAME_SIZE = "닉네임은 10글자를 넘길 수 없습니다.";
    public static final String EMAIL_FORMAT = "올바른 이메일 형식이 아닙니다.";
    public static final String PASSWORD_NOT_BLANK = "비밀번호는 필수입니다.";
    public static final String PASSWORD_PATTERN_MESSAGE = "비밀번호는 영어와 숫자, 특수문자를 최소 1개 이상 포함해서 8자리 이상 입력해주세요.";
    public static final String NAME_NOT_BLANK = "이름은 필수입니다.";
    public static final String LOGIN_REQUIRED = "username과 password는 필수입니다";
    public static final String PASSWORD_REQUIRED = "비밀번호를 입력해주세요.";

    // --- QNA 관련 ---
    public static final String TITLE_CONTENT_NOT_BLANK = "질문 제목과 질문 내용은 필수입니다.";
    public static final String ASK_CONTENT_NOT_BLANK = "답변 내용은 필수 입니다.";

    // --- Notice 관련 ---
    public static final String NOTICE_TITLE_CONTENT_NOT_BLANK = "공지 제목과 공지 내용은 필수입니다.";

    // --- Comment 관련 ---
    public static final String COMMENT_CONTENT_NOT_BLANK = "댓글 내용은 필수입니다.";

    // --- Order 관련 ---
    public static final String NOT_BLANK_PRODUCT_ID = "product id는 필수입니다";
    public static final String NOT_BLANK_QUANTITY = "수량은 필수 입력 요소입니다.";

    // --- Payment 관련 ---
    public static final String NOT_SELECTED_ORDER_ID = "order id가 누락되었습니다.";
    public static final String NOT_BLANK_ORDER_NUMBER = "주문번호가 누락되었습니다.";
    public static final String NOT_BLANK_PAYMENT_KEY = "결제 키 값이 누락되었습니다.";
    public static final String NOT_BLANK_CANCEL_REASON = "결제 취소 사유가 누락되었습니다";

    // --- 정규식 ---
    public static final String PASSWORD_REGEXP = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[~!@#$%])(?=\\S+$).{8,}$";
}
