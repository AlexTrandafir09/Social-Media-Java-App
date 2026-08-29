package com.socialmediaapp.socialmediaapp.content.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.content.dto.PostImageCreateRequest;
import com.socialmediaapp.socialmediaapp.content.dto.PostImageUpdateRequest;
import com.socialmediaapp.socialmediaapp.content.entity.ImageFilter;
import com.socialmediaapp.socialmediaapp.content.entity.Post;
import com.socialmediaapp.socialmediaapp.content.entity.PostImage;
import com.socialmediaapp.socialmediaapp.content.exception.PostImageNotFoundException;
import com.socialmediaapp.socialmediaapp.content.exception.PostNotFoundException;
import com.socialmediaapp.socialmediaapp.content.repository.PostImageRepository;
import com.socialmediaapp.socialmediaapp.content.repository.PostRepository;
import com.socialmediaapp.socialmediaapp.user.entity.User;
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
class PostImageServiceTest {

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ActivityLogService activityLogService;

    private PostImageService postImageService;

    private Post post;
    private PostImage image;

    @BeforeEach
    void setUp() {
        postImageService = new PostImageService(postImageRepository, postRepository, activityLogService);
        User author = User.builder().id(1L).username("alice").build();
        post = Post.builder().id(1L).author(author).content("hi").build();
        image = PostImage.builder().id(1L).post(post).storageKey("a.png").activeFilter(ImageFilter.NONE).build();
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
}
