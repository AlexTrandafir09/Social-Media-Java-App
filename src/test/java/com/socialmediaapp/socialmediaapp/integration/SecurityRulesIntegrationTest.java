package com.socialmediaapp.socialmediaapp.integration;

import com.socialmediaapp.socialmediaapp.user.entity.Role;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"email\":\"%s@test.com\",\"password\":\"pass1234\"}"
                        .formatted(username, username)));

        return login(username);
    }

    private String login(String username) throws Exception {
        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"pass1234\"}".formatted(username)))
                .andReturn().getResponse().getContentAsString();

        return loginBody.split("\"accessToken\":\"")[1].split("\"")[0];
    }

    @Test
    void protectedEndpoint_rejectsRequestWithNoToken() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePost_forbiddenForNonOwner() throws Exception {
        String aliceToken = registerAndLogin("alice");
        String bobToken = registerAndLogin("bob");

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
        String aliceToken = registerAndLogin("alice");
        String bobToken = registerAndLogin("bob");

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

    @Test
    void activityLog_forbiddenForNonAdmin() throws Exception {
        String bobToken = registerAndLogin("bob");

        mockMvc.perform(get("/api/activity")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void activityLog_allowedForAdmin() throws Exception {
        registerAndLogin("alice");
        User alice = userRepository.findByUsername("alice").orElseThrow();
        alice.setRole(Role.ADMIN);
        userRepository.save(alice);
        String adminToken = login("alice");

        mockMvc.perform(get("/api/activity")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
