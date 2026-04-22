package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "알림 설정 수정 요청 (null인 항목은 변경하지 않음)")
public class AlarmAgreeRequest {

    @Schema(description = "전체 알림 마스터 토글", example = "true")
    private Boolean isAllNotiEnabled;

    @Schema(description = "마케팅 알림 수신 동의", example = "false")
    private Boolean isMarketingNotiAllowed;

    @Schema(description = "개인 알림 (집안일 시작/마감 임박/마감 초과)", example = "true")
    private Boolean isPersonalNotiEnabled;

    @Schema(description = "그룹 알림 전체 마스터", example = "true")
    private Boolean isGroupNotiAllGlobalEnabled;
}
