package com.redmath.lecture06.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class ApiUser {
    @Id
    private Long userId;
    private String username;
    private String password;
    private String role;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
