package com.lms.referral.repository;

import com.lms.referral.entity.Referral;
import com.lms.referral.entity.ReferralStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ReferralRepository extends JpaRepository<Referral, UUID>, JpaSpecificationExecutor<Referral> {

    Optional<Referral> findByTokenHash(String tokenHash);

    Page<Referral> findByReferrerId(UUID referrerId, Pageable pageable);

    /** Guards against the same referrer sending a duplicate pending referral to the same email. */
    boolean existsByRefereeEmailAndReferrerIdAndStatus(String refereeEmail, UUID referrerId, ReferralStatus status);
}
