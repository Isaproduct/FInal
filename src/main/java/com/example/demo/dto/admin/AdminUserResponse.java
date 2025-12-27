package com.example.demo.dto.admin;

import com.example.demo.model.Role;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
}
