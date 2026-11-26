package com.pragma.powerup.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    private static final String EMPLOYEE_ROLE = "EMPLOYEE";
    private static final String SMS_URL = "/api/v1/sms";
    private static final String ERROR_URL = "/error";
    private static final String[] SWAGGER_URLS = {"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenParser jwtTokenParser)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                        .accessDeniedHandler(new JsonAccessDeniedHandler()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(SWAGGER_URLS).permitAll()
                        .requestMatchers(ERROR_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, SMS_URL).hasRole(EMPLOYEE_ROLE)
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenParser),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
