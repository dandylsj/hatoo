package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "알림 동의 항목 요청 (설정에서 변경 가능)")
public class AlarmAgreeRequest {

    @Schema(description = "집안일 알림 수신 동의", example = "true")
    private Boolean isChoreNotiAllowed;

    @Schema(description = "마케팅 알림 수신 동의", example = "false")
    private Boolean isMarketingNotiAllowed;
}
