package com.hatoo.domain.groupAlarmSetting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupAlarmSettingRepository extends JpaRepository<GroupAlarmSetting, UUID> {

    Optional<GroupAlarmSetting> findByUserIdAndGroupId(UUID userId, UUID groupId);

    // 특정 유저가 특정 그룹에서 탈퇴할 때
    void deleteByUserIdAndGroupId(UUID userId, UUID groupId);

    // 그룹 삭제 시 전체 설정 삭제
    void deleteByGroupId(UUID groupId);
}
