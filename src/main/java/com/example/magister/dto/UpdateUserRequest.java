package com.example.magister.dto;

import com.example.magister.entity.UserRole;
import lombok.Data;

@Data
public class UpdateUserRequest {
      private String fullName;
      private String phone;
      private String email; // for admin and teacher updates
      private String password; // for admin and teacher updates
      private UserRole role; // for admin updates
}