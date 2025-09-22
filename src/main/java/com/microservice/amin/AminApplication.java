package com.microservice.amin;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
@EnableAsync
public class AminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AminApplication.class, args);
    }

}
