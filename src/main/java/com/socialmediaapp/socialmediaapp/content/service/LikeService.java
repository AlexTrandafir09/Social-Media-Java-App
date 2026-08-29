package com.socialmediaapp.socialmediaapp.content.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityAction;
import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.content.entity.Like;
import com.socialmediaapp.socialmediaapp.content.entity.Post;
import com.socialmediaapp.socialmediaapp.content.exception.DuplicateLikeException;
import com.socialmediaapp.socialmediaapp.content.exception.LikeNotFoundException;
import com.socialmediaapp.socialmediaapp.content.exception.PostNotFoundException;
import com.socialmediaapp.socialmediaapp.content.repository.LikeRepository;
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
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public Like like(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new DuplicateLikeException(userId, postId);
        }
        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();
        Like saved = likeRepository.save(like);
        activityLogService.record(user, ActivityAction.LIKE_CREATED, "Like created: postId=" + postId);
        log.debug("Like created: userId={}, postId={}", userId, postId);
        return saved;
    }

    public void unlike(Long userId, Long postId) {
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new LikeNotFoundException(userId, postId));
        likeRepository.delete(like);
        activityLogService.record(like.getUser(), ActivityAction.LIKE_REMOVED, "Like removed: postId=" + postId);
        log.debug("Like removed: userId={}, postId={}", userId, postId);
    }

    @Transactional(readOnly = true)
    public List<Like> getLikesForPost(Long postId) {
        return likeRepository.findByPostId(postId);
    }

    @Transactional(readOnly = true)
    public long countLikesForPost(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
