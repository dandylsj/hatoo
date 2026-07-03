package com.hatoo.domain.alarm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {

    // 스케줄러 기반
    TASK_START("집안일 시작", "집안일을 시작해볼까요?"),
    TASK_DEADLINE("마감 알림", "마감까지 얼마 안남았어요!"),
    TASK_OVERDUE("마감 시간 초과", "집안일 마감이 2시간 경과 됐어요."),
    WEEKLY_CHART("집안일 차트 집계", "이번 주 집안일 차트 집계 확인하러 가기 ➡"),

    // 이벤트 기반
    NEW_MEMBER("새 멤버 추가", "새 멤버 %s님이 참여했어요."),
    TASK_CREATED("집안일 등록", "%s님이 새 집안일을 등록했어요."),
    TASK_ASSIGNED("집안일 배정", "%s님이 %s님에게 새로운 집안일을 배정했어요. 지금 확인해보세요!"),
    FORCED_LEAVE("그룹 탈퇴", "%s 그룹에서 탈퇴되었습니다.");

    private final String title;
    private final String bodyTemplate; // %s 자리에 동적 값 삽입
}
