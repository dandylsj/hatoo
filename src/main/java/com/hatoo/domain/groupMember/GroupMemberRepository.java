package com.hatoo.domain.groupMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // 특정 그룹의 모든 멤버 조회
    List<GroupMember> findByGroupId(UUID groupId);

    // 특정 유저의 모든 그룹멤버 조회
    List<GroupMember> findByUserId(UUID userId);

    // 특정 그룹에서 특정 유저 조회
    Optional<GroupMember> findByUserIdAndGroupId(UUID userId, UUID groupId);

    // 특정 그룹에 특정 유저가 있는지 확인
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    // 특정 그룹에서 해당 색상이 이미 사용 중인지 확인
    boolean existsByGroupIdAndProfileImg(UUID groupId, ProfileImg profileImg);
}
