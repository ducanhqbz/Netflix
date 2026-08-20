package com.example.authservice.config;

import com.example.authservice.Repository.UserRepository;
import com.example.authservice.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    private final EntityManager entityManager;

    public UserDetailsServiceImpl(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = entityManager.createQuery(
                        "SELECT DISTINCT u FROM User u " +
                                "LEFT JOIN FETCH u.roles r " +
                                "LEFT JOIN FETCH r.permissions " +
                                "WHERE u.username = :username",
                        User.class
                )
                .setParameter("username", username)
                .getSingleResult();

        // Convert Role entities to GrantedAuthority
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null) {
            authorities.addAll(user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
                    .collect(Collectors.toList()));
        }

        user.getRoles().forEach(role -> role.getPermissions().forEach(permission -> {
            if (permission.getHttpMethod() != null && permission.getApiPattern() != null) {
                authorities.add(new SimpleGrantedAuthority(
                        "API:" + permission.getHttpMethod() + ":" + permission.getApiPattern()
                ));
            }
        }));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
