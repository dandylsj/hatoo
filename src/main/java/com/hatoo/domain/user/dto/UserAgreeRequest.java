package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 후 동의 항목 요청")
public class UserAgreeRequest {

    // ── 필수 동의 3개 ──────────────────────────
    @Schema(description = "서비스 이용약관 동의 (필수)", example = "true")
    private Boolean isTermsAgreed;

    @Schema(description = "개인정보 처리방침 동의 (필수)", example = "true")
    private Boolean isPrivacyAgreed;

    @Schema(description = "만 14세 이상 확인 (필수)", example = "true")
    private Boolean isOverFourteen;

    // ── 알림 동의 2개 ──────────────────────────
    @Schema(description = "집안일 알림 수신 동의 (선택)", example = "true")
    private Boolean isChoreNotiAllowed;

    @Schema(description = "마케팅 알림 수신 동의 (선택)", example = "false")
    private Boolean isMarketingNotiAllowed;
}
