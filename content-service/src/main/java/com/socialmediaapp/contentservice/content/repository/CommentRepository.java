package com.socialmediaapp.contentservice.content.repository;

import com.socialmediaapp.contentservice.content.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    List<Comment> findByAuthorId(Long authorId);

    void deleteByPostId(Long postId);
}
