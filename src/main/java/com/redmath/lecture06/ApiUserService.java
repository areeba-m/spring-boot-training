package com.redmath.lecture06;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApiUserService implements UserDetailsService {

    private ApiUserRepository userRepository;

    public ApiUserService(ApiUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApiUser user = findUser(username);
        return User.withUsername(username)
                .password(user.getPassword())
                .roles(user.getRole().split(","))
                .build();
    }

    public ApiUser generateToken(String username) {
        ApiUser user = findUser(username);
        return generateToken(user);
    }

    private ApiUser generateToken(ApiUser user) {
        user.setToken(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    private ApiUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
