package com.hatoo.domain.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "그룹별 알림 설정 요청")
public class GroupAlarmSettingRequest {

    @Schema(description = "그룹 알림 마스터 토글 (OFF 시 세부 설정도 모두 OFF)", example = "true")
    private Boolean isGroupNotiEnabled;

    @Schema(description = "새 집안일 등록 알림", example = "true")
    private Boolean isNewTaskNotiEnabled;

    @Schema(description = "새 멤버 추가 알림", example = "true")
    private Boolean isNewMemberNotiEnabled;

    @Schema(description = "집안일 완료 알림", example = "true")
    private Boolean isTaskCompleteNotiEnabled;
}
