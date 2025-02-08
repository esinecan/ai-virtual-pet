package com.cybercore.companion.service;

import com.cybercore.companion.dto.AuthResponse;
import com.cybercore.companion.dto.RegisterRequest;
import com.cybercore.companion.exception.AuthException;
import com.cybercore.companion.model.Coreling;
import com.cybercore.companion.model.UserAccount;
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

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        userAccount.setCoreling(createNewCoreling()); // Auto-create Coreling
        userRepository.save(userAccount);

        String token = jwtUtil.generateToken(userAccount.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse login(String username, String password) {
        UserAccount userAccount = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(password, userAccount.getPassword())) {
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
