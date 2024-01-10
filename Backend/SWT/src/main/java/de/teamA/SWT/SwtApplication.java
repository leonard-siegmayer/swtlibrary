package de.teamA.SWT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwtApplication.class, args);
    }
}
