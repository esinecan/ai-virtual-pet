package com.cybercore.companion.service;

import com.cybercore.companion.dto.RegisterRequest;
import com.cybercore.companion.exception.AuthException;
import com.cybercore.companion.model.User;
import com.cybercore.companion.repository.UserRepository;
import com.cybercore.companion.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password");

        assertDoesNotThrow(() -> authService.register(request));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_InvalidCredentials() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        
        assertThrows(AuthException.class, () -> 
            authService.login("user", "wrongpass")
        );
    }
}
