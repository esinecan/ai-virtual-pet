package com.cybercore.companion.service;

import com.cybercore.companion.dto.AuthResponse;
import com.cybercore.companion.dto.RegisterRequest;
import com.cybercore.companion.exception.AuthException;
import com.cybercore.companion.model.Coreling;
import com.cybercore.companion.model.User;
import com.cybercore.companion.repository.UserRepository;
import com.cybercore.companion.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCoreling(createNewCoreling()); // Auto-create Coreling
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(username);
        return new AuthResponse(token);
    }

    private Coreling createNewCoreling() {
        Coreling coreling = new Coreling();
        coreling.setDataIntegrity(100);
        coreling.setProcessingLoad(50);
        coreling.setEmotionalCharge(50);
        return coreling;
    }
}
