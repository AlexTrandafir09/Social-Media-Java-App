package com.socialmediaapp.socialmediaapp.content.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityAction;
import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.content.dto.PostCreateRequest;
import com.socialmediaapp.socialmediaapp.content.dto.PostUpdateRequest;
import com.socialmediaapp.socialmediaapp.content.entity.ImageFilter;
import com.socialmediaapp.socialmediaapp.content.entity.Post;
import com.socialmediaapp.socialmediaapp.content.entity.PostImage;
import com.socialmediaapp.socialmediaapp.content.exception.PostMustHaveImageException;
import com.socialmediaapp.socialmediaapp.content.exception.PostNotFoundException;
import com.socialmediaapp.socialmediaapp.content.repository.PostImageRepository;
import com.socialmediaapp.socialmediaapp.content.repository.PostRepository;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public Post createPost(PostCreateRequest request) {
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException(request.authorId()));
        if (request.images() == null || request.images().isEmpty()) {
            throw new PostMustHaveImageException();
        }

        Post post = Post.builder()
                .author(author)
                .content(request.content())
                .build();
        Post savedPost = postRepository.save(post);

        List<PostImage> images = request.images().stream()
                .map(img -> PostImage.builder()
                        .post(savedPost)
                        .storageKey(img.storageKey())
                        .activeFilter(img.filter() != null ? img.filter() : ImageFilter.NONE)
                        .build())
                .toList();
        postImageRepository.saveAll(images);
        savedPost.setImages(images);
        activityLogService.record(author, ActivityAction.POST_CREATED, "Post created: " + savedPost.getId());
        log.info("Post created: id={}, authorId={}", savedPost.getId(), author.getId());
        return savedPost;
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    public Post updatePost(Long id, PostUpdateRequest request) {
        Post post = getPostById(id);
        post.setContent(request.content());
        Post saved = postRepository.save(post);
        activityLogService.record(post.getAuthor(), ActivityAction.POST_UPDATED, "Post updated: " + id);
        log.info("Post updated: id={}", id);
        return saved;
    }

    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.deleteById(id);
        activityLogService.record(post.getAuthor(), ActivityAction.POST_DELETED, "Post deleted: " + id);
        log.info("Post deleted: id={}", id);
    }
}
