package com.edulearn.progress.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificateId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private String certificateUrl;

    @Column(nullable = false, unique = true)
    private String verificationCode;

    private String instructorName;

    private String courseName;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
        if (verificationCode == null) {
            verificationCode = UUID.randomUUID().toString();
        }
    }

    public Certificate() {}

    public Certificate(Long certificateId, Long studentId, Long courseId, LocalDateTime issuedAt, String certificateUrl, String verificationCode, String instructorName, String courseName) {
        this.certificateId = certificateId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.issuedAt = issuedAt;
        this.certificateUrl = certificateUrl;
        this.verificationCode = verificationCode;
        this.instructorName = instructorName;
        this.courseName = courseName;
    }

    public Long getCertificateId() { return certificateId; }
    public void setCertificateId(Long certificateId) { this.certificateId = certificateId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
