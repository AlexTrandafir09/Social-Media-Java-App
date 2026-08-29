package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.entity.Like;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.exception.DuplicateLikeException;
import com.socialmediaapp.contentservice.content.exception.LikeNotFoundException;
import com.socialmediaapp.contentservice.content.exception.PostNotFoundException;
import com.socialmediaapp.contentservice.content.repository.LikeRepository;
import com.socialmediaapp.contentservice.content.repository.PostRepository;
import com.socialmediaapp.contentservice.messaging.ActivityAction;
import com.socialmediaapp.contentservice.messaging.ActivityEvent;
import com.socialmediaapp.contentservice.messaging.ActivityEventPublisher;
import com.socialmediaapp.contentservice.messaging.NotificationType;
import com.socialmediaapp.contentservice.notification.NotificationService;
import com.socialmediaapp.contentservice.security.SecurityUtils;
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
    private final ActivityEventPublisher activityEventPublisher;
    private final NotificationService notificationService;

    public Like like(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new DuplicateLikeException(userId, postId);
        }
        Like like = Like.builder()
                .userId(userId)
                .post(post)
                .build();
        Like saved = likeRepository.save(like);
        activityEventPublisher.publish(new ActivityEvent(userId, ActivityAction.LIKE_CREATED, "Like created: postId=" + postId));
        notificationService.notifyIfEnabled(post.getAuthorId(), userId, NotificationType.LIKE, postId);
        log.debug("Like created: userId={}, postId={}", userId, postId);
        return saved;
    }

    public void unlike(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new LikeNotFoundException(userId, postId));
        likeRepository.delete(like);
        activityEventPublisher.publish(new ActivityEvent(like.getUserId(), ActivityAction.LIKE_REMOVED, "Like removed: postId=" + postId));
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
