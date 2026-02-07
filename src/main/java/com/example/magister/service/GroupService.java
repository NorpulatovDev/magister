package com.example.magister.service;

import com.example.magister.dto.CreateGroupRequest;
import com.example.magister.dto.GroupDTO;
import com.example.magister.dto.UpdateGroupRequest;
import com.example.magister.dto.UserDTO;
import com.example.magister.entity.EnrollmentStatus;
import com.example.magister.entity.Group;
import com.example.magister.entity.GroupStatus;
import com.example.magister.entity.GroupStudent;
import com.example.magister.entity.User;
import com.example.magister.entity.UserRole;
import com.example.magister.exception.BusinessException;
import com.example.magister.exception.ResourceNotFoundException;
import com.example.magister.exception.UnauthorizedException;
import com.example.magister.repository.GroupRepository;
import com.example.magister.repository.GroupStudentRepository;
import com.example.magister.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupDTO createGroup(CreateGroupRequest request) {
        return createGroup(request, null);
    }

    @Transactional
    public GroupDTO createGroup(CreateGroupRequest request, Long currentUserId) {
        log.info("Creating group: {}", request.getName());

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new BusinessException("User is not a teacher");
        }

        // If currentUserId is provided, verify authorization
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

            // Teachers can only create groups for themselves
            if (currentUser.getRole() == UserRole.TEACHER &&
                    !currentUser.getId().equals(request.getTeacherId())) {
                throw new UnauthorizedException("Teachers can only create groups for themselves");
            }
            // Admins can create groups for any teacher
        }

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .teacher(teacher)
                .schedule(request.getSchedule())
                .status(GroupStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .students(new HashSet<>())
                .build();

        group = groupRepository.save(group);
        log.info("Group created: {}", group.getName());

        return mapToGroupDTO(group);
    }

    @Transactional
    public GroupDTO updateGroup(Long groupId, UpdateGroupRequest request) {
        return updateGroup(groupId, request, null);
    }

    @Transactional
    public GroupDTO updateGroup(Long groupId, UpdateGroupRequest request, Long currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // If currentUserId is provided, verify authorization
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

            // Teachers can only update their own groups
            if (currentUser.getRole() == UserRole.TEACHER &&
                    !group.getTeacher().getId().equals(currentUserId)) {
                throw new UnauthorizedException("You can only update your own groups");
            }
        }

        if (request.getName() != null) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getSchedule() != null) {
            group.setSchedule(request.getSchedule());
        }
        if (request.getStatus() != null) {
            group.setStatus(request.getStatus());
        }

        group = groupRepository.save(group);
        return mapToGroupDTO(group);
    }

    @Transactional
    public void enrollStudent(Long groupId, Long studentId) {
        enrollStudent(groupId, studentId, null);
    }

    @Transactional
    public void enrollStudent(Long groupId, Long studentId, Long currentUserId) {
        log.info("Enrolling student {} in group {}", studentId, groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessException("User is not a student");
        }

        // If currentUserId is provided, verify authorization
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

            // Teachers can only enroll students in their own groups
            if (currentUser.getRole() == UserRole.TEACHER &&
                    !group.getTeacher().getId().equals(currentUserId)) {
                throw new UnauthorizedException("You can only enroll students in your own groups");
            }
        }

        if (groupStudentRepository.existsByGroupIdAndStudentId(groupId, studentId)) {
            throw new BusinessException("Student is already enrolled in this group");
        }

        GroupStudent enrollment = GroupStudent.builder()
                .group(group)
                .student(student)
                .enrolledAt(LocalDateTime.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();

        groupStudentRepository.save(enrollment);
        log.info("Student enrolled successfully");
    }

    @Transactional
    public void removeStudent(Long groupId, Long studentId) {
        removeStudent(groupId, studentId, null);
    }

    @Transactional
    public void removeStudent(Long groupId, Long studentId, Long currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // If currentUserId is provided, verify authorization
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

            // Teachers can only remove students from their own groups
            if (currentUser.getRole() == UserRole.TEACHER &&
                    !group.getTeacher().getId().equals(currentUserId)) {
                throw new UnauthorizedException("You can only remove students from your own groups");
            }
        }

        GroupStudent enrollment = groupStudentRepository.findByGroupIdAndStudentId(groupId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "groupId-studentId",
                        groupId + "-" + studentId));

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.setCompletedAt(LocalDateTime.now());
        groupStudentRepository.save(enrollment);

        log.info("Student {} removed from group {}", studentId, groupId);
    }

    @Transactional(readOnly = true)
    public List<GroupDTO> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::mapToGroupDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupDTO getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        return mapToGroupDTO(group);
    }

    @Transactional(readOnly = true)
    public List<GroupDTO> getGroupsByTeacher(Long teacherId) {
        return groupRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToGroupDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupDTO> getGroupsByStudent(Long studentId) {
        return groupStudentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(gs -> mapToGroupDTO(gs.getGroup()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getGroupStudents(Long groupId) {
        return groupStudentRepository.findByGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(gs -> {
                    User student = gs.getStudent();
                    UserDTO dto = new UserDTO();
                    dto.setId(student.getId());
                    dto.setEmail(student.getEmail());
                    dto.setFullName(student.getFullName());
                    dto.setPhone(student.getPhone());
                    dto.setRole(student.getRole());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * OPTIMIZATSIYA QILINGAN: Group entity ni DTO ga map qilish
     * Avval: group.getStudents() ishlatib, Lazy Loading tufayli 0 qaytardi
     * Hozir: GroupStudentRepository dan to'g'ridan-to'g'ri count oladi
     */
    private GroupDTO mapToGroupDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setTeacherId(group.getTeacher().getId());
        dto.setTeacherName(group.getTeacher().getFullName());
        dto.setSchedule(group.getSchedule());
        dto.setStatus(group.getStatus());

        // YECHIM: Repository dan to'g'ridan-to'g'ri count olamiz
        // Bu Lazy Loading muammosini hal qiladi va performance yaxshiroq
        Integer studentCount = groupStudentRepository.countByGroupIdAndStatus(
                group.getId(), 
                EnrollmentStatus.ACTIVE
        );
        dto.setStudentCount(studentCount != null ? studentCount : 0);

        dto.setCreatedAt(group.getCreatedAt());
        return dto;
    }
}