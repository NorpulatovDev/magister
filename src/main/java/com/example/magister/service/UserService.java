package com.example.magister.service;

import com.example.magister.dto.CreateUserRequest;
import com.example.magister.dto.UpdateUserRequest;
import com.example.magister.dto.UserDTO;
import com.example.magister.entity.*;
import com.example.magister.exception.BusinessException;
import com.example.magister.exception.ResourceNotFoundException;
import com.example.magister.exception.UnauthorizedException;
import com.example.magister.repository.*;
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
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;
    private final CoinRepository coinRepository;
    private final GroupRepository groupRepository;

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

    /**
     * YANGI: Guruhsiz (orphaned) o'quvchilarni topish
     * Bu o'quvchilar yaratilgan, lekin hech qaysi guruhga qo'shilmagan
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getOrphanedStudents() {
        log.info("Fetching orphaned students (students not enrolled in any active group)");
        
        List<User> allStudents = userRepository.findByRole(UserRole.STUDENT);
        
        return allStudents.stream()
                .filter(student -> {
                    // Check if student has any ACTIVE enrollments
                    return groupStudentRepository.findByStudentIdAndStatus(
                            student.getId(), 
                            EnrollmentStatus.ACTIVE
                    ).isEmpty(); // True if no active enrollments
                })
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

            // Admins can update everything
            if (currentUser.getRole() == UserRole.ADMIN) {
                updateAllFields(user, request);
            }
            // Teachers can update everything for their students (same as admin)
            else if (currentUser.getRole() == UserRole.TEACHER && user.getRole() == UserRole.STUDENT) {
                if (isStudentOfTeacher(currentUser.getId(), user.getId())) {
                    updateAllFields(user, request);
                } else {
                    throw new UnauthorizedException("You can only update your own students");
                }
            }
        } else {
            // User updating themselves - only basic fields
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
        }

        user = userRepository.save(user);
        log.info("User updated: {} by user {}", userId, currentUserId);
        return mapToUserDTO(user);
    }

    private void updateAllFields(User user, UpdateUserRequest request) {
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
    }

    private boolean canUpdateUser(User currentUser, User targetUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return true;
        }

        if (currentUser.getRole() == UserRole.TEACHER && targetUser.getRole() == UserRole.STUDENT) {
            return isStudentOfTeacher(currentUser.getId(), targetUser.getId());
        }

        return false;
    }

    private boolean isStudentOfTeacher(Long teacherId, Long studentId) {
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

        cascadeDeleteUser(user);
        log.info("User deleted: {}", user.getEmail());
    }

    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        if (currentUser.getRole() == UserRole.ADMIN) {
            cascadeDeleteUser(user);
        } else if (currentUser.getRole() == UserRole.TEACHER && user.getRole() == UserRole.STUDENT) {
            if (!isStudentOfTeacher(currentUserId, userId)) {
                throw new UnauthorizedException("You can only delete your own students");
            }
            cascadeDeleteUser(user);
        } else {
            throw new UnauthorizedException("You don't have permission to delete this user");
        }

        log.info("User {} deleted by user {}", userId, currentUserId);
    }

    private void cascadeDeleteUser(User user) {
        Long userId = user.getId();

        // Delete records where user is a student
        paymentRepository.deleteByStudentId(userId);
        attendanceRepository.deleteByStudentId(userId);
        coinRepository.deleteByStudentId(userId);
        groupStudentRepository.deleteByStudentId(userId);

        // Delete records where user is a teacher
        if (user.getRole() == UserRole.TEACHER) {
            paymentRepository.deleteByTeacherId(userId);
            attendanceRepository.deleteByMarkedById(userId);
            coinRepository.deleteByTeacherId(userId);

            // Delete groups owned by this teacher (cascades to group_students via entity)
            List<Group> teacherGroups = groupRepository.findByTeacherId(userId);
            groupRepository.deleteAll(teacherGroups);
        }

        userRepository.delete(user);
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        return dto;
    }
}