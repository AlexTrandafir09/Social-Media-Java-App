package com.socialmediaapp.socialmediaapp.user.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateEmailException;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateUsernameException;
import com.socialmediaapp.socialmediaapp.user.exception.InvalidCurrentPasswordException;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceService userPreferenceService;

    @Mock
    private ActivityLogService activityLogService;

    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userPreferenceService, activityLogService);
        user = User.builder()
                .id(1L)
                .username("alext")
                .email("alext@example.com")
                .password("secret")
                .build();
    }

    @Test
    void createUser_savesWhenUsernameAndEmailAreUnique() {
        when(userRepository.existsByUsername("alext")).thenReturn(false);
        when(userRepository.existsByEmail("alext@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User created = userService.createUser(user);

        assertThat(created).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_throwsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("alext")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByUsername("alext")).thenReturn(false);
        when(userRepository.existsByEmail("alext@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_returnsUserWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getUserById(1L);

        assertThat(found).isEqualTo(user);
    }

    @Test
    void getUserById_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();

        assertThat(users).containsExactly(user);
    }

    @Test
    void updateUser_updatesBioAndAvatar() {
        User updates = User.builder()
                .bio("hello")
                .avatarUrl("avatar.png")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(1L, updates);

        assertThat(result.getBio()).isEqualTo("hello");
        assertThat(result.getAvatarUrl()).isEqualTo("avatar.png");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_throwsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, user))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_updatesWhenEmailIsUnused() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeEmail(1L, "new@example.com");

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void changeEmail_allowsSettingToOwnCurrentEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("alext@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeEmail(1L, "alext@example.com");

        assertThat(result.getEmail()).isEqualTo("alext@example.com");
    }

    @Test
    void changeEmail_throwsWhenEmailBelongsToAnotherUser() {
        User otherUser = User.builder().id(2L).email("taken@example.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> userService.changeEmail(1L, "taken@example.com"))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_updatesWhenCurrentPasswordMatches() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.changePassword(1L, "secret", "newSecret");

        assertThat(user.getPassword()).isEqualTo("newSecret");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_throwsWhenCurrentPasswordIsWrong() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "newSecret"))
                .isInstanceOf(InvalidCurrentPasswordException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_deletesWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_throwsWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }
}
