package com.lms.user.entity;

import com.lms.shared.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** Ref: SRS Chapter 3/4 - the users table; single table for all three roles. */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, columnDefinition = "citext")
    private String email;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    // Instructor-only fields (Ref: SRS 4.7)
    @Column(name = "professional_bio")
    private String professionalBio;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    // EAGER, not LAZY: this is a tiny collection (a handful of short strings)
    // and User is read outside a transaction in several places (e.g.
    // UserService.getOwnProfile) - LAZY here throws
    // LazyInitializationException the moment a DTO factory or Jackson
    // touches it after the loading transaction has already closed, since
    // open-in-view is deliberately disabled (Ref: application.yml).
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "instructor_specializations", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "specialization")
    private Set<String> specializations = new HashSet<>();
}
