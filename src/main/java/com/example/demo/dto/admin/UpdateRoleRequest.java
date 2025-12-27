package com.example.demo.dto.admin;

import com.example.demo.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateRoleRequest {
    @NotNull private Role role;
}
