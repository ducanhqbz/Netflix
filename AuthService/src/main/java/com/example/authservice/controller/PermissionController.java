package com.example.authservice.controller;

import com.example.authservice.Service.PermissionService;
import com.example.authservice.dto.PermissionRequest;
import com.example.authservice.dto.PermissionTreeResponse;
import com.example.authservice.dto.RolePermissionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth/admin/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/tree")
    public List<PermissionTreeResponse> getTree() {
        return permissionService.getTree();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionTreeResponse create(@Valid @RequestBody PermissionRequest request) {
        return permissionService.create(request);
    }

    @PutMapping("/roles/{roleCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignToRole(
            @PathVariable String roleCode,
            @Valid @RequestBody RolePermissionRequest request
    ) {
        permissionService.assignToRole(roleCode, request.permissionIds());
    }
}
