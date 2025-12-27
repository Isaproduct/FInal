package com.example.demo.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileUpdateRequest {
    @NotBlank private String fullName;
    private String email;
}
