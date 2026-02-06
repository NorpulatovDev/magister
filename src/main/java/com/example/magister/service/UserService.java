package com.example.magister.service;

import com.example.magister.dto.CreateUserRequest;
import com.example.magister.dto.UpdateUserRequest;
import com.example.magister.dto.UserDTO;
import com.example.magister.entity.User;
import com.example.magister.entity.UserRole;
import com.example.magister.exception.BusinessException;
import com.example.magister.exception.ResourceNotFoundException;
import com.example.magister.exception.UnauthorizedException;
import com.example.magister.repository.GroupStudentRepository;
import com.example.magister.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupStudentRepository groupStudentRepository;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToUserDTO(user);
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        log.info("User created: {}", user.getEmail());

        return mapToUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        return updateUser(userId, request, null);
    }

    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserRequest request, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // If currentUserId is provided, verify authorization
        if (currentUserId != null && !currentUserId.equals(userId)) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

            // Verify authorization based on role
            if (!canUpdateUser(currentUser, user)) {
                throw new UnauthorizedException("You don't have permission to update this user");
            }

            // Admins can update email, password, role, and active status
            if (currentUser.getRole() == UserRole.ADMIN) {
                if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                    // Check if new email already exists
                    if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BusinessException("Email already exists");
                    }
                    user.setEmail(request.getEmail());
                }

                if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                }

                if (request.getRole() != null) {
                    user.setRole(request.getRole());
                }

                if (request.getActive() != null) {
                    user.setActive(request.getActive());
                }
            }
        }

        // All users can update these fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        log.info("User updated: {} by user {}", userId, currentUserId);
        return mapToUserDTO(user);
    }

    /**
     * Check if currentUser can update targetUser
     * Rules:
     * - Admin can update anyone
     * - Teacher can update their own students
     * - Users can update themselves (handled by checking currentUserId == userId)
     */
    private boolean canUpdateUser(User currentUser, User targetUser) {
        // Admin can update anyone
        if (currentUser.getRole() == UserRole.ADMIN) {
            return true;
        }

        // Teacher can update students
        if (currentUser.getRole() == UserRole.TEACHER && targetUser.getRole() == UserRole.STUDENT) {
            // Check if student is enrolled in any of the teacher's groups
            return isStudentOfTeacher(currentUser.getId(), targetUser.getId());
        }

        // Otherwise, not authorized
        return false;
    }

    /**
     * Check if a student is enrolled in any of the teacher's groups
     */
    private boolean isStudentOfTeacher(Long teacherId, Long studentId) {
        // Get all groups taught by the teacher
        List<Long> teacherGroupIds = groupStudentRepository.findByStudentId(studentId)
                .stream()
                .filter(gs -> gs.getGroup().getTeacher().getId().equals(teacherId))
                .map(gs -> gs.getGroup().getId())
                .collect(Collectors.toList());

        return !teacherGroupIds.isEmpty();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Soft delete - just deactivate
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getEmail());
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        return dto;
    }
}