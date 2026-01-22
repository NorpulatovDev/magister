package com.example.magister.dto;

@Data
public class CreateUserRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private UserRole role;
}