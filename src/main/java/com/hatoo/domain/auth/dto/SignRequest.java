package com.hatoo.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignRequest {

    @Schema(description = "로그인에 사용할 아이디", example = "hatoo123")
    @NotBlank(message = "아이디는 필수입니다.")
    private String loginId;

    @Schema(description = "로그인 비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @Schema(description = "사용자 이메일 주소", example = "hatoo@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "사용자 닉네임(이름)", example = "하투")
    @NotBlank(message = "이름은 필수입니다.")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImg;

    @Schema(description = "개인정보 수집, 이용동의 여부", example = "true")
    private Boolean isPrivacyAgreed;

    @Schema(description = "서비스 이용약관 동의 여부", example = "true")
    private Boolean isTermsAgreed;

    @Schema(description = "14세 이상 여부", example = "true")
    private Boolean isOverFourteen;

    @Schema(description = "집안일 알림 수신 동의 여부", example = "true")
    private Boolean isChoreNotiAllowed;

    @Schema(description = "이벤트, 공지 알림 수신 동의 여부", example = "true")
    private Boolean isMarketingNotiAllowed;



}
