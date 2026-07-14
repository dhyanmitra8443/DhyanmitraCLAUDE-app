package com.lms.payment.entity;

/** Ref: SRS Chapter 10 - the payments.status CHECK constraint values. */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED
}
