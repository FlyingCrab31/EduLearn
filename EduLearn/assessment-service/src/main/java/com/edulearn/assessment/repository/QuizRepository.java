package com.edulearn.assessment.repository;

import com.edulearn.assessment.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourseId(Long courseId);
    List<Quiz> findByIsPublished(Boolean isPublished);
    long countByCourseId(Long courseId);
}
