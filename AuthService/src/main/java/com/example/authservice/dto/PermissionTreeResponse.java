package com.example.authservice.dto;

import java.util.List;

public record PermissionTreeResponse(
        Long id,
        String code,
        String name,
        String httpMethod,
        String apiPattern,
        List<PermissionTreeResponse> children
) {
}
