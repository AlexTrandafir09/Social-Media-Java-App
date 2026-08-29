package com.socialmediaapp.socialmediaapp.integration;

import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private Long authorId;

    @BeforeEach
    void setUp() {
        User author = userRepository.save(User.builder()
                .username("alice")
                .email("alice@test.com")
                .password("pass1234")
                .build());
        authorId = author.getId();
    }

    @Test
    void createPost_returnsCreatedPostWithNestedImagesAndNoProxyLeak() throws Exception {
        String body = """
                {"authorId": %d, "content": "hello world", "images": [{"storageKey": "a.png", "filter": "SEPIA"}]}
                """.formatted(authorId);

        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello world"))
                .andExpect(jsonPath("$.author.username").value("alice"))
                .andExpect(jsonPath("$.images[0].storageKey").value("a.png"))
                .andExpect(jsonPath("$.images[0].activeFilter").value("SEPIA"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("hibernateLazyInitializer", "handler");
    }

    @Test
    void createPost_rejectsWhenImagesEmpty() throws Exception {
        String body = """
                {"authorId": %d, "content": "hello world", "images": []}
                """.formatted(authorId);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.images").exists());
    }

    @Test
    void createPost_returnsNotFoundWhenAuthorMissing() throws Exception {
        String body = """
                {"authorId": 999999, "content": "hello world", "images": [{"storageKey": "a.png"}]}
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999999"));
    }
}
