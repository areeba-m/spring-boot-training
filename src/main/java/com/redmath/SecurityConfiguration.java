package com.redmath;

import com.redmath.lecture06.user.ApiUserService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
@OpenAPIDefinition(info=@Info(title="News Api"), security=@SecurityRequirement(name="BearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer")
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ApiUserService userService,
                                           JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/news", "/api/v1/news/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/news").hasAnyRole("REPORTER", "EDITOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/news/*").hasAnyRole("REPORTER", "EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*").hasRole("EDITOR")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(config -> config
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(config -> config
                        .successHandler((request, response, authentication) -> {
                            String token = userService.generateToken(authentication.getName());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"access_token\":" + "\"" + token + "\"}");
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                            String email = oAuth2User.getAttribute("email");

                            String token = userService.registerAndGenerateToken(email);

                            response.setContentType("application/json");
                            response.getWriter().write("{\"access_token\":" + "\"" + token + "\"}");
                        })
                )
                .oauth2ResourceServer(config -> config
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }
}
