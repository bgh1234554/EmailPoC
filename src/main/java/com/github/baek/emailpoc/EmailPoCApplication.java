package com.github.baek.emailpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EmailPoCApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailPoCApplication.class, args);
    }

}
