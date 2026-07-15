package com.redmath.lecture02;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableMethodSecurity
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(auth -> auth.disable())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/news", "/api/v1/news/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/news").hasAnyRole("REPORTER", "EDITOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/news/*").hasAnyRole("REPORTER", "EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*").hasRole("EDITOR")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails reporter = User.builder()
                .username("reporter")
                .password("{noop}reporter")
                .roles("REPORTER")
                .build();

        UserDetails editor = User.builder()
                .username("editor")
                .password("{noop}editor")
                .roles("EDITOR")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(reporter, editor, admin);
    }
}
