package com.redmath.lecture06.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class ApiUserController {
    private final ApiUserRepository userRepository;

    public ApiUserController(ApiUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<ApiUser> findAll(){
        return userRepository.findAll();
    }
}
