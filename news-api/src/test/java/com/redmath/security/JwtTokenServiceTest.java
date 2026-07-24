package com.redmath.security;

import com.redmath.user.ApiUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenService = new JwtTokenService();
        jwtTokenService.afterPropertiesSet();
    }

    @Test
    void generateToken_shouldCreateValidJwt() {
        ApiUser user = new ApiUser();
        user.setUsername("john");
        user.setRole("ADMIN");
        user.setToken("abc123");

        String token = jwtTokenService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());

        Jwt jwt = jwtTokenService.decode(token);
        assertEquals("john", jwt.getSubject());
        assertEquals("ADMIN", jwt.getClaimAsString("scope"));
        assertEquals("abc123", jwt.getId());
    }

    @Test
    void decode_shouldReturnSameClaimsGenerated() {
        ApiUser user = new ApiUser();
        user.setUsername("alice");
        user.setRole("EDITOR");
        user.setToken("token-id");

        String token = jwtTokenService.generateToken(user);

        Jwt jwt = jwtTokenService.decode(token);
        assertAll(
                () -> assertEquals("alice", jwt.getSubject()),
                () -> assertEquals("EDITOR", jwt.getClaimAsString("scope")),
                () -> assertEquals("token-id", jwt.getId())
        );
    }

    @Test
    void generatedToken_shouldExpireInApproximatelyFiveMinutes() {
        ApiUser user = new ApiUser();
        user.setUsername("john");
        user.setRole("ADMIN");
        user.setToken("xyz");

        Instant before = Instant.now();
        String token = jwtTokenService.generateToken(user);

        Jwt jwt = jwtTokenService.decode(token);
        Instant expires = jwt.getExpiresAt();
        assertNotNull(expires);

        long seconds = Duration.between(before, expires).getSeconds();
        assertTrue(seconds >= 295);
        assertTrue(seconds <= 305);
    }

    @Test
    void decode_shouldThrowExceptionForInvalidToken() {
        assertThrows(JwtException.class, () -> jwtTokenService.decode("this-is-not-a-jwt"));
    }

    @Test
    void generatedToken_shouldContainExpectedHeader() {
        ApiUser user = new ApiUser();
        user.setUsername("john");
        user.setRole("ADMIN");
        user.setToken("id");

        String token = jwtTokenService.generateToken(user);

        Jwt jwt = jwtTokenService.decode(token);
        assertEquals("PS256", jwt.getHeaders().get("alg"));
    }
}