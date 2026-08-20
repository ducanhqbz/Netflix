package com.example.authservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RolePermissionRequest(@NotNull Set<Long> permissionIds) {
}
