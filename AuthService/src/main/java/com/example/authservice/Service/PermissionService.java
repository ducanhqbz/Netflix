package com.example.authservice.Service;

import com.example.authservice.Repository.PermissionRepository;
import com.example.authservice.Repository.RoleRepository;
import com.example.authservice.dto.PermissionRequest;
import com.example.authservice.dto.PermissionTreeResponse;
import com.example.authservice.entity.Permission;
import com.example.authservice.entity.Role;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public PermissionTreeResponse create(PermissionRequest request) {
        if (permissionRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Permission code already exists");
        }
        validateApiMapping(request.httpMethod(), request.apiPattern());

        Permission parent = request.parentId() == null ? null : permissionRepository.findById(request.parentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent permission not found"));

        Permission permission = permissionRepository.save(Permission.builder()
                .code(request.code())
                .name(request.name())
                .parent(parent)
                .httpMethod(normalize(request.httpMethod()))
                .apiPattern(request.apiPattern())
                .build());
        return toTree(permission);
    }

    @Transactional
    public List<PermissionTreeResponse> getTree() {
        return permissionRepository.findByParentIsNullOrderByIdAsc().stream()
                .map(this::toTree)
                .toList();
    }

    @Transactional
    public void assignToRole(String roleCode, Set<Long> permissionIds) {
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more permissions were not found");
        }
        role.setPermissions(new LinkedHashSet<>(permissions));
    }

    private PermissionTreeResponse toTree(Permission permission) {
        return new PermissionTreeResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getHttpMethod(),
                permission.getApiPattern(),
                permission.getChildren().stream().map(this::toTree).toList()
        );
    }

    private void validateApiMapping(String httpMethod, String apiPattern) {
        boolean hasMethod = httpMethod != null && !httpMethod.isBlank();
        boolean hasPattern = apiPattern != null && !apiPattern.isBlank();
        if (hasMethod != hasPattern) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "httpMethod and apiPattern must be provided together"
            );
        }
    }

    private String normalize(String httpMethod) {
        return httpMethod == null ? null : httpMethod.toUpperCase();
    }
}
