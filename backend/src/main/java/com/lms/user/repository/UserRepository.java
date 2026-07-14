package com.lms.user.repository;

import com.lms.user.entity.User;
import com.lms.user.entity.UserRole;
import com.lms.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    long countByRole(UserRole role);

    long countByRoleAndStatus(UserRole role, UserStatus status);

    long countByRoleAndCreatedAtBetween(UserRole role, java.time.OffsetDateTime from, java.time.OffsetDateTime to);
}
