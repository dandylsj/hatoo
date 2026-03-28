package com.hatoo.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(@NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email);

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    List<User> findAllByGroupsId(UUID groupId);
}
