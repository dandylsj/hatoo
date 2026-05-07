package com.hatoo.domain.groupMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // 특정 그룹의 모든 멤버 조회 - user fetch join (N+1 방지)
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId ORDER BY gm.createdAt ASC")
    List<GroupMember> findByGroupIdOrderByCreatedAtAsc(@Param("groupId") UUID groupId);

    // 특정 유저가 속한 모든 그룹멤버 조회 - group fetch join (N+1 방지)
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.group WHERE gm.user.id = :userId ORDER BY gm.createdAt DESC")
    List<GroupMember> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    // 특정 유저가 속한 모든 그룹멤버 조회 - group fetch join (탈퇴 로직용)
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.group WHERE gm.user.id = :userId")
    List<GroupMember> findByUserId(@Param("userId") UUID userId);

    // 특정 그룹에서 특정 유저 조회
    Optional<GroupMember> findByUserIdAndGroupId(UUID userId, UUID groupId);

    // 특정 그룹에 특정 유저가 있는지 확인
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    // 특정 그룹의 모든 멤버 조회 - user fetch join (알림/랭킹 로직용)
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId")
    List<GroupMember> findByGroupId(@Param("groupId") UUID groupId);

    // 같은 그룹 내 동일 프로필 이미지를 사용 중인 다른 멤버가 있는지 확인
    boolean existsByGroupIdAndProfileImgAndUserIdNot(UUID groupId, String profileImg, UUID userId);
}
