package com.court.hearing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Standalone bean config — lives outside SecurityConfig to break the
 * circular reference:
 *   SecurityConfig → JwtAuthFilter → UserDetailsService (AuthService)
 *   AuthService → AuthenticationManager → SecurityConfig
 *
 * Moving PasswordEncoder here means AuthService can inject it without
 * touching SecurityConfig at all.
 */
@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
