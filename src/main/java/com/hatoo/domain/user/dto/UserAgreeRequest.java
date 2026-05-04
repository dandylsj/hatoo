package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "필수 동의 항목 요청 (회원가입 직후 1회 호출)")
public class UserAgreeRequest {

    @Schema(description = "서비스 이용약관 동의 (필수)", example = "true")
    private Boolean isTermsAgreed;

    @Schema(description = "개인정보 처리방침 동의 (필수)", example = "true")
    private Boolean isPrivacyAgreed;

    @Schema(description = "만 14세 이상 확인 (필수)", example = "true")
    private Boolean isOverFourteen;
}
