package com.redmath.lecture06.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiUserService implements UserDetailsService {

    private ApiUserRepository userRepository;

    public ApiUserService(ApiUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ApiUser> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return User.withUsername(username)
                .password(user.get().getPassword())
                .roles(user.get().getRole().split(","))
                .build();
    }
}
