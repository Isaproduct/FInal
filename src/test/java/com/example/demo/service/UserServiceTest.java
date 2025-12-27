package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    void createUser_success() {
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = userService.createUser("user1", "pass", Role.STUDENT);

        assertEquals(1L, saved.getId());
        assertEquals("user1", saved.getUsername());
        assertEquals("ENC", saved.getPassword());
        assertEquals(Role.STUDENT, saved.getRole());
        assertTrue(saved.isEnabled());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("user1", captor.getValue().getUsername());
        assertEquals("ENC", captor.getValue().getPassword());
    }

    @Test
    void createUser_usernameExists_throws() {
        when(userRepository.existsByUsername("user1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("user1", "pass", Role.STUDENT));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserCredentials_updatesUsernameAndPassword() {
        User existing = User.builder().id(10L).username("old").password("OLD_ENC").role(Role.STUDENT).enabled(true).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("new")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpass")).thenReturn("NEW_ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUserCredentials(10L, "new", "newpass");

        assertEquals("new", updated.getUsername());
        assertEquals("NEW_ENC", updated.getPassword());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUserCredentials_usernameTakenByOther_throws() {
        User existing = User.builder().id(10L).username("old").password("OLD_ENC").build();
        User other = User.builder().id(99L).username("new").password("X").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("new")).thenReturn(Optional.of(other));

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserCredentials(10L, "new", "newpass"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserCredentials_onlyPasswordChanges() {
        User existing = User.builder().id(10L).username("same").password("OLD_ENC").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("NEW_ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUserCredentials(10L, null, "newpass");

        assertEquals("same", updated.getUsername());
        assertEquals("NEW_ENC", updated.getPassword());
        verify(userRepository).save(existing);
    }
}
