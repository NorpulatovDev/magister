package com.example.magister.controller;

import com.example.magister.dto.CreateUserRequest;
import com.example.magister.dto.UpdateUserRequest;
import com.example.magister.dto.UserDTO;
import com.example.magister.entity.UserRole;
import com.example.magister.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping("/students")
    @Operation(summary = "Create new student (Teachers can create students for their groups)")
    public ResponseEntity<UserDTO> createStudent(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        
        // Force role to STUDENT
        request.setRole(UserRole.STUDENT);
        
        // Check if user is teacher or admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @Operation(summary = "Get all users (Admin and Teacher)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile (Admin can update anyone, Teacher can update their students)")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        Long currentUserId = (Long) authentication.getPrincipal();
        UserDTO user = userService.updateUser(id, request, currentUserId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/students")
    @Operation(summary = "Get all students")
    public ResponseEntity<List<UserDTO>> getAllStudents() {
        return ResponseEntity.ok(userService.getUsersByRole(UserRole.STUDENT));
    }

    @GetMapping("/teachers")
    @Operation(summary = "Get all teachers")
    public ResponseEntity<List<UserDTO>> getAllTeachers() {
        return ResponseEntity.ok(userService.getUsersByRole(UserRole.TEACHER));
    }
}