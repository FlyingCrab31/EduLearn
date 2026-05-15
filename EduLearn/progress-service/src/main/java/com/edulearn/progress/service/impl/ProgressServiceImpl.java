package com.edulearn.progress.service.impl;

import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProgressServiceImpl implements ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Override
    public Progress trackProgress(Long studentId, Long courseId, Long lessonId, Integer watchedSeconds) {
        Optional<Progress> existing = progressRepository.findByStudentIdAndLessonId(studentId, lessonId);
        Progress progress = existing.orElse(new Progress());
        
        if (existing.isEmpty()) {
            progress.setStudentId(studentId);
            progress.setCourseId(courseId);
            progress.setLessonId(lessonId);
            progress.setIsCompleted(false);
        }
        
        progress.setWatchedSeconds(watchedSeconds);
        return progressRepository.save(progress);
    }

    @Override
    public Progress markLessonComplete(Long studentId, Long courseId, Long lessonId, int totalLessonsInCourse) {
        Optional<Progress> existing = progressRepository.findByStudentIdAndLessonId(studentId, lessonId);
        Progress progress = existing.orElse(new Progress());
        
        if (existing.isEmpty()) {
            progress.setStudentId(studentId);
            progress.setCourseId(courseId);
            progress.setLessonId(lessonId);
        }
        
        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        Progress saved = progressRepository.save(progress);
        return saved;
    }

    @Override
    public Double getCourseProgress(Long studentId, Long courseId, int totalLessonsInCourse) {
        long completedCount = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(studentId, courseId, true);
        return (double) completedCount / totalLessonsInCourse * 100;
    }

    @Override
    public Progress getLessonProgress(Long studentId, Long lessonId) {
        return progressRepository.findByStudentIdAndLessonId(studentId, lessonId).orElse(null);
    }

    @Override
    public Certificate issueManualCertificate(Long studentId, Long courseId, String instructorName, String courseName, String studentName) {
        Certificate cert = new Certificate();
        cert.setStudentId(studentId);
        cert.setCourseId(courseId);
        cert.setInstructorName(instructorName);
        cert.setCourseName(courseName);
        cert.setStudentName(studentName);
        cert.setCertificateUrl("https://edulearn.com/certificates/" + UUID.randomUUID().toString());
        cert.setVerificationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return certificateRepository.save(cert);
    }

    @Override
    public Certificate updateCertificateName(Long certificateId, String newStudentName) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        cert.setStudentName(newStudentName);
        return certificateRepository.save(cert);
    }

    @Override
    public Certificate getCertificate(Long studentId, Long courseId) {
        return certificateRepository.findByStudentIdAndCourseId(studentId, courseId).orElse(null);
    }

    @Override
    public Certificate verifyCertificate(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
    }

    @Override
    public List<Progress> getAllProgressByStudent(Long studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    @Override
    public List<Progress> getStudentProgressByCourse(Long studentId, Long courseId) {
        return progressRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    @Override
    public List<Certificate> getAllCertificatesByStudent(Long studentId) {
        return certificateRepository.findByStudentId(studentId);
    }

    @Override
    public Long getTotalStudyTime(Long studentId) {
        Long totalSeconds = progressRepository.sumWatchedSecondsByStudentId(studentId);
        return totalSeconds != null ? totalSeconds : 0L;
    }
}
