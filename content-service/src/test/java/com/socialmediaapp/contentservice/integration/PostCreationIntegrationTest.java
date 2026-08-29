package com.socialmediaapp.contentservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = TestJwtSupport.accessTokenFor(1L, "alice", "USER");
    }

    @Test
    void createPost_returnsCreatedPostWithNestedImagesAndNoProxyLeak() throws Exception {
        String body = """
                {"content": "hello world", "images": [{"storageKey": "a.png", "filter": "SEPIA"}]}
                """;

        MvcResult result = mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello world"))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.images[0].storageKey").value("a.png"))
                .andExpect(jsonPath("$.images[0].activeFilter").value("SEPIA"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("hibernateLazyInitializer", "handler");
    }

    @Test
    void createPost_rejectsWhenImagesEmpty() throws Exception {
        String body = """
                {"content": "hello world", "images": []}
                """;

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.images").exists());
    }
}
