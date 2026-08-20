package com.example.homepageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"com.example.homepageservice", "Config", "Controller", "Repository", "Advice", "Error"},
        exclude = { MongoAutoConfiguration.class }
)
@EnableJpaRepositories(basePackages = "Repository")
@EntityScan(basePackages = "entity")
public class HomePageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomePageServiceApplication.class, args);
    }

}

