package com.socialmediaapp.socialmediaapp.integration;

import com.socialmediaapp.socialmediaapp.content.entity.Post;
import com.socialmediaapp.socialmediaapp.content.repository.PostRepository;
import com.socialmediaapp.socialmediaapp.security.service.JwtService;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostPaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtService jwtService;

    private String accessToken;

    @BeforeEach
    void setUp() {
        User author = userRepository.save(User.builder()
                .username("alice")
                .email("alice@test.com")
                .password("pass1234")
                .build());
        accessToken = jwtService.generateAccessToken(author);

        for (char c = 'A'; c <= 'E'; c++) {
            postRepository.save(Post.builder()
                    .author(author)
                    .content("Post " + c)
                    .build());
        }
    }

    @Test
    void getAllPosts_returnsFirstPageSortedByContent() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "content,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value("Post A"))
                .andExpect(jsonPath("$.content[1].content").value("Post B"));
    }

    @Test
    void getAllPosts_returnsSecondPageSortedByContent() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "content,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Post C"))
                .andExpect(jsonPath("$.content[1].content").value("Post D"));
    }
}
