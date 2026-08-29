package com.socialmediaapp.contentservice.content.repository;

import com.socialmediaapp.contentservice.content.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    List<Like> findByPostId(Long postId);

    long countByPostId(Long postId);
}
