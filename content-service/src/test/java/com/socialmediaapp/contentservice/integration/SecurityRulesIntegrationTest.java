package com.socialmediaapp.contentservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpoint_rejectsRequestWithNoToken() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePost_forbiddenForNonOwner() throws Exception {
        String aliceToken = TestJwtSupport.accessTokenFor(1L, "alice", "USER");
        String bobToken = TestJwtSupport.accessTokenFor(2L, "bob", "USER");

        String postBody = mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"alice's post\",\"images\":[{\"storageKey\":\"a.png\"}]}"))
                .andReturn().getResponse().getContentAsString();
        Long postId = Long.valueOf(postBody.split("\"id\":")[1].split(",")[0]);

        mockMvc.perform(put("/api/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hacked\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"edited by owner\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("edited by owner"));
    }

    @Test
    void deletePost_forbiddenForNonOwner() throws Exception {
        String aliceToken = TestJwtSupport.accessTokenFor(1L, "alice", "USER");
        String bobToken = TestJwtSupport.accessTokenFor(2L, "bob", "USER");

        String postBody = mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"alice's post\",\"images\":[{\"storageKey\":\"a.png\"}]}"))
                .andReturn().getResponse().getContentAsString();
        Long postId = Long.valueOf(postBody.split("\"id\":")[1].split(",")[0]);

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isForbidden());
    }
}
