package com.example.magister.dto;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private UserRole role;
}
