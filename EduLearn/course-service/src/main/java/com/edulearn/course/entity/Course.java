package com.edulearn.course.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;
    
    private String level;
    
    private BigDecimal price;
    
    private Long instructorId;
    
    private String thumbnailUrl;
    
    private Integer totalDuration; // in minutes
    
    private Boolean isPublished = false;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private String language;

    public Course() {
    }

    public Course(Long courseId, String title, String description, String category, String level, BigDecimal price, Long instructorId, String thumbnailUrl, Integer totalDuration, Boolean isPublished, LocalDateTime createdAt, String language) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.level = level;
        this.price = price;
        this.instructorId = instructorId;
        this.thumbnailUrl = thumbnailUrl;
        this.totalDuration = totalDuration;
        this.isPublished = isPublished;
        this.createdAt = createdAt;
        this.language = language;
    }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Integer getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
