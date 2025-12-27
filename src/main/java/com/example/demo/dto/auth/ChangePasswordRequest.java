package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChangePasswordRequest {
    @NotBlank private String oldPassword;
    @NotBlank private String newPassword;
}
