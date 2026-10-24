package com.example.demo.dashboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** El reloj se inyecta para poder probar el dashboard con una fecha fija. */
@Configuration
public class RelojConfig {
    @Bean
    Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
