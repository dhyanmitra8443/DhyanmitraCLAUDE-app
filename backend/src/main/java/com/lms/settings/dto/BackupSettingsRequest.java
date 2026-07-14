package com.lms.settings.dto;

import com.lms.settings.entity.BackupFrequency;
import jakarta.validation.constraints.Min;

/** Ref: SRS 16.13. */
public record BackupSettingsRequest(
        String backupLocation,
        BackupFrequency backupFrequency,
        @Min(1) Integer retentionDays
) {
}
