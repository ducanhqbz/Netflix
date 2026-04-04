package com.example.discoverservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


@EnableEurekaServer
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataRedisAutoConfiguration.class}) // <--- Thêm phần exclude này
public class DiscoverServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoverServiceApplication.class, args);
    }

}
