package com.edulearn.assessment.service;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;

import java.util.List;

public interface AssessmentService {
    // Quiz lifecycle
    Quiz createQuiz(Quiz quiz);
    Quiz updateQuiz(Long id, Quiz quiz);
    void deleteQuiz(Long id);
    Quiz publishQuiz(Long id);
    Quiz getQuizById(Long id);
    List<Quiz> getQuizzesByCourse(Long courseId);
    
    // Question management
    Question addQuestion(Long quizId, Question question);
    
    // Attempt lifecycle
    Attempt startAttempt(Long quizId, Long studentId);
    Attempt submitAttempt(Long attemptId, Attempt submission);
    List<Attempt> getAttemptsByStudent(Long studentId);
    Integer getBestScore(Long studentId, Long quizId);
}
