package com.example.demo.service;

import com.example.demo.dto.admin.AdminCreateUserRequest;
import com.example.demo.dto.admin.AdminUserResponse;
import com.example.demo.dto.admin.UpdateEnabledRequest;
import com.example.demo.dto.admin.UpdateRoleRequest;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repo.StudentRepository;
import com.example.demo.repo.TeacherRepository;
import com.example.demo.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();

        User saved = userRepository.save(user);

        // если админ создает пользователя сразу STUDENT/TEACHER — создаем профиль
        ensureProfilesExist(saved);

        return toResponse(saved);
    }

    public AdminUserResponse findById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toResponse(u);
    }

    @Transactional
    public AdminUserResponse updateRole(Long id, UpdateRoleRequest request) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        u.setRole(request.getRole());

        User saved = userRepository.save(u);

        // ВАЖНО: при смене роли на STUDENT/TEACHER — автоматически создаем запись
        ensureProfilesExist(saved);

        return toResponse(saved);
    }

    private void ensureProfilesExist(User u) {
        if (u.getRole() == Role.STUDENT) {
            studentRepository.findByUserUsername(u.getUsername())
                    .orElseGet(() -> studentRepository.save(
                            Student.builder()
                                    .user(u)
                                    .fullName(u.getFullName())
                                    .studentNo(generateUniqueStudentNo(u))
                                    .build()
                    ));
        }

        if (u.getRole() == Role.TEACHER) {
            teacherRepository.findByUserUsername(u.getUsername())
                    .orElseGet(() -> teacherRepository.save(
                            Teacher.builder()
                                    .user(u)
                                    .fullName(u.getFullName())
                                    .department("General")
                                    .build()
                    ));
        }
    }

    private String generateUniqueStudentNo(User u) {
        String base = "S-" + String.format("%08d", u.getId()); // пример: S-00000012
        if (!studentRepository.existsByStudentNo(base)) return base;

        int i = 1;
        while (studentRepository.existsByStudentNo(base + "-" + i)) i++;
        return base + "-" + i;
    }

    public AdminUserResponse updateEnabled(Long id, UpdateEnabledRequest request) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        u.setEnabled(request.isEnabled());

        return toResponse(userRepository.save(u));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private AdminUserResponse toResponse(User u) {
        AdminUserResponse r = new AdminUserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setFullName(u.getFullName());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setEnabled(u.isEnabled());
        return r;
    }
}
