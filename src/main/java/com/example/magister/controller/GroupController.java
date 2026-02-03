package com.example.magister.controller;

import com.example.magister.dto.*;
import com.example.magister.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Group management endpoints (accessible by teachers and admins)")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Create group (Teachers can create groups for themselves, admins for any teacher)")
    public ResponseEntity<GroupDTO> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        GroupDTO group = groupService.createGroup(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update group (Teachers can update only their groups)")
    public ResponseEntity<GroupDTO> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(groupService.updateGroup(id, request, currentUserId));
    }

    @PostMapping("/{groupId}/enroll/{studentId}")
    @Operation(summary = "Enroll student in group (Teachers can enroll in their own groups)")
    public ResponseEntity<Void> enrollStudent(
            @PathVariable Long groupId,
            @PathVariable Long studentId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.enrollStudent(groupId, studentId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/students/{studentId}")
    @Operation(summary = "Remove student from group (Teachers can remove from their own groups)")
    public ResponseEntity<Void> removeStudent(
            @PathVariable Long groupId,
            @PathVariable Long studentId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.removeStudent(groupId, studentId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all groups")
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "Get group students")
    public ResponseEntity<List<UserDTO>> getGroupStudents(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupStudents(id));
    }
}