package com.socialmediaapp.socialmediaapp.user.service;

import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateEmailException;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateUsernameException;
import com.socialmediaapp.socialmediaapp.user.exception.InvalidCurrentPasswordException;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUsernameException(user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }
        return userRepository.save(user);
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
        User existing = getUserById(id);
        existing.setBio(updates.getBio());
        existing.setAvatarUrl(updates.getAvatarUrl());
        return userRepository.save(existing);
    }

    public User changeEmail(Long id, String newEmail) {
        User user = getUserById(id);
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateEmailException(newEmail);
            }
        });
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = getUserById(id);
        if (!user.getPassword().equals(currentPassword)) {
            throw new InvalidCurrentPasswordException();
        }
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
