package com.edulearn.payment.service.impl;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;
import com.edulearn.payment.exception.PaymentNotFoundException;
import com.edulearn.payment.exception.SubscriptionNotFoundException;
import com.edulearn.payment.repository.PaymentRepository;
import com.edulearn.payment.repository.SubscriptionRepository;
import com.edulearn.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public Payment processPayment(Payment payment) {
        payment.setPaidAt(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        if (payment.getTransactionId() == null) {
            payment.setTransactionId(UUID.randomUUID().toString());
        }
        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getPaymentsByStudent(Long studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

    @Override
    public Subscription subscribe(Long studentId, String plan) {
        Subscription subscription = new Subscription();
        subscription.setStudentId(studentId);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDateTime.now());
        
        if ("Annual".equalsIgnoreCase(plan)) {
            subscription.setEndDate(LocalDateTime.now().plusYears(1));
            subscription.setAmountPaid(99.99);
        } else if ("Monthly".equalsIgnoreCase(plan)) {
            subscription.setEndDate(LocalDateTime.now().plusMonths(1));
            subscription.setAmountPaid(9.99);
        } else {
            subscription.setEndDate(LocalDateTime.now().plusYears(100)); // Free plan
            subscription.setAmountPaid(0.0);
        }
        
        subscription.setStatus("ACTIVE");
        subscription.setAutoRenew(true);
        return subscriptionRepository.save(subscription);
    }

    @Override
    public void cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found with ID: " + subscriptionId));
        subscription.setStatus("CANCELLED");
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription renewSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found with ID: " + subscriptionId));
        
        subscription.setStartDate(LocalDateTime.now());
        if ("Annual".equalsIgnoreCase(subscription.getPlan())) {
            subscription.setEndDate(LocalDateTime.now().plusYears(1));
        } else if ("Monthly".equalsIgnoreCase(subscription.getPlan())) {
            subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        }
        subscription.setStatus("ACTIVE");
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Optional<Subscription> getSubscriptionByStudent(Long studentId) {
        return subscriptionRepository.findByStudentId(studentId);
    }

    @Override
    public boolean isSubscriptionActive(Long studentId) {
        return subscriptionRepository.findByStudentId(studentId)
                .map(sub -> "ACTIVE".equalsIgnoreCase(sub.getStatus()) && sub.getEndDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }
}
