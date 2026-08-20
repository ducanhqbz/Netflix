package com.example.authservice.Repository;

import com.example.authservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByCode(String code);

    List<Permission> findByParentIsNullOrderByIdAsc();
}
