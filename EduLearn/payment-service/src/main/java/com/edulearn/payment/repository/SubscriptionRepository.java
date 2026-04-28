package com.edulearn.payment.repository;

import com.edulearn.payment.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByStudentId(Long studentId);
    List<Subscription> findByStudentIdAndStatus(Long studentId, String status);
    List<Subscription> findByEndDateBefore(LocalDateTime date);
    Long countByPlan(String plan);
}
