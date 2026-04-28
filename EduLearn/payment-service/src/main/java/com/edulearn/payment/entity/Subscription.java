package com.edulearn.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;
    private Long studentId;
    private String plan;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Double amountPaid;
    private Boolean autoRenew;

    public Subscription() {}

    public Subscription(Long subscriptionId, Long studentId, String plan, LocalDateTime startDate, LocalDateTime endDate, String status, Double amountPaid, Boolean autoRenew) {
        this.subscriptionId = subscriptionId;
        this.studentId = studentId;
        this.plan = plan;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.amountPaid = amountPaid;
        this.autoRenew = autoRenew;
    }

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }
    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }
}
