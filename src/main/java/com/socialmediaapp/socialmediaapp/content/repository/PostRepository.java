package com.socialmediaapp.socialmediaapp.content.repository;

import com.socialmediaapp.socialmediaapp.content.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorId(Long authorId);
}
