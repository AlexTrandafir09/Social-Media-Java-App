package com.socialmediaapp.socialmediaapp.user.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityAction;
import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateEmailException;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateUsernameException;
import com.socialmediaapp.socialmediaapp.security.SecurityUtils;
import com.socialmediaapp.socialmediaapp.user.exception.InvalidCurrentPasswordException;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceService userPreferenceService;
    private final ActivityLogService activityLogService;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUsernameException(user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        userPreferenceService.createDefault(saved);
        activityLogService.record(saved, ActivityAction.USER_REGISTERED, "User registered: " + saved.getUsername());
        log.info("User registered: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updates) {
        if (!SecurityUtils.isCurrentUserOrAdmin(id)) {
            throw new AccessDeniedException("You can only edit your own profile");
        }
        User existing = getUserById(id);
        existing.setBio(updates.getBio());
        existing.setAvatarUrl(updates.getAvatarUrl());
        return userRepository.save(existing);
    }

    public User changeEmail(Long id, String newEmail) {
        if (!SecurityUtils.isCurrentUserOrAdmin(id)) {
            throw new AccessDeniedException("You can only change your own email");
        }
        User user = getUserById(id);
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateEmailException(newEmail);
            }
        });
        user.setEmail(newEmail);
        User saved = userRepository.save(user);
        activityLogService.record(saved, ActivityAction.EMAIL_CHANGED, "Email changed for " + saved.getUsername());
        log.info("Email changed: userId={}", id);
        return saved;
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        if (!SecurityUtils.isCurrentUserOrAdmin(id)) {
            throw new AccessDeniedException("You can only change your own password");
        }
        User user = getUserById(id);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        activityLogService.record(user, ActivityAction.PASSWORD_CHANGED, "Password changed for " + user.getUsername());
        log.info("Password changed: userId={}", id);
    }

    public void deleteUser(Long id) {
        if (!SecurityUtils.isCurrentUserOrAdmin(id)) {
            throw new AccessDeniedException("You can only delete your own account");
        }
        User user = getUserById(id);
        activityLogService.record(user, ActivityAction.USER_DELETED, "User deleted: " + user.getUsername());
        userRepository.delete(user);
        log.info("User deleted: id={}, username={}", id, user.getUsername());
    }
}
