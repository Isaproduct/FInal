package com.example.demo.dto.auth;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
}
