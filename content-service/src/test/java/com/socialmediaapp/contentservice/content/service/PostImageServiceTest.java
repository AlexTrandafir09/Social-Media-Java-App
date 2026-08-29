package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.dto.PostImageCreateRequest;
import com.socialmediaapp.contentservice.content.dto.PostImageUpdateRequest;
import com.socialmediaapp.contentservice.content.entity.ImageFilter;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.entity.PostImage;
import com.socialmediaapp.contentservice.content.exception.PostImageNotFoundException;
import com.socialmediaapp.contentservice.content.exception.PostNotFoundException;
import com.socialmediaapp.contentservice.content.repository.PostImageRepository;
import com.socialmediaapp.contentservice.content.repository.PostRepository;
import com.socialmediaapp.contentservice.messaging.ActivityEventPublisher;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostImageServiceTest {

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ActivityEventPublisher activityEventPublisher;

    private PostImageService postImageService;

    private Post post;
    private PostImage image;

    @BeforeEach
    void setUp() {
        postImageService = new PostImageService(postImageRepository, postRepository, activityEventPublisher);
        post = Post.builder().id(1L).authorId(1L).content("hi").build();
        image = PostImage.builder().id(1L).post(post).storageKey("a.png").activeFilter(ImageFilter.NONE).build();
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
    void addImage_savesWhenPostExists() {
        PostImageCreateRequest request = new PostImageCreateRequest("a.png", ImageFilter.CONTRAST);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageRepository.save(any(PostImage.class))).thenReturn(image);

        PostImage result = postImageService.addImage(1L, request);

        assertThat(result.getPost()).isEqualTo(post);
    }

    @Test
    void addImage_defaultsToNoneFilterWhenNull() {
        PostImageCreateRequest request = new PostImageCreateRequest("a.png", null);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageRepository.save(any(PostImage.class))).thenAnswer(inv -> inv.getArgument(0));

        PostImage result = postImageService.addImage(1L, request);

        assertThat(result.getActiveFilter()).isEqualTo(ImageFilter.NONE);
    }

    @Test
    void addImage_throwsWhenPostNotFound() {
        PostImageCreateRequest request = new PostImageCreateRequest("a.png", null);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postImageService.addImage(1L, request))
                .isInstanceOf(PostNotFoundException.class);

        verify(postImageRepository, never()).save(any());
    }

    @Test
    void addImage_throwsWhenNotOwnerAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");
        PostImageCreateRequest request = new PostImageCreateRequest("a.png", null);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postImageService.addImage(1L, request))
                .isInstanceOf(AccessDeniedException.class);

        verify(postImageRepository, never()).save(any());
    }

    @Test
    void addImage_succeedsWhenAdminButNotOwner() {
        authenticateAs(2L, "ROLE_ADMIN");
        PostImageCreateRequest request = new PostImageCreateRequest("a.png", ImageFilter.CONTRAST);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postImageRepository.save(any(PostImage.class))).thenReturn(image);

        PostImage result = postImageService.addImage(1L, request);

        assertThat(result.getPost()).isEqualTo(post);
    }

    @Test
    void getImageById_returnsImageWhenFound() {
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));

        PostImage result = postImageService.getImageById(1L, 1L);

        assertThat(result).isEqualTo(image);
    }

    @Test
    void getImageById_throwsWhenNotFound() {
        when(postImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postImageService.getImageById(1L, 99L))
                .isInstanceOf(PostImageNotFoundException.class);
    }

    @Test
    void getImageById_throwsWhenImageBelongsToDifferentPost() {
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> postImageService.getImageById(99L, 1L))
                .isInstanceOf(PostImageNotFoundException.class);
    }

    @Test
    void getImagesForPost_returnsList() {
        when(postImageRepository.findByPostId(1L)).thenReturn(List.of(image));

        List<PostImage> result = postImageService.getImagesForPost(1L);

        assertThat(result).containsExactly(image);
    }

    @Test
    void updateFilter_changesFilter() {
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(postImageRepository.save(image)).thenReturn(image);

        PostImage result = postImageService.updateFilter(1L, 1L, new PostImageUpdateRequest(ImageFilter.VINTAGE));

        assertThat(result.getActiveFilter()).isEqualTo(ImageFilter.VINTAGE);
    }

    @Test
    void updateFilter_throwsWhenNotFound() {
        when(postImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postImageService.updateFilter(1L, 99L, new PostImageUpdateRequest(ImageFilter.SEPIA)))
                .isInstanceOf(PostImageNotFoundException.class);
    }

    @Test
    void updateFilter_throwsWhenNotOwnerAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> postImageService.updateFilter(1L, 1L, new PostImageUpdateRequest(ImageFilter.SEPIA)))
                .isInstanceOf(AccessDeniedException.class);

        verify(postImageRepository, never()).save(any());
    }

    @Test
    void updateFilter_succeedsWhenAdminButNotOwner() {
        authenticateAs(2L, "ROLE_ADMIN");
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(postImageRepository.save(image)).thenReturn(image);

        PostImage result = postImageService.updateFilter(1L, 1L, new PostImageUpdateRequest(ImageFilter.VINTAGE));

        assertThat(result.getActiveFilter()).isEqualTo(ImageFilter.VINTAGE);
    }

    @Test
    void deleteImage_deletesWhenExists() {
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));

        postImageService.deleteImage(1L, 1L);

        verify(postImageRepository).deleteById(1L);
    }

    @Test
    void deleteImage_throwsWhenNotFound() {
        when(postImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postImageService.deleteImage(1L, 99L))
                .isInstanceOf(PostImageNotFoundException.class);

        verify(postImageRepository, never()).deleteById(any());
    }

    @Test
    void deleteImage_throwsWhenNotOwnerAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> postImageService.deleteImage(1L, 1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(postImageRepository, never()).deleteById(any());
    }

    @Test
    void deleteImage_succeedsWhenAdminButNotOwner() {
        authenticateAs(2L, "ROLE_ADMIN");
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));

        postImageService.deleteImage(1L, 1L);

        verify(postImageRepository).deleteById(1L);
    }
}
