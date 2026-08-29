package com.socialmediaapp.socialmediaapp.user.service;

import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.entity.UserPreference;
import com.socialmediaapp.socialmediaapp.user.exception.UserPreferenceNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    private UserPreferenceService userPreferenceService;

    private User user;
    private UserPreference preference;

    @BeforeEach
    void setUp() {
        userPreferenceService = new UserPreferenceService(userPreferenceRepository);
        user = User.builder().id(1L).username("alice").build();
        preference = UserPreference.builder().id(1L).user(user).build();
    }

    @Test
    void createDefault_savesPreferenceForUser() {
        when(userPreferenceRepository.save(any(UserPreference.class))).thenReturn(preference);

        UserPreference result = userPreferenceService.createDefault(user);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.isNotifyOnLike()).isTrue();
        assertThat(result.isNotifyOnComment()).isTrue();
        assertThat(result.isNotifyOnFollow()).isTrue();
    }

    @Test
    void getByUserId_returnsPreferenceWhenFound() {
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));

        UserPreference result = userPreferenceService.getByUserId(1L);

        assertThat(result).isEqualTo(preference);
    }

    @Test
    void getByUserId_throwsWhenNotFound() {
        when(userPreferenceRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPreferenceService.getByUserId(99L))
                .isInstanceOf(UserPreferenceNotFoundException.class);
    }

    @Test
    void update_changesAllThreeToggles() {
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(userPreferenceRepository.save(preference)).thenReturn(preference);

        UserPreference result = userPreferenceService.update(1L, false, false, true);

        assertThat(result.isNotifyOnLike()).isFalse();
        assertThat(result.isNotifyOnComment()).isFalse();
        assertThat(result.isNotifyOnFollow()).isTrue();
        verify(userPreferenceRepository).save(preference);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(userPreferenceRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPreferenceService.update(99L, true, true, true))
                .isInstanceOf(UserPreferenceNotFoundException.class);
    }
}
