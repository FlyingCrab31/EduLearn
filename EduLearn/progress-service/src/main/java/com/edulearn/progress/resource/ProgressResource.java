package com.edulearn.progress.resource;

import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Progress & Certificates", description = "Endpoints for tracking progress and issuing certificates")
public class ProgressResource {

    @Autowired
    private ProgressService progressService;

    @Operation(summary = "Track learning progress for a lesson")
    @PostMapping("/progress/track")
    public ResponseEntity<Progress> trackProgress(@RequestBody Map<String, Object> payload) {
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        Long courseId = Long.valueOf(payload.get("courseId").toString());
        Long lessonId = Long.valueOf(payload.get("lessonId").toString());
        Integer watchedSeconds = Integer.valueOf(payload.get("watchedSeconds").toString());
        
        Progress progress = progressService.trackProgress(studentId, courseId, lessonId, watchedSeconds);
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "Mark a lesson as completed")
    @PostMapping("/progress/complete")
    public ResponseEntity<Progress> markLessonComplete(@RequestBody Map<String, Object> payload) {
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        Long courseId = Long.valueOf(payload.get("courseId").toString());
        Long lessonId = Long.valueOf(payload.get("lessonId").toString());
        int totalLessons = payload.containsKey("totalLessons") ? Integer.parseInt(payload.get("totalLessons").toString()) : 10; // Defaulting to 10 for demo if not provided
        
        Progress progress = progressService.markLessonComplete(studentId, courseId, lessonId, totalLessons);
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "Get course completion percentage")
    @GetMapping("/progress/course/{courseId}/student/{studentId}")
    public ResponseEntity<Double> getCourseProgress(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "10") int totalLessons) {
        Double progress = progressService.getCourseProgress(studentId, courseId, totalLessons);
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "Get progress for a specific lesson")
    @GetMapping("/progress/lesson/{lessonId}/student/{studentId}")
    public ResponseEntity<Progress> getLessonProgress(
            @PathVariable Long lessonId,
            @PathVariable Long studentId) {
        Progress progress = progressService.getLessonProgress(studentId, lessonId);
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "Get all progress records for a student")
    @GetMapping("/progress/student/{studentId}")
    public ResponseEntity<List<Progress>> getAllProgressByStudent(@PathVariable Long studentId) {
        List<Progress> progressList = progressService.getAllProgressByStudent(studentId);
        return ResponseEntity.ok(progressList);
    }

    @Operation(summary = "Get a certificate for a completed course")
    @GetMapping("/certificates/course/{courseId}/student/{studentId}")
    public ResponseEntity<Certificate> getCertificate(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        Certificate certificate = progressService.getCertificate(studentId, courseId);
        return ResponseEntity.ok(certificate);
    }

    @Operation(summary = "Get all certificates for a student")
    @GetMapping("/certificates/student/{studentId}")
    public ResponseEntity<List<Certificate>> getAllCertificatesByStudent(@PathVariable Long studentId) {
        List<Certificate> certificates = progressService.getAllCertificatesByStudent(studentId);
        return ResponseEntity.ok(certificates);
    }

    @Operation(summary = "Verify a certificate using its verification code")
    @GetMapping("/certificates/verify/{code}")
    public ResponseEntity<Certificate> verifyCertificate(@PathVariable String code) {
        Certificate certificate = progressService.verifyCertificate(code);
        return ResponseEntity.ok(certificate);
    }
}
