package com.edulearn.progress.service.impl;

import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.exception.CertificateNotFoundException;
import com.edulearn.progress.exception.ProgressNotFoundException;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgressServiceImpl implements ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Override
    public Progress trackProgress(Long studentId, Long courseId, Long lessonId, Integer watchedSeconds) {
        Progress progress = progressRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .orElse(new Progress());
        
        if (progress.getProgressId() == null) {
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
        Progress progress = progressRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .orElse(new Progress());
                
        if (progress.getProgressId() == null) {
            progress.setStudentId(studentId);
            progress.setCourseId(courseId);
            progress.setLessonId(lessonId);
            progress.setWatchedSeconds(0);
        }
        
        progress.setIsCompleted(true);
        progress = progressRepository.save(progress);
        
        // Check if course is fully completed
        Long completedLessons = progressRepository.countByStudentIdAndCourseIdAndIsCompletedTrue(studentId, courseId);
        if (completedLessons != null && completedLessons >= totalLessonsInCourse) {
            // Auto issue certificate if not already exists
            if (!certificateRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
                issueCertificate(studentId, courseId, "System Generated", "Course " + courseId);
            }
        }
        
        return progress;
    }

    @Override
    public Double getCourseProgress(Long studentId, Long courseId, int totalLessonsInCourse) {
        if (totalLessonsInCourse <= 0) return 0.0;
        Long completedLessons = progressRepository.countByStudentIdAndCourseIdAndIsCompletedTrue(studentId, courseId);
        return ((double) completedLessons / totalLessonsInCourse) * 100.0;
    }

    @Override
    public Progress getLessonProgress(Long studentId, Long lessonId) {
        return progressRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .orElseThrow(() -> new ProgressNotFoundException("Progress not found for student " + studentId + " and lesson " + lessonId));
    }

    @Override
    public Certificate issueCertificate(Long studentId, Long courseId, String instructorName, String courseName) {
        if (certificateRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("Certificate already issued for this course.");
        }
        
        Certificate certificate = new Certificate();
        certificate.setStudentId(studentId);
        certificate.setCourseId(courseId);
        certificate.setInstructorName(instructorName);
        certificate.setCourseName(courseName);
        certificate.setCertificateUrl("https://edulearn.com/certificates/" + studentId + "/" + courseId);
        
        return certificateRepository.save(certificate);
    }

    @Override
    public Certificate getCertificate(Long studentId, Long courseId) {
        return certificateRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new CertificateNotFoundException("Certificate not found for student " + studentId + " and course " + courseId));
    }

    @Override
    public Certificate verifyCertificate(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new CertificateNotFoundException("Invalid verification code: " + verificationCode));
    }

    @Override
    public List<Progress> getAllProgressByStudent(Long studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    @Override
    public List<Certificate> getAllCertificatesByStudent(Long studentId) {
        return certificateRepository.findByStudentId(studentId);
    }
}
