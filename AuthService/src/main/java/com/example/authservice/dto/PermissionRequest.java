package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionRequest(
        @NotBlank String code,
        @NotBlank String name,
        Long parentId,
        String httpMethod,
        String apiPattern
) {
}
