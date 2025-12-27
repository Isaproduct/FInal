package com.example.demo.controller;

import com.example.demo.dto.grade.GradeRequest;
import com.example.demo.dto.grade.GradeResponse;
import com.example.demo.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    // ✅ Все оценки (только ADMIN/TEACHER)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<GradeResponse> all() {
        return gradeService.findAll();
    }

    // ✅ Оценки конкретного студента (только ADMIN/TEACHER)
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<GradeResponse> byStudent(@PathVariable Long studentId) {
        return gradeService.findByStudent(studentId);
    }

    // ✅ Мои оценки (только STUDENT)
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<GradeResponse> my(Authentication authentication) {
        return gradeService.findMy(authentication.getName());
    }

    // ✅ Поставить оценку (ADMIN/TEACHER)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public GradeResponse create(@Valid @RequestBody GradeRequest request) {
        return gradeService.create(request);
    }

    // ✅ Удалить оценку (только ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        gradeService.delete(id);
    }
}

