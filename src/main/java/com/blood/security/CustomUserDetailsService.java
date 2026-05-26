package com.blood.security;

import lombok.extern.slf4j.Slf4j;

import com.blood.model.Users;
import com.blood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    //load thong tin user va chi lay email, pw, role
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Security Checkpoint: User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found");
                });

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        return new User(user.getEmail(), user.getPassword(), authorities);
    }
}
