package com.edulearn.assessment.resource;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentResource {

    @Autowired
    private AssessmentService assessmentService;

    // Quiz Endpoints
    @PostMapping("/quizzes")
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        return ResponseEntity.ok(assessmentService.createQuiz(quiz));
    }

    @PutMapping("/quizzes/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
        return ResponseEntity.ok(assessmentService.updateQuiz(id, quiz));
    }

    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        assessmentService.deleteQuiz(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quizzes/{id}/publish")
    public ResponseEntity<Quiz> publishQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.publishQuiz(id));
    }

    @GetMapping("/quizzes/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getQuizById(id));
    }

    @GetMapping("/quizzes/course/{courseId}")
    public ResponseEntity<List<Quiz>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assessmentService.getQuizzesByCourse(courseId));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<Question> addQuestion(@PathVariable Long quizId, @RequestBody Question question) {
        return ResponseEntity.ok(assessmentService.addQuestion(quizId, question));
    }

    // Attempt Endpoints
    @PostMapping("/attempts/start")
    public ResponseEntity<Attempt> startAttempt(@RequestParam Long quizId, @RequestParam Long studentId) {
        return ResponseEntity.ok(assessmentService.startAttempt(quizId, studentId));
    }

    @PostMapping("/attempts/{id}/submit")
    public ResponseEntity<Attempt> submitAttempt(@PathVariable Long id, @RequestBody Attempt submission) {
        return ResponseEntity.ok(assessmentService.submitAttempt(id, submission));
    }

    @GetMapping("/attempts/student/{studentId}")
    public ResponseEntity<List<Attempt>> getAttemptsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(assessmentService.getAttemptsByStudent(studentId));
    }

    @GetMapping("/attempts/best-score")
    public ResponseEntity<Integer> getBestScore(@RequestParam Long studentId, @RequestParam Long quizId) {
        return ResponseEntity.ok(assessmentService.getBestScore(studentId, quizId));
    }
}
