package com.example.demo.controller;

import com.example.demo.dto.auth.*;
import com.example.demo.dto.user.ProfileResponse;
import com.example.demo.dto.user.ProfileUpdateRequest;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public ProfileResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }
        return authService.me(authentication.getName());
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(Authentication authentication,
                                         @Valid @RequestBody ProfileUpdateRequest req) {
        return authService.updateProfile(authentication.getName(), req);
    }

    @PostMapping("/change-password")
    public void changePassword(Authentication authentication,
                               @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(authentication.getName(), req);
    }
}
