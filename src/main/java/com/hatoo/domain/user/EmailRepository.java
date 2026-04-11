package com.hatoo.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);
}
