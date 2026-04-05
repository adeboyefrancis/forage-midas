package com.jpmc.midascore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // Add this import
import org.springframework.web.client.RestTemplate; // Add this import

@SpringBootApplication
public class MidasCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MidasCoreApplication.class, args);
    }

    // The Bean must be inside the class braces
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
