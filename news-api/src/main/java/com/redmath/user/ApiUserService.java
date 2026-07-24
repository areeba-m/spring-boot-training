package com.redmath.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiUserService implements UserDetailsService {

    private final ApiUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiUserService(ApiUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApiUser user = findUser(username);
        return User.withUsername(username)
                .password(user.getPassword())
                .roles(user.getRole().split(","))
                .build();
    }

    public ApiUser loginAndGenerateToken(String username) {
        ApiUser user = findUser(username);
        return generateUuidToken(user);
    }

    public ApiUser registerAndGenerateToken(String username) {
        Optional<ApiUser> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            user = Optional.of(registerOAuthUser(username));
        }
        return generateUuidToken(user.get());
    }

    private ApiUser registerOAuthUser(String username) {
        ApiUser newUser = new ApiUser();
        newUser.setUserId(System.currentTimeMillis());
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole("REPORTER");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(newUser);
    }

    private ApiUser generateUuidToken(ApiUser user) {
        user.setToken(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    private ApiUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
