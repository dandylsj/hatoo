package com.hatoo.domain.groups;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findByName(String name);

    List<Group> findAllByInviteCode(String token);

    // 최근 30일간 할일 변경이 없는 비활성 그룹 조회
    @Query("SELECT g FROM Group g WHERE g.id NOT IN " +
           "(SELECT DISTINCT grp.id FROM Task t JOIN t.groups grp WHERE t.updatedAt >= :since)")
    List<Group> findInactiveGroups(@Param("since") LocalDateTime since);
}
