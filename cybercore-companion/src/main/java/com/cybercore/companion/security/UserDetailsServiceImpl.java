package com.cybercore.companion.security;

import com.cybercore.companion.model.UserAccount;
import com.cybercore.companion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userRepository.findByUsername(username)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("User not found with username: " + username)
                );

        return org.springframework.security.core.userdetails.User.builder()
                .username(userAccount.getUsername())
                .password(userAccount.getPassword())
                .authorities(new ArrayList<>()) // Add roles if needed
                .build();
    }
}
