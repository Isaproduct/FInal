package com.example.demo.service;

import com.example.demo.dto.auth.*;
import com.example.demo.dto.user.ProfileResponse;
import com.example.demo.dto.user.ProfileUpdateRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private AuthService authService;

    @Test
    void register_success() {
        RegisterRequest req = RegisterRequest.builder()
                .username("user1")
                .password("pass")
                .fullName("User One")
                .email("u1@mail.com")
                .build();

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("u1@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("ENC");
        when(jwtUtil.generateToken(any())).thenReturn("TOKEN");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = authService.register(req);

        assertEquals("TOKEN", resp.getToken());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("user1", saved.getUsername());
        assertEquals("ENC", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());     // ВАЖНО: у тебя роль USER по умолчанию
        assertTrue(saved.isEnabled());
    }

    @Test
    void register_usernameExists_throws() {
        when(userRepository.existsByUsername("user1")).thenReturn(true);

        RegisterRequest req = RegisterRequest.builder()
                .username("user1").password("pass").fullName("U").email("u@mail.com")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest req = LoginRequest.builder().username("user1").password("pass").build();

        User u = User.builder().id(1L).username("user1").password("ENC").role(Role.USER).enabled(true).build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(u));
        when(jwtUtil.generateToken(any())).thenReturn("TOKEN");

        AuthResponse resp = authService.login(req);

        assertEquals("TOKEN", resp.getToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_userBlocked_throws() {
        LoginRequest req = LoginRequest.builder().username("user1").password("pass").build();

        User u = User.builder().id(1L).username("user1").enabled(false).role(Role.USER).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(u));

        assertThrows(IllegalArgumentException.class, () -> authService.login(req));
    }

    @Test
    void me_returnsProfileResponse() {
        User u = User.builder().id(2L).username("u").fullName("Name").email("a@b.com").role(Role.ADMIN).enabled(true).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(u));

        ProfileResponse resp = authService.me("u");

        assertEquals(2L, resp.getId());
        assertEquals("u", resp.getUsername());
        assertEquals("Name", resp.getFullName());
        assertEquals("a@b.com", resp.getEmail());
        assertEquals(Role.ADMIN, resp.getRole());
        assertTrue(resp.isEnabled());
    }

    @Test
    void updateProfile_updatesEmailAndFullName() {
        User u = User.builder().id(2L).username("u").fullName("Old").email("old@b.com").role(Role.USER).enabled(true).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(u));
        when(userRepository.existsByEmail("new@b.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileUpdateRequest req = ProfileUpdateRequest.builder().fullName("New").email("new@b.com").build();

        ProfileResponse resp = authService.updateProfile("u", req);

        assertEquals("New", resp.getFullName());
        assertEquals("new@b.com", resp.getEmail());
        verify(userRepository).save(u);
    }

    @Test
    void changePassword_success() {
        User u = User.builder().id(2L).username("u").password("OLD_ENC").enabled(true).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old", "OLD_ENC")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("NEW_ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ChangePasswordRequest req = ChangePasswordRequest.builder().oldPassword("old").newPassword("new").build();

        authService.changePassword("u", req);

        assertEquals("NEW_ENC", u.getPassword());
        verify(userRepository).save(u);
    }

    @Test
    void changePassword_wrongOld_throws() {
        User u = User.builder().id(2L).username("u").password("OLD_ENC").enabled(true).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", "OLD_ENC")).thenReturn(false);

        ChangePasswordRequest req = ChangePasswordRequest.builder().oldPassword("bad").newPassword("new").build();

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword("u", req));
        verify(userRepository, never()).save(any());
    }
}
