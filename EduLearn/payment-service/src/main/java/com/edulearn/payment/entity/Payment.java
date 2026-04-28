package com.edulearn.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    private Long studentId;
    private Long courseId;
    private Double amount;
    private String status;
    private String mode;
    private String transactionId;
    private LocalDateTime paidAt;
    private String currency;

    public Payment() {}

    public Payment(Long paymentId, Long studentId, Long courseId, Double amount, String status, String mode, String transactionId, LocalDateTime paidAt, String currency) {
        this.paymentId = paymentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.amount = amount;
        this.status = status;
        this.mode = mode;
        this.transactionId = transactionId;
        this.paidAt = paidAt;
        this.currency = currency;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
