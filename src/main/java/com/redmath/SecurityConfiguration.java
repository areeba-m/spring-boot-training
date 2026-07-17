package com.redmath;

import com.redmath.lecture06.user.ApiUser;
import com.redmath.lecture06.user.ApiUserService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiUserService userService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/news", "/api/v1/news/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/news").hasAnyRole("REPORTER", "EDITOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/news/*").hasAnyRole("REPORTER", "EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*").hasRole("EDITOR")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(config -> config
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(config -> config.successHandler((
                        (request, response, authentication) -> {
                            ApiUser user = userService.generateToken(authentication.getName());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"access_token\":" + "\"" + user.getToken() + "\"}");
                        }))
                )
                .oauth2ResourceServer(config -> config
                        .opaqueToken(config2 -> config2.introspector(token -> {
                            ApiUser user = userService.findByToken(token);

                            List<GrantedAuthority> authorities = Arrays.stream(user.getRole().split(","))
                                    .map(String::trim)
                                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList());

                            return new DefaultOAuth2AuthenticatedPrincipal(
                                    user.getUsername(),
                                    Map.of("sub", user.getUsername()),
                                    authorities
                            );
                        }))
                )
        ;

        return http.build();
    }
}
