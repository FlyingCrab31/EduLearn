package com.edulearn.lesson.entity;

import jakarta.persistence.*;

/**
 * Entity representing a Lesson in the EduLearn LMS.
 *
 * <p>A lesson can be marked as {@code previewable = true} to allow
 * unenrolled users to watch it as a free preview.
 */
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer durationMinutes; // in minutes

    private Integer orderIndex;

    private Long courseId;

    /**
     * When {@code true}, this lesson is publicly accessible as a free preview
     * — no enrollment is required.
     */
    @Column(name = "previewable", nullable = false)
    private boolean previewable = false;

    @Column(name = "is_preview", nullable = false)
    private boolean isPreview = false;

    private String contentType; // e.g., VIDEO, ARTICLE, PDF

    private String contentUrl;

    public Lesson() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public boolean isPreview() { return isPreview; }
    public void setIsPreview(boolean isPreview) { 
        this.isPreview = isPreview; 
        this.previewable = isPreview;
    }

    public boolean isPreviewable() { return previewable; }
    public void setPreviewable(boolean previewable) { 
        this.previewable = previewable; 
        this.isPreview = previewable;
    }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
}
