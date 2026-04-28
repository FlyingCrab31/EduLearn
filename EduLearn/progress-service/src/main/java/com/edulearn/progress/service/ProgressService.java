package com.edulearn.progress.service;

import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.entity.Progress;

import java.util.List;

public interface ProgressService {
    
    Progress trackProgress(Long studentId, Long courseId, Long lessonId, Integer watchedSeconds);
    
    Progress markLessonComplete(Long studentId, Long courseId, Long lessonId, int totalLessonsInCourse);
    
    Double getCourseProgress(Long studentId, Long courseId, int totalLessonsInCourse);
    
    Progress getLessonProgress(Long studentId, Long lessonId);
    
    Certificate issueCertificate(Long studentId, Long courseId, String instructorName, String courseName);
    
    Certificate getCertificate(Long studentId, Long courseId);
    
    Certificate verifyCertificate(String verificationCode);
    
    List<Progress> getAllProgressByStudent(Long studentId);
    
    List<Certificate> getAllCertificatesByStudent(Long studentId);
}
