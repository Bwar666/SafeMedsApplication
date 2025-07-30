package com.safemeds.safemedsbackend;

import com.safemeds.safemedsbackend.entities.UserProfile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SafemedsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafemedsBackendApplication.class, args);
    }

    @GetMapping
    public String  test(){
        return "Hello World Spring Boot SafemedsBackendApplication";
    }
}
