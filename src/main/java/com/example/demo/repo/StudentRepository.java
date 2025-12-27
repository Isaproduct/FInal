package com.example.demo.repo;

import com.example.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentNo(String studentNo);
    Optional<Student> findByUserUsername(String username);
}
