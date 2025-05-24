package com.example.ridesharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@ComponentScan("com.example.ridesharing")
@SpringBootApplication
@EnableScheduling
public class RidesharingApplication {
    public static void main(String[] args) {
        SpringApplication.run(RidesharingApplication.class, args);
    }
@PostConstruct
public void printMongoUri() {
    System.out.println("MONGODB_URI: " + System.getenv("MONGODB_URI"));
}

}
