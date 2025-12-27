package com.example.demo.dto.admin;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateEnabledRequest {
    private boolean enabled;
}
