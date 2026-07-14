package com.lms.settings.repository;

import com.lms.settings.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Ref: SRS Chapter 16 - singleton table; the migration seeds the only row. */
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, java.util.UUID> {

    /**
     * The single settings row. Deliberately not findById(...) - no caller
     * anywhere knows or should know the seeded row's UUID.
     */
    default Optional<SystemSettings> findSingleton() {
        return findAll().stream().findFirst();
    }
}
