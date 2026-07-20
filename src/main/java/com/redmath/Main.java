package com.redmath;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @PostConstruct
    public void init() {
        System.out.println("DEBUG - Loaded Google Client ID: " + clientId);
    }

    static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
