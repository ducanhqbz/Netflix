package com.example.authservice.Service;

import com.example.authservice.Repository.RoleRepository;
import com.example.authservice.entity.Role;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Optional<Role> findByRoleCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode);
    }
}

