package com.edulearn.enrollment.service.impl;

import com.edulearn.enrollment.entity.Enrollment;
import com.edulearn.enrollment.repository.EnrollmentRepository;
import com.edulearn.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    public Enrollment enroll(Long studentId, Long courseId) {
        if (isEnrolled(studentId, courseId)) {
            throw new RuntimeException("Student already enrolled in this course");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus("Active");
        enrollment.setProgressPercent(0);
        enrollment.setCertificateIssued(false);
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public void unenroll(Long enrollmentId) {
        enrollmentRepository.findById(enrollmentId).ifPresent(enrollment -> {
            enrollment.setStatus("Cancelled");
            enrollmentRepository.save(enrollment);
        });
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    public void updateProgress(Long enrollmentId, Integer progressPercent) {
        enrollmentRepository.findById(enrollmentId).ifPresent(enrollment -> {
            enrollment.setProgressPercent(progressPercent);
            if (progressPercent >= 100) {
                markComplete(enrollmentId);
            }
            enrollmentRepository.save(enrollment);
        });
    }

    @Override
    public void markComplete(Long enrollmentId) {
        enrollmentRepository.findById(enrollmentId).ifPresent(enrollment -> {
            enrollment.setStatus("Completed");
            enrollment.setCompletedAt(LocalDateTime.now());
            enrollment.setProgressPercent(100);
            enrollmentRepository.save(enrollment);
        });
    }

    @Override
    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public void issueCertificate(Long enrollmentId) {
        enrollmentRepository.findById(enrollmentId).ifPresent(enrollment -> {
            if ("Completed".equals(enrollment.getStatus())) {
                enrollment.setCertificateIssued(true);
                enrollmentRepository.save(enrollment);
            } else {
                throw new RuntimeException("Course not completed yet");
            }
        });
    }

    @Override
    public long getEnrollmentCount(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }
}
