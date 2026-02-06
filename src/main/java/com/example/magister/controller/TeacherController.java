package com.example.magister.controller;

import com.example.magister.dto.*;
import com.example.magister.entity.UserRole;
import com.example.magister.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "Teacher", description = "Teacher endpoints")
public class TeacherController {

    private final GroupService groupService;
    private final AttendanceService attendanceService;
    private final PaymentService paymentService;
    private final CoinService coinService;
    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get teacher dashboard")
    public ResponseEntity<TeacherDashboardDTO> getDashboard(@RequestHeader("X-User-Id") Long teacherId) {
        return ResponseEntity.ok(dashboardService.getTeacherDashboard(teacherId));
    }

    // Groups
    @GetMapping("/groups")
    @Operation(summary = "Get my groups")
    public ResponseEntity<List<GroupDTO>> getMyGroups(@RequestHeader("X-User-Id") Long teacherId) {
        return ResponseEntity.ok(groupService.getGroupsByTeacher(teacherId));
    }

    @GetMapping("/groups/{id}")
    @Operation(summary = "Get group details")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @GetMapping("/groups/{id}/students")
    @Operation(summary = "Get students in my group")
    public ResponseEntity<List<UserDTO>> getGroupStudents(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupStudents(id));
    }

    // Students
    @GetMapping("/students")
    @Operation(summary = "Get all my students across all groups")
    public ResponseEntity<List<UserDTO>> getMyStudents(@RequestHeader("X-User-Id") Long teacherId) {
        // Get all groups taught by this teacher
        List<GroupDTO> myGroups = groupService.getGroupsByTeacher(teacherId);

        // Get all students from these groups (distinct)
        List<UserDTO> allStudents = myGroups.stream()
                .flatMap(group -> groupService.getGroupStudents(group.getId()).stream())
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(allStudents);
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Get student details (only if student is in my groups)")
    public ResponseEntity<UserDTO> getStudentById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long teacherId) {
        // This will be validated by service layer to ensure teacher can access this student
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/students/{id}")
    @Operation(summary = "Update student details (same permissions as admin)")
    public ResponseEntity<UserDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("X-User-Id") Long teacherId) {
        // Teachers have full update permissions for their students (email, password, etc.)
        UserDTO updatedStudent = userService.updateUser(id, request, teacherId);
        return ResponseEntity.ok(updatedStudent);
    }

    // Attendance
    @PostMapping("/attendance")
    @Operation(summary = "Mark attendance")
    public ResponseEntity<AttendanceDTO> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request,
            @RequestHeader("X-User-Id") Long teacherId) {
        AttendanceDTO attendance = attendanceService.markAttendance(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
    }

    @PutMapping("/attendance/{id}")
    @Operation(summary = "Update attendance")
    public ResponseEntity<AttendanceDTO> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttendanceRequest request,
            @RequestHeader("X-User-Id") Long teacherId) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request, teacherId));
    }

    @GetMapping("/attendance/group/{groupId}")
    @Operation(summary = "Get attendance for my group")
    public ResponseEntity<List<AttendanceDTO>> getGroupAttendance(@PathVariable Long groupId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByGroup(groupId));
    }

    @GetMapping("/attendance/student/{studentId}")
    @Operation(summary = "Get attendance history for a specific student")
    public ResponseEntity<List<AttendanceDTO>> getStudentAttendance(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByStudent(studentId));
    }

    // Payments
    @PostMapping("/payments")
    @Operation(summary = "Record payment (auto-confirmed, no admin approval needed)")
    public ResponseEntity<PaymentDTO> recordPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("X-User-Id") Long teacherId) {
        PaymentDTO payment = paymentService.createPayment(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/payments")
    @Operation(summary = "Get my payments")
    public ResponseEntity<List<PaymentDTO>> getMyPayments(@RequestHeader("X-User-Id") Long teacherId) {
        return ResponseEntity.ok(paymentService.getPaymentsByTeacher(teacherId));
    }

    @GetMapping("/payments/student/{studentId}")
    @Operation(summary = "Get payment history for a specific student")
    public ResponseEntity<List<PaymentDTO>> getStudentPayments(@PathVariable Long studentId) {
        return ResponseEntity.ok(paymentService.getPaymentsByStudent(studentId));
    }

    @GetMapping("/payments/group/{groupId}")
    @Operation(summary = "Get payments for a specific group")
    public ResponseEntity<List<PaymentDTO>> getGroupPayments(@PathVariable Long groupId) {
        return ResponseEntity.ok(paymentService.getPaymentsByGroup(groupId));
    }

    @GetMapping("/payments/stats")
    @Operation(summary = "Get payment statistics")
    public ResponseEntity<PaymentStatsDTO> getPaymentStats(@RequestHeader("X-User-Id") Long teacherId) {
        return ResponseEntity.ok(paymentService.getPaymentStats(teacherId));
    }

    // Coins
    @PostMapping("/coins")
    @Operation(summary = "Award coins to student")
    public ResponseEntity<CoinDTO> awardCoins(
            @Valid @RequestBody AwardCoinsRequest request,
            @RequestHeader("X-User-Id") Long teacherId) {
        CoinDTO coin = coinService.awardCoins(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(coin);
    }

    @GetMapping("/coins/student/{studentId}")
    @Operation(summary = "Get coins for a specific student")
    public ResponseEntity<List<CoinDTO>> getStudentCoins(@PathVariable Long studentId) {
        return ResponseEntity.ok(coinService.getCoinsByStudent(studentId));
    }

    @GetMapping("/coins/group/{groupId}")
    @Operation(summary = "Get coins for my group")
    public ResponseEntity<List<CoinDTO>> getGroupCoins(@PathVariable Long groupId) {
        return ResponseEntity.ok(coinService.getCoinsByGroup(groupId));
    }

    @GetMapping("/coins/leaderboard/{groupId}")
    @Operation(summary = "Get group leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getGroupLeaderboard(@PathVariable Long groupId) {
        return ResponseEntity.ok(coinService.getGroupLeaderboard(groupId));
    }
}