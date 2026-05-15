package com.edulearn.assessment.service.impl;

import com.edulearn.assessment.exception.BadRequestException;
import com.edulearn.assessment.exception.ResourceNotFoundException;
import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.repository.AttemptRepository;
import com.edulearn.assessment.repository.QuizRepository;
import com.edulearn.assessment.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    @Override
    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        if (quiz.getQuestions() != null) {
            quiz.getQuestions().forEach(q -> q.setQuiz(quiz));
        }
        quiz.setIsPublished(false);
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional
    public Quiz updateQuiz(Long id, Quiz quizUpdate) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        quiz.setTitle(quizUpdate.getTitle());
        quiz.setDescription(quizUpdate.getDescription());
        quiz.setTimeLimitMinutes(quizUpdate.getTimeLimitMinutes());
        quiz.setPassingScore(quizUpdate.getPassingScore());
        quiz.setMaxAttempts(quizUpdate.getMaxAttempts());
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Quiz publishQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        quiz.setIsPublished(true);
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Quiz getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        if (quiz.getQuestions() != null) quiz.getQuestions().size(); // Initialize lazy list
        return quiz;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional
    public Question addQuestion(Long quizId, Question question) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        question.setQuiz(quiz);
        quiz.getQuestions().add(question);
        quizRepository.save(quiz);
        return question;
    }

    @Override
    @Transactional
    public Attempt startAttempt(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        // Check max attempts
        long count = attemptRepository.countByStudentIdAndQuizId(studentId, quizId);
        if (quiz.getMaxAttempts() != null && count >= quiz.getMaxAttempts()) {
            throw new BadRequestException("Maximum attempts reached for this quiz");
        }

        Attempt attempt = new Attempt();
        attempt.setQuizId(quizId);
        attempt.setStudentId(studentId);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setPassed(false);
        attempt.setScore(0);
        
        return attemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public Attempt submitAttempt(Long attemptId, Attempt submission) {
        Attempt attempt = attemptRepository.findById(attemptId).orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));
        if (attempt.getSubmittedAt() != null) {
            throw new BadRequestException("Attempt already submitted");
        }

        Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + attempt.getQuizId()));
        
        // Auto-grading logic
        int totalScore = 0;
        int maxPossibleScore = 0;
        Map<Long, String> userAnswers = submission.getAnswers();
        
        for (Question question : quiz.getQuestions()) {
            int marks = (question.getMarks() != null) ? question.getMarks() : 1;
            maxPossibleScore += marks;
            
            String correctAnswer = question.getCorrectAnswer();
            String userAnswer = userAnswers.get(question.getId());
            
            if (correctAnswer != null && correctAnswer.equalsIgnoreCase(userAnswer)) {
                totalScore += marks;
            }
        }

        int percentage = (maxPossibleScore > 0) ? (int) ((double) totalScore / maxPossibleScore * 100) : 0;

        attempt.setAnswers(userAnswers);
        attempt.setScore(percentage);
        attempt.setSubmittedAt(LocalDateTime.now());
        
        if (quiz.getPassingScore() != null) {
            attempt.setPassed(percentage >= quiz.getPassingScore());
        } else {
            attempt.setPassed(true);
        }

        return attemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public Attempt submitQuiz(Long quizId, Long studentId, Map<Long, String> answers) {
        Attempt attempt = startAttempt(quizId, studentId);
        
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        int totalScore = 0;
        int maxPossibleScore = 0;
        for (Question question : quiz.getQuestions()) {
            int marks = (question.getMarks() != null) ? question.getMarks() : 1;
            maxPossibleScore += marks;

            String correctAnswer = question.getCorrectAnswer();
            String userAnswer = answers.get(question.getId());
            
            if (correctAnswer != null && correctAnswer.equalsIgnoreCase(userAnswer)) {
                totalScore += marks;
            }
        }

        int percentage = (maxPossibleScore > 0) ? (int) ((double) totalScore / maxPossibleScore * 100) : 0;

        attempt.setAnswers(answers);
        attempt.setScore(percentage);
        attempt.setSubmittedAt(LocalDateTime.now());
        
        if (quiz.getPassingScore() != null) {
            attempt.setPassed(percentage >= quiz.getPassingScore());
        } else {
            attempt.setPassed(true);
        }

        return attemptRepository.save(attempt);
    }

    @Override
    public List<Attempt> getAttemptsByStudent(Long studentId) {
        return attemptRepository.findByStudentId(studentId);
    }

    @Override
    public Integer getBestScore(Long studentId, Long quizId) {
        return attemptRepository.findFirstByStudentIdAndQuizIdOrderByScoreDesc(studentId, quizId)
                .map(Attempt::getScore)
                .orElse(0);
    }
}
