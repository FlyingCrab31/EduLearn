package com.edulearn.payment.service;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment processPayment(Payment payment);
    List<Payment> getPaymentsByStudent(Long studentId);
    Subscription subscribe(Long studentId, String plan);
    void cancelSubscription(Long subscriptionId);
    Subscription renewSubscription(Long subscriptionId);
    Optional<Subscription> getSubscriptionByStudent(Long studentId);
    boolean isSubscriptionActive(Long studentId);
    Payment refundPayment(Long paymentId);
}
