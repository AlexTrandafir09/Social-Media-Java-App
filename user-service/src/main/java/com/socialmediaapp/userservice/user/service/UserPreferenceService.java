package com.socialmediaapp.userservice.user.service;

import com.socialmediaapp.userservice.security.SecurityUtils;
import com.socialmediaapp.userservice.user.entity.User;
import com.socialmediaapp.userservice.user.entity.UserPreference;
import com.socialmediaapp.userservice.user.exception.UserPreferenceNotFoundException;
import com.socialmediaapp.userservice.user.repository.UserPreferenceRepository;
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
