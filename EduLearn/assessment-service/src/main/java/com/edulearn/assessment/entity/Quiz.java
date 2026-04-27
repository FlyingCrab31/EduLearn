package com.edulearn.assessment.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private Boolean isPublished;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions;

    public Quiz() {}

    public Quiz(Long id, Long courseId, String title, String description, Integer timeLimitMinutes, Integer passingScore, Integer maxAttempts, Boolean isPublished, List<Question> questions) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore;
        this.maxAttempts = maxAttempts;
        this.isPublished = isPublished;
        this.questions = questions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }
    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
