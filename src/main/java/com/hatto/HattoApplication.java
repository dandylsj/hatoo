package com.hatto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HattoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HattoApplication.class, args);
    }

}
