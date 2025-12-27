package com.example.demo.dto.user;

import com.example.demo.model.Role;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
}
