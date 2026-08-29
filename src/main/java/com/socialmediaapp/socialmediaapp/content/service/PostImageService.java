package com.socialmediaapp.socialmediaapp.content.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityAction;
import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.content.dto.PostImageCreateRequest;
import com.socialmediaapp.socialmediaapp.content.dto.PostImageUpdateRequest;
import com.socialmediaapp.socialmediaapp.content.entity.ImageFilter;
import com.socialmediaapp.socialmediaapp.content.entity.Post;
import com.socialmediaapp.socialmediaapp.content.entity.PostImage;
import com.socialmediaapp.socialmediaapp.content.exception.PostImageNotFoundException;
import com.socialmediaapp.socialmediaapp.content.exception.PostNotFoundException;
import com.socialmediaapp.socialmediaapp.content.repository.PostImageRepository;
import com.socialmediaapp.socialmediaapp.content.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostImageService {

    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final ActivityLogService activityLogService;

    public PostImage addImage(PostImageCreateRequest request) {
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new PostNotFoundException(request.postId()));
        PostImage image = PostImage.builder()
                .post(post)
                .storageKey(request.storageKey())
                .activeFilter(request.filter() != null ? request.filter() : ImageFilter.NONE)
                .build();
        PostImage saved = postImageRepository.save(image);
        activityLogService.record(post.getAuthor(), ActivityAction.POST_IMAGE_ADDED, "Image added to post " + post.getId());
        log.debug("Post image added: id={}, postId={}", saved.getId(), post.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public PostImage getImageById(Long id) {
        return postImageRepository.findById(id)
                .orElseThrow(() -> new PostImageNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<PostImage> getImagesForPost(Long postId) {
        return postImageRepository.findByPostId(postId);
    }

    public PostImage updateFilter(Long id, PostImageUpdateRequest request) {
        PostImage image = getImageById(id);
        image.setActiveFilter(request.filter());
        PostImage saved = postImageRepository.save(image);
        log.debug("Post image filter updated: id={}, filter={}", id, request.filter());
        return saved;
    }

    public void deleteImage(Long id) {
        PostImage image = getImageById(id);
        postImageRepository.deleteById(id);
        activityLogService.record(image.getPost().getAuthor(), ActivityAction.POST_IMAGE_DELETED, "Image deleted: " + id);
        log.debug("Post image deleted: id={}", id);
    }
}
