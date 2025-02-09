package com.cybercore.companion.security;

import com.cybercore.companion.dto.AuthRequest;
import com.cybercore.companion.dto.AuthResponse;
import com.cybercore.companion.dto.RegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "Testpass123!";
    private String validToken;

    @BeforeEach
    void setup() {
        // Create test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setPassword(TEST_PASSWORD);
        
        restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);
        
        // Get valid token
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(TEST_USERNAME);
        authRequest.setPassword(TEST_PASSWORD);
        
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/api/auth/login", 
            authRequest, 
            AuthResponse.class
        );
        
        AuthResponse authResponse = response.getBody();
        if (authResponse != null && authResponse.getToken() != null) {
            validToken = authResponse.getToken();
        }
    }

    @AfterEach
    void cleanup() {
        // Cleanup test user (implementation depends on my user deletion setup)
        // Typically I'd need to add a delete endpoint for testing purposes
    }

    @Test
    void whenAccessProtectedEndpointWithoutToken_thenUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/coreling/1", 
            String.class
        );
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void whenAccessWithValidToken_thenAuthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/coreling/1",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void whenAccessWithInvalidToken_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.token.here");
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/coreling/1",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
