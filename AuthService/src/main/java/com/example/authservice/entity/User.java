package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.util.HashSet;
import java.util.Set;

@Table(name = "users")
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;


    @Column(unique = true, nullable = false, name = "USERNAME")
    private String username;

    @Column(unique = true, nullable = false, name = "EMAIL")
    private String email;

    @Column(nullable = false, name = "PASSWORD")
    private String password;


    @Column(name = "STATUS")
    private String status;

    @CreatedDate
    @Column(nullable = false, name = "CREATED_DATE")
    private String createdDate;


    @Column(nullable = false, name = "MODIFIED_DATE")
    private String modifiedDate;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
