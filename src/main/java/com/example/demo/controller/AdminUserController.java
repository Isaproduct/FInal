package com.example.demo.controller;

import com.example.demo.dto.admin.AdminCreateUserRequest;
import com.example.demo.dto.admin.AdminUserResponse;
import com.example.demo.dto.admin.UpdateEnabledRequest;
import com.example.demo.dto.admin.UpdateRoleRequest;
import com.example.demo.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserResponse> getAll() {
        return adminUserService.findAll();
    }

    @PostMapping
    public AdminUserResponse create(@Valid @RequestBody AdminCreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    @GetMapping("/{id}")
    public AdminUserResponse getById(@PathVariable Long id) {
        return adminUserService.findById(id);
    }

    @PatchMapping("/{id}/role")
    public AdminUserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return adminUserService.updateRole(id, request);
    }

    @PatchMapping("/{id}/enabled")
    public AdminUserResponse updateEnabled(@PathVariable Long id, @Valid @RequestBody UpdateEnabledRequest request) {
        return adminUserService.updateEnabled(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminUserService.delete(id);
    }
}
