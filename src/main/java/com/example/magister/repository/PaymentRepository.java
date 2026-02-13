package com.example.magister.repository;

import com.example.magister.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);

    List<Payment> findByTeacherId(Long teacherId);

    List<Payment> findByGroupId(Long groupId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.student.id = :studentId")
    Double getTotalPaymentsByStudent(Long studentId);

    void deleteByStudentId(Long studentId);

    void deleteByTeacherId(Long teacherId);
}