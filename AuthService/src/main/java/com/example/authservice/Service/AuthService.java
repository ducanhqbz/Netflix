package com.example.authservice.Service;


import org.springframework.stereotype.Service;


public interface AuthService {

    String login(String username, String password);

}
