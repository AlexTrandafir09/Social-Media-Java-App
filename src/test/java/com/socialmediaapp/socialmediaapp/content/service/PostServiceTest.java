package com.socialmediaapp.socialmediaapp.content.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.content.dto.PostCreateRequest;
import com.socialmediaapp.socialmediaapp.content.dto.PostImageInput;
import com.socialmediaapp.socialmediaapp.content.dto.PostUpdateRequest;
import com.socialmediaapp.socialmediaapp.content.entity.ImageFilter;
import com.socialmediaapp.socialmediaapp.content.entity.Post;
import com.socialmediaapp.socialmediaapp.content.exception.PostMustHaveImageException;
import com.socialmediaapp.socialmediaapp.content.exception.PostNotFoundException;
import com.socialmediaapp.socialmediaapp.content.repository.PostImageRepository;
import com.socialmediaapp.socialmediaapp.content.repository.PostRepository;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    private PostService postService;

    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, postImageRepository, userRepository, activityLogService);
        author = User.builder().id(1L).username("alice").build();
        post = Post.builder().id(1L).author(author).content("hello").build();
    }

    @Test
    void createPost_savesPostAndImages() {
        PostCreateRequest request = new PostCreateRequest(1L, "hello", List.of(new PostImageInput("a.png", ImageFilter.CONTRAST)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postImageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Post result = postService.createPost(request);

        assertThat(result.getContent()).isEqualTo("hello");
        assertThat(result.getImages()).hasSize(1);
        assertThat(result.getImages().get(0).getStorageKey()).isEqualTo("a.png");
        assertThat(result.getImages().get(0).getActiveFilter()).isEqualTo(ImageFilter.CONTRAST);
    }

    @Test
    void createPost_throwsWhenAuthorNotFound() {
        PostCreateRequest request = new PostCreateRequest(1L, "hello", List.of(new PostImageInput("a.png", null)));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(UserNotFoundException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_throwsWhenImagesEmpty() {
        PostCreateRequest request = new PostCreateRequest(1L, "hello", List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(PostMustHaveImageException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_throwsWhenImagesNull() {
        PostCreateRequest request = new PostCreateRequest(1L, "hello", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(PostMustHaveImageException.class);
    }

    @Test
    void getPostById_returnsPostWhenFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.getPostById(1L);

        assertThat(result).isEqualTo(post);
    }

    @Test
    void getPostById_throwsWhenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(99L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void getAllPosts_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(postRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(post), pageable, 1));

        Page<Post> result = postService.getAllPosts(pageable);

        assertThat(result.getContent()).containsExactly(post);
    }

    @Test
    void getPostsByAuthor_returnsList() {
        when(postRepository.findByAuthorId(1L)).thenReturn(List.of(post));

        List<Post> result = postService.getPostsByAuthor(1L);

        assertThat(result).containsExactly(post);
    }

    @Test
    void updatePost_updatesContent() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        Post result = postService.updatePost(1L, new PostUpdateRequest("updated"));

        assertThat(result.getContent()).isEqualTo("updated");
    }

    @Test
    void updatePost_throwsWhenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(99L, new PostUpdateRequest("x")))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void deletePost_deletesWhenExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository).deleteById(1L);
    }

    @Test
    void deletePost_throwsWhenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(99L))
                .isInstanceOf(PostNotFoundException.class);

        verify(postRepository, never()).deleteById(any());
    }
}
