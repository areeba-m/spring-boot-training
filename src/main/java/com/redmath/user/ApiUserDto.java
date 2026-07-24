package com.redmath.user;

import lombok.Data;

@Data
public class ApiUserDto {
    private String username;
    private String password;
    private String role;
}
