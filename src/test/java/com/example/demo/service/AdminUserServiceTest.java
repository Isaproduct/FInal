package com.example.demo.service;

import com.example.demo.dto.admin.AdminCreateUserRequest;
import com.example.demo.dto.admin.UpdateEnabledRequest;
import com.example.demo.dto.admin.UpdateRoleRequest;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repo.StudentRepository;
import com.example.demo.repo.TeacherRepository;
import com.example.demo.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AdminUserService adminUserService;

    @Test
    void findAll_returnsList() {
        User u1 = User.builder().id(1L).username("u1").role(Role.USER).enabled(true).build();
        User u2 = User.builder().id(2L).username("u2").role(Role.ADMIN).enabled(false).build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        var res = adminUserService.findAll();

        assertEquals(2, res.size());
        assertEquals("u1", res.get(0).getUsername());
        assertEquals("u2", res.get(1).getUsername());
    }

    @Test
    void createUser_student_createsStudentProfileIfMissing() {
        AdminCreateUserRequest req = AdminCreateUserRequest.builder()
                .username("stud1")
                .password("pass")
                .fullName("Student One")
                .email("s1@mail.com")
                .role(Role.STUDENT)
                .build();

        when(userRepository.existsByUsername("stud1")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("ENC");

        // важно: нужен id, потому что generateUniqueStudentNo использует u.getId()
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(12L);
            return u;
        });

        when(studentRepository.findByUserUsername("stud1")).thenReturn(Optional.empty());
        when(studentRepository.existsByStudentNo(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = adminUserService.createUser(req);

        assertEquals("stud1", resp.getUsername());
        assertEquals(Role.STUDENT, resp.getRole());
        assertTrue(resp.isEnabled());

        verify(studentRepository).save(any(Student.class));
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void createUser_usernameExists_throws() {
        when(userRepository.existsByUsername("x")).thenReturn(true);

        AdminCreateUserRequest req = AdminCreateUserRequest.builder()
                .username("x").password("p").fullName("X").role(Role.USER)
                .build();

        assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateRole_toStudent_createsStudentProfileIfMissing() {
        User u = User.builder().id(1L).username("u").fullName("U").role(Role.ADMIN).enabled(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        when(studentRepository.findByUserUsername("u")).thenReturn(Optional.empty());
        when(studentRepository.existsByStudentNo(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateRoleRequest req = UpdateRoleRequest.builder().role(Role.STUDENT).build();

        adminUserService.updateRole(1L, req);

        assertEquals(Role.STUDENT, u.getRole());
        verify(studentRepository).save(any(Student.class));
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateRole_toTeacher_createsTeacherProfileIfMissing() {
        User u = User.builder().id(1L).username("u").fullName("U").role(Role.ADMIN).enabled(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        when(teacherRepository.findByUserUsername("u")).thenReturn(Optional.empty());
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateRoleRequest req = UpdateRoleRequest.builder().role(Role.TEACHER).build();

        adminUserService.updateRole(1L, req);

        assertEquals(Role.TEACHER, u.getRole());
        verify(teacherRepository).save(any(Teacher.class));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateRole_toAdmin_doesNotCreateProfiles() {
        User u = User.builder().id(1L).username("u").role(Role.STUDENT).enabled(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateRoleRequest req = UpdateRoleRequest.builder().role(Role.ADMIN).build();

        adminUserService.updateRole(1L, req);

        assertEquals(Role.ADMIN, u.getRole());
        verify(studentRepository, never()).save(any());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateEnabled_updatesFlag() {
        User u = User.builder().id(1L).username("u").enabled(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateEnabledRequest req = UpdateEnabledRequest.builder().enabled(false).build();

        adminUserService.updateEnabled(1L, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertFalse(captor.getValue().isEnabled());
    }

    @Test
    void delete_notFound_throws() {
        when(userRepository.existsById(7L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> adminUserService.delete(7L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_success_callsDelete() {
        when(userRepository.existsById(7L)).thenReturn(true);

        adminUserService.delete(7L);

        verify(userRepository).deleteById(7L);
    }
}
