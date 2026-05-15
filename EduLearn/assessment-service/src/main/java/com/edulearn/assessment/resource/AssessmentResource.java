package com.edulearn.assessment.resource;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
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
    @PostMapping("/attempts")
    public ResponseEntity<Attempt> submitQuiz(@RequestBody Map<String, Object> payload) {
        Long quizId = Long.valueOf(payload.get("quizId").toString());
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        @SuppressWarnings("unchecked")
        Map<String, String> rawAnswers = (Map<String, String>) payload.get("answers");
        
        java.util.Map<Long, String> answers = new java.util.HashMap<>();
        if (rawAnswers != null) {
            rawAnswers.forEach((k, v) -> answers.put(Long.valueOf(k), v));
        }
        
        return ResponseEntity.ok(assessmentService.submitQuiz(quizId, studentId, answers));
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
