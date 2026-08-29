package com.socialmediaapp.contentservice.content.repository;

import com.socialmediaapp.contentservice.content.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    Page<Post> findByAuthorIdIn(List<Long> authorIds, Pageable pageable);
}
