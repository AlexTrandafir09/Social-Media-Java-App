package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.dto.PostCreateRequest;
import com.socialmediaapp.contentservice.content.dto.PostUpdateRequest;
import com.socialmediaapp.contentservice.content.entity.ImageFilter;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.entity.PostImage;
import com.socialmediaapp.contentservice.content.exception.PostMustHaveImageException;
import com.socialmediaapp.contentservice.content.exception.PostNotFoundException;
import com.socialmediaapp.contentservice.content.repository.CommentRepository;
import com.socialmediaapp.contentservice.content.repository.LikeRepository;
import com.socialmediaapp.contentservice.content.repository.PostImageRepository;
import com.socialmediaapp.contentservice.content.repository.PostRepository;
import com.socialmediaapp.contentservice.messaging.ActivityAction;
import com.socialmediaapp.contentservice.messaging.ActivityEvent;
import com.socialmediaapp.contentservice.messaging.ActivityEventPublisher;
import com.socialmediaapp.contentservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ActivityEventPublisher activityEventPublisher;

    public Post createPost(PostCreateRequest request) {
        Long authorId = SecurityUtils.getCurrentUserId();
        if (request.images() == null || request.images().isEmpty()) {
            throw new PostMustHaveImageException();
        }

        Post post = Post.builder()
                .authorId(authorId)
                .content(request.content())
                .build();
        Post savedPost = postRepository.save(post);

        List<PostImage> images = request.images().stream()
                .map(img -> PostImage.builder()
                        .post(savedPost)
                        .storageKey(img.storageKey())
                        .contentType(img.contentType())
                        .data(Base64.getDecoder().decode(img.data()))
                        .activeFilter(img.filter() != null ? img.filter() : ImageFilter.NONE)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
        postImageRepository.saveAll(images);
        savedPost.setImages(images);
        activityEventPublisher.publish(new ActivityEvent(authorId, ActivityAction.POST_CREATED, "Post created: " + savedPost.getId()));
        log.info("Post created: id={}, authorId={}", savedPost.getId(), authorId);
        return savedPost;
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // authorIds narrows the feed to specific authors (e.g. "people I follow" -
    // that list is resolved by the caller, since this service doesn't know
    // about follow relationships).
    @Transactional(readOnly = true)
    public Page<Post> getPostsByAuthors(List<Long> authorIds, Pageable pageable) {
        return postRepository.findByAuthorIdIn(authorIds, pageable);
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    public Post updatePost(Long id, PostUpdateRequest request) {
        Post post = getPostById(id);
        if (!SecurityUtils.isCurrentUserOrAdmin(post.getAuthorId())) {
            throw new AccessDeniedException("You can only edit your own posts");
        }
        post.setContent(request.content());
        Post saved = postRepository.save(post);
        activityEventPublisher.publish(new ActivityEvent(post.getAuthorId(), ActivityAction.POST_UPDATED, "Post updated: " + id));
        log.info("Post updated: id={}", id);
        return saved;
    }

    public void deletePost(Long id) {
        Post post = getPostById(id);
        if (!SecurityUtils.isCurrentUserOrAdmin(post.getAuthorId())) {
            throw new AccessDeniedException("You can only delete your own posts");
        }
        likeRepository.deleteByPostId(id);
        commentRepository.deleteByPostId(id);
        postImageRepository.deleteByPostId(id);
        postRepository.deleteById(id);
        activityEventPublisher.publish(new ActivityEvent(post.getAuthorId(), ActivityAction.POST_DELETED, "Post deleted: " + id));
        log.info("Post deleted: id={}", id);
    }
}
