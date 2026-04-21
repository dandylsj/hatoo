package com.hatoo.domain.groups.dto;

import com.hatoo.domain.groupAlarmSetting.GroupAlarmSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "그룹별 알림 설정 응답")
public class GroupAlarmSettingResponse {

    @Schema(description = "그룹 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID groupId;

    @Schema(description = "그룹 알림 마스터 토글. false이면 아래 세부 설정과 무관하게 모든 그룹 알림이 차단됩니다.", example = "true")
    private Boolean isGroupNotiEnabled;

    @Schema(description = "새 집안일 등록 알림. 그룹에 새로운 할일이 추가됐을 때 알림을 받을지 여부입니다.", example = "true")
    private Boolean isNewTaskNotiEnabled;

    @Schema(description = "새 멤버 추가 알림. 새로운 멤버가 그룹에 참여했을 때 알림을 받을지 여부입니다.", example = "true")
    private Boolean isNewMemberNotiEnabled;

    @Schema(description = "집안일 완료 알림. 그룹 내 누군가가 할일을 완료했을 때 알림을 받을지 여부입니다.", example = "true")
    private Boolean isTaskCompleteNotiEnabled;

    public static GroupAlarmSettingResponse from(GroupAlarmSetting setting) {
        return new GroupAlarmSettingResponse(
                setting.getGroupId(),
                setting.getIsGroupNotiEnabled(),
                setting.getIsNewTaskNotiEnabled(),
                setting.getIsNewMemberNotiEnabled(),
                setting.getIsTaskCompleteNotiEnabled()
        );
    }

    // 설정 레코드가 없는 경우 기본값(전부 true) 반환
    public static GroupAlarmSettingResponse defaultEnabled(UUID groupId) {
        return new GroupAlarmSettingResponse(groupId, true, true, true, true);
    }
}
