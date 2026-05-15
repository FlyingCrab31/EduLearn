package com.edulearn.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

/**
 * Entry point for the EduLearn Spring MVC Web application.
 *
 * <p>This module acts as the presentation layer (MVC / Thymeleaf) that integrates
 * all underlying microservices (auth, course, lesson, enrollment, assessment,
 * payment, progress, discussion, notification) via a load-balanced RestTemplate
 * and renders Thymeleaf views for Students, Instructors, and Administrators.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class EduLearnWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduLearnWebApplication.class, args);
    }

    /**
     * Load-balanced RestTemplate that resolves logical service names
     * (e.g. "http://auth-service/...") through Eureka + Spring Cloud LoadBalancer.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
