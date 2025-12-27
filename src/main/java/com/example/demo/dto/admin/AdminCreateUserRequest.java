package com.example.demo.dto.admin;

import com.example.demo.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AdminCreateUserRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String fullName;
    private String email;

    @NotNull private Role role;
}
