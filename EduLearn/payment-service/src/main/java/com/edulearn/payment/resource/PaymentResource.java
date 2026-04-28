package com.edulearn.payment.resource;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;
import com.edulearn.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Payment & Subscription", description = "Endpoints for processing payments and managing subscriptions")
public class PaymentResource {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payments")
    @Operation(summary = "Process a new payment", description = "Creates a payment record and returns the processed payment")
    public ResponseEntity<Payment> processPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.processPayment(payment));
    }

    @GetMapping("/payments/student/{studentId}")
    @Operation(summary = "Get payments by student ID", description = "Returns a list of all payments made by a specific student")
    public ResponseEntity<List<Payment>> getPaymentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(paymentService.getPaymentsByStudent(studentId));
    }

    @PostMapping("/payments/refund/{paymentId}")
    @Operation(summary = "Refund a payment", description = "Marks a payment as refunded")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Subscribe to a plan", description = "Enrolls a student in a subscription plan (Free/Monthly/Annual)")
    public ResponseEntity<Subscription> subscribe(@RequestParam Long studentId, @RequestParam String plan) {
        return ResponseEntity.ok(paymentService.subscribe(studentId, plan));
    }

    @GetMapping("/subscriptions/student/{studentId}")
    @Operation(summary = "Get subscription by student ID", description = "Returns the active subscription for a specific student")
    public ResponseEntity<Subscription> getSubscription(@PathVariable Long studentId) {
        return paymentService.getSubscriptionByStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/subscriptions/{subscriptionId}")
    @Operation(summary = "Cancel a subscription", description = "Cancels an active subscription")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long subscriptionId) {
        paymentService.cancelSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/subscriptions/renew/{subscriptionId}")
    @Operation(summary = "Renew a subscription", description = "Renews an existing subscription for another term")
    public ResponseEntity<Subscription> renewSubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(paymentService.renewSubscription(subscriptionId));
    }

    @GetMapping("/subscriptions/check/{studentId}")
    @Operation(summary = "Check if subscription is active", description = "Returns true if the student has an active, non-expired subscription")
    public ResponseEntity<Boolean> isSubscriptionActive(@PathVariable Long studentId) {
        return ResponseEntity.ok(paymentService.isSubscriptionActive(studentId));
    }
}
