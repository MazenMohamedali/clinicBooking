package com.clinicHelper.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(req -> req
                .requestMatchers("/api/v1/auth/**")
                .permitAll()
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/patient/**").hasAnyRole("PATIENT", "ADMIN") 
                .requestMatchers("/appointments/book").hasRole("PATIENT")
                .requestMatchers("/appointments/patient/**").hasRole("PATIENT")
                .requestMatchers("/appointments/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/appointments/receptionist/**").hasRole("RECEPTIONIST")
                .requestMatchers("/appointments/today").hasAnyRole("DOCTOR", "RECEPTIONIST")
                .requestMatchers("/appointments/**")
                .authenticated()
                .anyRequest()
                .authenticated())
                .sessionManagement(session -> session 
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
