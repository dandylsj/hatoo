package com.hatoo.domain.alarm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {

    // 스케줄러 기반
    TASK_START("집안일 시작", "%s 할 일이 오늘 시작돼요!"),
    TASK_DEADLINE("마감 임박", "%s 마감이 내일이에요! 서두르세요."),
    TASK_OVERDUE("마감 초과", "%s 마감 시간이 지났어요. 빨리 완료해주세요!"),
    WEEKLY_CHART("이번 주 차트", "이번 주 집안일 차트 결과가 공개됐어요! 지금 확인해보세요."),
    INACTIVE_GROUP("그룹 비활성화", "한 달 동안 활동이 없는 그룹이 있어요. 계속 사용하려면 할 일을 추가하거나, 사용하지 않는다면 삭제하세요."),

    // 이벤트 기반
    NEW_MEMBER("새 멤버 참여", "%s에 새 멤버 %s님이 참여했어요!"),
    TASK_CREATED("새 집안일 등록", "%s님이 새 집안일을 등록했어요. [%s]"),
    TASK_ASSIGNED("집안일 배정", "%s님이 %s님에게 새로운 집안일을 배정했어요. 지금 확인해보세요!"),
    TASK_COMPLETE("집안일 완료", "%s님이 [%s]을 완료했어요! 👏");

    private final String title;
    private final String bodyTemplate; // %s 자리에 동적 값 삽입
}
