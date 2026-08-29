package com.socialmediaapp.socialmediaapp.content.repository;

import com.socialmediaapp.socialmediaapp.content.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostId(Long postId);
}
