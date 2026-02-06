package com.example.magister.dto;

import com.example.magister.entity.UserRole;
import lombok.Data;

@Data
public class UpdateUserRequest {
      private String fullName;
      private String phone;
      private String email; // for admin updates
      private String password; // for admin updates
      private UserRole role; // for admin updates
      private Boolean active; // for admin updates
}