package com.edulearn.payment.repository;

import com.edulearn.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByCourseId(Long courseId);
    List<Payment> findByStatus(String status);
    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.studentId = :studentId")
    Double sumAmountByStudentId(Long studentId);
}
