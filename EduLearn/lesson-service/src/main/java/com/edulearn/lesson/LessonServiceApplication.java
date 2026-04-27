
package com.edulearn.lesson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Entry point for the Lesson Service Spring Boot application.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LessonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LessonServiceApplication.class, args);
    }
}
