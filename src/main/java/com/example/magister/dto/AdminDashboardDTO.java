package com.example.magister.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminDashboardDTO {
      private Integer totalUsers;
      private Integer totalGroups;
      private Integer activeGroups;
      private Integer totalStudents;
      private Integer totalTeachers;
      private PaymentStatsDTO paymentStats;
      private List<UserDTO> recentUsers;
}