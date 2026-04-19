package com.hatoo.domain.weeklyStats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeeklyStatsRepository extends JpaRepository<WeeklyStats, UUID> {

    // 특정 그룹의 특정 주차 결과 조회 (퍼센트 내림차순)
    List<WeeklyStats> findByGroupIdAndWeekStartOrderByPercentDesc(UUID groupId, String weekStart);

    // 특정 그룹의 저장된 주차 목록 조회 (최신순) - 주차 선택 드롭다운용
    @Query("SELECT DISTINCT w.weekStart FROM WeeklyStats w WHERE w.groupId = :groupId ORDER BY w.weekStart DESC")
    List<String> findDistinctWeekStartsByGroupId(@Param("groupId") UUID groupId);

    // 중복 저장 방지: 이미 해당 주차에 저장된 데이터가 있는지 확인
    boolean existsByGroupIdAndWeekStart(UUID groupId, String weekStart);
}
