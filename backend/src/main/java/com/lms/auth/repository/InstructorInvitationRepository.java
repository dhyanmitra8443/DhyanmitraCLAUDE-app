package com.lms.auth.repository;

import com.lms.auth.entity.InstructorInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstructorInvitationRepository extends JpaRepository<InstructorInvitation, UUID> {

    Optional<InstructorInvitation> findByTokenHash(String tokenHash);
}
