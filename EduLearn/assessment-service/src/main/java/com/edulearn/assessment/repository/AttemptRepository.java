package com.edulearn.assessment.repository;

import com.edulearn.assessment.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByStudentId(Long studentId);
    List<Attempt> findByQuizId(Long quizId);
    List<Attempt> findByStudentIdAndQuizId(Long studentId, Long quizId);
    long countByStudentIdAndQuizId(Long studentId, Long quizId);
    Optional<Attempt> findFirstByStudentIdAndQuizIdOrderByScoreDesc(Long studentId, Long quizId);
}
