package com.socialmediaapp.userservice.user.service;

import com.socialmediaapp.userservice.messaging.ActivityEventPublisher;
import com.socialmediaapp.userservice.user.entity.User;
import com.socialmediaapp.userservice.user.exception.AvatarNotFoundException;
import com.socialmediaapp.userservice.user.exception.DuplicateEmailException;
import com.socialmediaapp.userservice.user.exception.DuplicateUsernameException;
import com.socialmediaapp.userservice.user.exception.InvalidCurrentPasswordException;
import com.socialmediaapp.userservice.user.exception.UserNotFoundException;
import com.socialmediaapp.userservice.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private ActivityEventPublisher activityEventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userPreferenceService, activityEventPublisher, passwordEncoder);
        user = User.builder()
                .id(1L)
                .username("alext")
                .email("alext@example.com")
                .password("secret")
                .build();
        authenticateAs(1L, "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId, String... authorities) {
        var grantedAuthorities = List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, grantedAuthorities));
    }

    @Test
    void createUser_savesWhenUsernameAndEmailAreUnique() {
        when(userRepository.existsByUsername("alext")).thenReturn(false);
        when(userRepository.existsByEmail("alext@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");
        when(userRepository.save(user)).thenReturn(user);

        User created = userService.createUser(user);

        assertThat(created).isEqualTo(user);
        assertThat(created.getPassword()).isEqualTo("hashed-secret");
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
    void searchUsers_returnsMatchingUsers() {
        when(userRepository.findByUsernameContainingIgnoreCase("ali")).thenReturn(List.of(user));

        List<User> users = userService.searchUsers("ali");

        assertThat(users).containsExactly(user);
    }

    @Test
    void searchUsers_returnsEmptyListWhenQueryBlank() {
        List<User> users = userService.searchUsers("  ");

        assertThat(users).isEmpty();
        verify(userRepository, never()).findByUsernameContainingIgnoreCase(any());
    }

    @Test
    void searchUsers_returnsEmptyListWhenQueryNull() {
        List<User> users = userService.searchUsers(null);

        assertThat(users).isEmpty();
    }

    @Test
    void updateUser_updatesBio() {
        User updates = User.builder()
                .bio("hello")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(1L, updates);

        assertThat(result.getBio()).isEqualTo("hello");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_throwsWhenUserNotFound() {
        authenticateAs(99L, "ROLE_USER");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, user))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_throwsWhenNotSelfAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");

        assertThatThrownBy(() -> userService.updateUser(1L, user))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_succeedsWhenAdminButNotSelf() {
        authenticateAs(2L, "ROLE_ADMIN");
        User updates = User.builder().bio("hello").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(1L, updates);

        assertThat(result.getBio()).isEqualTo("hello");
    }

    @Test
    void updateAvatar_decodesAndSavesAvatar() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateAvatar(1L, "image/png", "dGVzdC1pbWFnZS1ieXRlcw==");

        assertThat(result.getAvatarContentType()).isEqualTo("image/png");
        assertThat(result.getAvatarData()).isEqualTo("test-image-bytes".getBytes());
    }

    @Test
    void updateAvatar_throwsWhenNotSelfAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");

        assertThatThrownBy(() -> userService.updateAvatar(1L, "image/png", "dGVzdC1pbWFnZS1ieXRlcw=="))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserWithAvatar_returnsUserWhenAvatarSet() {
        user.setAvatarData("test-image-bytes".getBytes());
        user.setAvatarContentType("image/png");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserWithAvatar(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getUserWithAvatar_throwsWhenNoAvatarSet() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getUserWithAvatar(1L))
                .isInstanceOf(AvatarNotFoundException.class);
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
    void changeEmail_throwsWhenNotSelfAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");

        assertThatThrownBy(() -> userService.changeEmail(1L, "new@example.com"))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_succeedsWhenAdminButNotSelf() {
        authenticateAs(2L, "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeEmail(1L, "new@example.com");

        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void changePassword_updatesWhenCurrentPasswordMatches() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "secret")).thenReturn(true);
        when(passwordEncoder.encode("newSecret")).thenReturn("hashed-newSecret");

        userService.changePassword(1L, "secret", "newSecret");

        assertThat(user.getPassword()).isEqualTo("hashed-newSecret");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_throwsWhenCurrentPasswordIsWrong() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "secret")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "newSecret"))
                .isInstanceOf(InvalidCurrentPasswordException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsWhenNotSelfAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");

        assertThatThrownBy(() -> userService.changePassword(1L, "secret", "newSecret"))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_succeedsWhenAdminButNotSelf() {
        authenticateAs(2L, "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "secret")).thenReturn(true);
        when(passwordEncoder.encode("newSecret")).thenReturn("hashed-newSecret");

        userService.changePassword(1L, "secret", "newSecret");

        assertThat(user.getPassword()).isEqualTo("hashed-newSecret");
    }

    @Test
    void deleteUser_deletesWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_throwsWhenUserDoesNotExist() {
        authenticateAs(99L, "ROLE_USER");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_throwsWhenNotSelfAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_succeedsWhenAdminButNotSelf() {
        authenticateAs(2L, "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }
}
