package com.edulearn.assessment;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.repository.AttemptRepository;
import com.edulearn.assessment.repository.QuizRepository;
import com.edulearn.assessment.service.impl.AssessmentServiceImpl;
import com.edulearn.assessment.exception.BadRequestException;
import com.edulearn.assessment.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssessmentServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    private Quiz testQuiz;
    private Question q1, q2;

    @BeforeEach
    void setUp() {
        testQuiz = new Quiz();
        testQuiz.setId(1L);
        testQuiz.setPassingScore(7);
        testQuiz.setMaxAttempts(2);

        q1 = new Question();
        q1.setId(101L);
        q1.setCorrectAnswer("A");
        q1.setMarks(5);
        q1.setQuiz(testQuiz);

        q2 = new Question();
        q2.setId(102L);
        q2.setCorrectAnswer("True");
        q2.setMarks(5);
        q2.setQuiz(testQuiz);

        testQuiz.setQuestions(Arrays.asList(q1, q2));
    }

    @Test
    void testStartAttempt_Success() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(attemptRepository.countByStudentIdAndQuizId(1L, 1L)).thenReturn(0L);
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(i -> i.getArguments()[0]);

        Attempt attempt = assessmentService.startAttempt(1L, 1L);

        assertNotNull(attempt);
        assertEquals(1L, attempt.getQuizId());
        assertEquals(1L, attempt.getStudentId());
        assertNotNull(attempt.getStartedAt());
        assertNull(attempt.getSubmittedAt());
    }

    @Test
    void testStartAttempt_MaxAttemptsExceeded() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(attemptRepository.countByStudentIdAndQuizId(1L, 1L)).thenReturn(2L);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            assessmentService.startAttempt(1L, 1L);
        });

        assertEquals("Maximum attempts reached for this quiz", exception.getMessage());
    }

    @Test
    void testSubmitAttempt_Passing() {
        Attempt attempt = new Attempt();
        attempt.setId(1L);
        attempt.setQuizId(1L);
        attempt.setStudentId(1L);

        Attempt submission = new Attempt();
        Map<Long, String> answers = new HashMap<>();
        answers.put(101L, "A"); // Correct (5 marks)
        answers.put(102L, "True"); // Correct (5 marks)
        submission.setAnswers(answers);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(i -> i.getArguments()[0]);

        Attempt result = assessmentService.submitAttempt(1L, submission);

        assertEquals(10, result.getScore());
        assertTrue(result.getPassed());
        assertNotNull(result.getSubmittedAt());
    }

    @Test
    void testSubmitAttempt_Failing() {
        Attempt attempt = new Attempt();
        attempt.setId(1L);
        attempt.setQuizId(1L);

        Attempt submission = new Attempt();
        Map<Long, String> answers = new HashMap<>();
        answers.put(101L, "A"); // Correct (5 marks)
        answers.put(102L, "False"); // Incorrect (0 marks)
        submission.setAnswers(answers);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(i -> i.getArguments()[0]);

        Attempt result = assessmentService.submitAttempt(1L, submission);

        assertEquals(5, result.getScore());
        assertFalse(result.getPassed()); // Passing score is 7
    }

    @Test
    void testGetBestScore() {
        Attempt bestAttempt = new Attempt();
        bestAttempt.setScore(9);

        when(attemptRepository.findFirstByStudentIdAndQuizIdOrderByScoreDesc(1L, 1L))
                .thenReturn(Optional.of(bestAttempt));

        Integer score = assessmentService.getBestScore(1L, 1L);

        assertEquals(9, score);
    }

    @Test
    void testGetQuizById_NotFound() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            assessmentService.getQuizById(999L);
        });
    }
}
