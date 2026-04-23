package com.edulearn.enrollment.service;

import com.edulearn.enrollment.entity.Enrollment;
import java.util.List;

public interface EnrollmentService {
    Enrollment enroll(Long studentId, Long courseId);
    void unenroll(Long enrollmentId);
    List<Enrollment> getEnrollmentsByStudent(Long studentId);
    List<Enrollment> getEnrollmentsByCourse(Long courseId);
    void updateProgress(Long enrollmentId, Integer progressPercent);
    void markComplete(Long enrollmentId);
    boolean isEnrolled(Long studentId, Long courseId);
    void issueCertificate(Long enrollmentId);
    long getEnrollmentCount(Long courseId);
}
