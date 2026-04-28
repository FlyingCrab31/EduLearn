package com.edulearn.discussion.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "replies")
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int replyId;

    private int threadId;
    private int authorId;
    
    @Column(columnDefinition = "TEXT")
    private String body;
    
    private boolean isAccepted;
    private int upvotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Reply() {}

    public Reply(int replyId, int threadId, int authorId, String body, boolean isAccepted, int upvotes, LocalDateTime createdAt) {
        this.replyId = replyId;
        this.threadId = threadId;
        this.authorId = authorId;
        this.body = body;
        this.isAccepted = isAccepted;
        this.upvotes = upvotes;
        this.createdAt = createdAt;
    }

    public int getReplyId() {
        return replyId;
    }

    public void setReplyId(int replyId) {
        this.replyId = replyId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
