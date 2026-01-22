package com.example.magister.dto;

import com.education.crm.entity.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
