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

import java.util.Collection;
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
                        "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username",
                        User.class
                )
                .setParameter("username", username)
                .getSingleResult();

        // Convert Role entities to GrantedAuthority
        var authorities = (user.getRoles() != null && !user.getRoles().isEmpty())
                ? user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
                        .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities((Collection<? extends GrantedAuthority>) authorities)
                .build();
    }
}

