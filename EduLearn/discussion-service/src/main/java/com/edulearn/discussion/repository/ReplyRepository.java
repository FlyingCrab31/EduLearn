package com.edulearn.discussion.repository;

import com.edulearn.discussion.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    List<Reply> findByThreadId(int threadId);
}
