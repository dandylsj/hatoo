package com.hatoo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "전체 알림 설정 조회 응답")
public class AlarmSettingResponse {

    @Schema(description = "전체 알림 마스터 토글. false이면 모든 알림이 차단됩니다.", example = "true")
    private Boolean isAllNotiEnabled;

    @Schema(description = "마케팅 알림 수신 동의. 공지사항/이벤트/업데이트 알림 수신 여부입니다.", example = "true")
    private Boolean isMarketingNotiAllowed;

    @Schema(description = "개인 알림. 내가 담당한 집안일의 시작/마감 임박/마감 초과 알림 수신 여부입니다.", example = "true")
    private Boolean isPersonalNotiEnabled;

    @Schema(description = "그룹 알림 전체 마스터. false이면 아래 모든 그룹 알림이 차단됩니다.", example = "true")
    private Boolean isGroupNotiAllGlobalEnabled;

//    @Schema(description = "그룹별 알림 세부 설정 목록")
//    private List<GroupAlarmDto> groupSettings;
//
//    @Getter
//    @AllArgsConstructor
//    @Schema(description = "그룹별 알림 세부 설정")
//    public static class GroupAlarmDto {
//
//        @Schema(description = "그룹 ID")
//        private UUID groupId;
//
//        @Schema(description = "그룹 이름", example = "하투네")
//        private String groupName;
//
//        @Schema(description = "해당 그룹 알림 마스터 토글", example = "true")
//        private Boolean isGroupNotiEnabled;
//
//        @Schema(description = "새 집안일 등록 알림", example = "true")
//        private Boolean isNewTaskNotiEnabled;
//
//        @Schema(description = "새 멤버 추가 알림", example = "true")
//        private Boolean isNewMemberNotiEnabled;
//
//        @Schema(description = "집안일 완료 알림", example = "true")
//        private Boolean isTaskCompleteNotiEnabled;
//    }
}
