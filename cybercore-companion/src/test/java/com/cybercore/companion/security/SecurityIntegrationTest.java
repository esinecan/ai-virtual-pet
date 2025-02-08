package com.cybercore.companion.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessProtectedEndpointWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/coreling/1"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAccessWithValidToken_thenAuthorized() throws Exception {
        String token = "your-generated-jwt-token";
        
        mockMvc.perform(get("/api/coreling/1")
               .header("Authorization", "Bearer " + token))
               .andExpect(status().isOk());
    }
}
