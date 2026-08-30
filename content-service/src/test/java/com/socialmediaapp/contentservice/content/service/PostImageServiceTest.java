package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.entity.ImageFilter;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.entity.PostImage;
import com.socialmediaapp.contentservice.content.exception.PostImageNotFoundException;
import com.socialmediaapp.contentservice.content.repository.PostImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostImageServiceTest {

    @Mock
    private PostImageRepository postImageRepository;

    private PostImageService postImageService;

    private PostImage image;

    @BeforeEach
    void setUp() {
        postImageService = new PostImageService(postImageRepository);
        Post post = Post.builder().id(1L).authorId(1L).content("hi").build();
        image = PostImage.builder().id(1L).post(post).storageKey("a.png").activeFilter(ImageFilter.NONE).build();
    }

    @Test
    void getImageById_returnsImageWhenFound() {
        when(postImageRepository.findById(1L)).thenReturn(Optional.of(image));

        PostImage result = postImageService.getImageById(1L);

        assertThat(result).isEqualTo(image);
    }

    @Test
    void getImageById_throwsWhenNotFound() {
        when(postImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postImageService.getImageById(99L))
                .isInstanceOf(PostImageNotFoundException.class);
    }
}
