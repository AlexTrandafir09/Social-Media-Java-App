package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.dto.PostCreateRequest;
import com.socialmediaapp.contentservice.content.dto.PostImageInput;
import com.socialmediaapp.contentservice.content.dto.PostUpdateRequest;
import com.socialmediaapp.contentservice.content.entity.ImageFilter;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.exception.PostMustHaveImageException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private ActivityEventPublisher activityEventPublisher;

    private PostService postService;

    private Post post;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, postImageRepository, activityEventPublisher);
        post = Post.builder().id(1L).authorId(1L).content("hello").build();
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
    void createPost_savesPostAndImages() {
        PostCreateRequest request = new PostCreateRequest("hello", List.of(new PostImageInput("a.png", "image/png", "dGVzdC1pbWFnZS1ieXRlcw==", ImageFilter.CONTRAST)));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postImageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Post result = postService.createPost(request);

        assertThat(result.getContent()).isEqualTo("hello");
        assertThat(result.getImages()).hasSize(1);
        assertThat(result.getImages().get(0).getStorageKey()).isEqualTo("a.png");
        assertThat(result.getImages().get(0).getActiveFilter()).isEqualTo(ImageFilter.CONTRAST);
    }

    @Test
    void createPost_throwsWhenImagesEmpty() {
        PostCreateRequest request = new PostCreateRequest("hello", List.of());

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(PostMustHaveImageException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_throwsWhenImagesNull() {
        PostCreateRequest request = new PostCreateRequest("hello", null);

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
    void getPostsByAuthors_returnsPageFilteredByAuthorIds() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Long> authorIds = List.of(1L, 2L);
        when(postRepository.findByAuthorIdIn(authorIds, pageable)).thenReturn(new PageImpl<>(List.of(post), pageable, 1));

        Page<Post> result = postService.getPostsByAuthors(authorIds, pageable);

        assertThat(result.getContent()).containsExactly(post);
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
    void updatePost_throwsWhenNotOwnerAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(1L, new PostUpdateRequest("x")))
                .isInstanceOf(AccessDeniedException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_succeedsWhenAdminButNotOwner() {
        authenticateAs(2L, "ROLE_ADMIN");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        Post result = postService.updatePost(1L, new PostUpdateRequest("updated"));

        assertThat(result.getContent()).isEqualTo("updated");
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

    @Test
    void deletePost_throwsWhenNotOwnerAndNotAdmin() {
        authenticateAs(2L, "ROLE_USER");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(postRepository, never()).deleteById(any());
    }

    @Test
    void deletePost_succeedsWhenAdminButNotOwner() {
        authenticateAs(2L, "ROLE_ADMIN");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository).deleteById(1L);
    }
}
