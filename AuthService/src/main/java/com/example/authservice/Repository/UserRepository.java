package com.example.authservice.Repository;

import com.example.authservice.entity.User;

public interface UserRepository {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void save(User user);



}
