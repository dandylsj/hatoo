package com.hatoo.domain.weeklyStats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeeklyStatsRepository extends JpaRepository<WeeklyStats, UUID> {

    // 특정 그룹의 특정 주차 결과 조회
    List<WeeklyStats> findByGroupIdAndWeekStartOrderByPercentDesc(UUID groupId, String weekStart);

    // 특정 그룹의 최근 주차 목록 조회 (주차 선택용)
    List<WeeklyStats> findDistinctWeekStartByGroupIdOrderByWeekStartDesc(UUID groupId);

    // 중복 저장 방지: 이미 해당 주차에 저장된 데이터가 있는지 확인
    boolean existsByGroupIdAndWeekStart(UUID groupId, String weekStart);
}
