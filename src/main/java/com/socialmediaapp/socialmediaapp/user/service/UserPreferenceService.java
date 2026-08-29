package com.socialmediaapp.socialmediaapp.user.service;

import com.socialmediaapp.socialmediaapp.security.SecurityUtils;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.exception.UserPreferenceNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreference createDefault(User user) {
        UserPreference preference = UserPreference.builder()
                .user(user)
                .build();
        return userPreferenceRepository.save(preference);
    }

    @Transactional(readOnly = true)
    public UserPreference getByUserId(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new UserPreferenceNotFoundException(userId));
    }

    public UserPreference update(Long userId, boolean notifyOnLike, boolean notifyOnComment, boolean notifyOnFollow) {
        if (!SecurityUtils.isCurrentUserOrAdmin(userId)) {
            throw new AccessDeniedException("You can only update your own preferences");
        }
        UserPreference preference = getByUserId(userId);
        preference.setNotifyOnLike(notifyOnLike);
        preference.setNotifyOnComment(notifyOnComment);
        preference.setNotifyOnFollow(notifyOnFollow);
        UserPreference saved = userPreferenceRepository.save(preference);
        log.debug("Preferences updated: userId={}", userId);
        return saved;
    }
}
