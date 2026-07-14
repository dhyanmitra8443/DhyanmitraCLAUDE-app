package com.lms.settings.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Ref: SRS 16.4. PATCH semantics apply to every settings request in this
 * package: a null field means "leave unchanged", so an administrator can
 * update one field without resending the whole group.
 */
public record GeneralSettingsRequest(
        @Size(max = 200) String organizationName,
        String organizationLogoUrl,
        @Email String supportEmail,
        @Size(max = 20) String supportPhone,
        String websiteUrl,
        String organizationAddress
) {
}
