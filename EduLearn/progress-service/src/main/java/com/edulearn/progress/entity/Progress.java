package com.edulearn.progress.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long lessonId;

    private Integer watchedSeconds;

    @Column(nullable = false)
    private Boolean isCompleted;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        lastAccessedAt = LocalDateTime.now();
        if (isCompleted != null && isCompleted) {
            completedAt = LocalDateTime.now();
        } else {
            isCompleted = false;
        }
        if (watchedSeconds == null) {
            watchedSeconds = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastAccessedAt = LocalDateTime.now();
        if (isCompleted != null && isCompleted && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    public Progress() {}

    public Progress(Long progressId, Long studentId, Long courseId, Long lessonId, Integer watchedSeconds, Boolean isCompleted, LocalDateTime lastAccessedAt, LocalDateTime completedAt) {
        this.progressId = progressId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.watchedSeconds = watchedSeconds;
        this.isCompleted = isCompleted;
        this.lastAccessedAt = lastAccessedAt;
        this.completedAt = completedAt;
    }

    public Long getProgressId() { return progressId; }
    public void setProgressId(Long progressId) { this.progressId = progressId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Integer getWatchedSeconds() { return watchedSeconds; }
    public void setWatchedSeconds(Integer watchedSeconds) { this.watchedSeconds = watchedSeconds; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
