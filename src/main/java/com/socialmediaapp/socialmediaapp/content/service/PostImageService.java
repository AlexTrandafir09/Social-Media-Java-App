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
import com.socialmediaapp.socialmediaapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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

    public PostImage addImage(Long postId, PostImageCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        if (!SecurityUtils.isCurrentUserOrAdmin(post.getAuthor().getId())) {
            throw new AccessDeniedException("You can only add images to your own posts");
        }
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
    public PostImage getImageById(Long postId, Long id) {
        PostImage image = postImageRepository.findById(id)
                .orElseThrow(() -> new PostImageNotFoundException(id));
        requireBelongsToPost(image, postId);
        return image;
    }

    @Transactional(readOnly = true)
    public List<PostImage> getImagesForPost(Long postId) {
        return postImageRepository.findByPostId(postId);
    }

    public PostImage updateFilter(Long postId, Long id, PostImageUpdateRequest request) {
        PostImage image = getImageById(postId, id);
        if (!SecurityUtils.isCurrentUserOrAdmin(image.getPost().getAuthor().getId())) {
            throw new AccessDeniedException("You can only edit images on your own posts");
        }
        image.setActiveFilter(request.filter());
        PostImage saved = postImageRepository.save(image);
        log.debug("Post image filter updated: id={}, filter={}", id, request.filter());
        return saved;
    }

    public void deleteImage(Long postId, Long id) {
        PostImage image = getImageById(postId, id);
        if (!SecurityUtils.isCurrentUserOrAdmin(image.getPost().getAuthor().getId())) {
            throw new AccessDeniedException("You can only delete images on your own posts");
        }
        postImageRepository.deleteById(id);
        activityLogService.record(image.getPost().getAuthor(), ActivityAction.POST_IMAGE_DELETED, "Image deleted: " + id);
        log.debug("Post image deleted: id={}", id);
    }

    private void requireBelongsToPost(PostImage image, Long postId) {
        if (!image.getPost().getId().equals(postId)) {
            throw new PostImageNotFoundException(image.getId());
        }
    }
}
