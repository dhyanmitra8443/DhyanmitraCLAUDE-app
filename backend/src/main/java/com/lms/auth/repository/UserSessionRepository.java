package com.lms.auth.repository;

import com.lms.auth.entity.SessionStatus;
import com.lms.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByUserIdAndStatus(UUID userId, SessionStatus status);

    boolean existsByIdAndStatus(UUID id, SessionStatus status);
}
