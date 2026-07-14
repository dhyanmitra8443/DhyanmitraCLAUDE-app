package com.lms.payment.entity;

/** Ref: SRS Chapter 10 - the orders.status CHECK constraint values. */
public enum OrderStatus {
    PENDING,
    PAID,
    FAILED,
    CANCELLED
}
