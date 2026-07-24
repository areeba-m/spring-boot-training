package com.redmath.security;

import com.redmath.user.ApiUser;
import com.redmath.user.ApiUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiSecurityServiceTest {

    @Mock
    private ApiUserService userService;

    @Mock
    private JwtTokenService jwtService;

    private ObjectMapper objectMapper;

    @InjectMocks
    private ApiSecurityService securityService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        securityService = new ApiSecurityService(userService, jwtService, objectMapper);
    }

    @Test
    void shouldGenerateJwtForFormLogin() throws Exception {
        ApiUser user = new ApiUser();
        user.setUsername("john");

        when(userService.loginAndGenerateToken("john")).thenReturn(user);

        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = new UsernamePasswordAuthenticationToken("john", "password");

        securityService.onAuthenticationSuccessForm(
                request,
                response,
                authentication
        );

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getContentAsString()).contains("jwt-token");

        verify(userService).loginAndGenerateToken("john");
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldGenerateJwtForGoogleOauthLogin() throws Exception {
        ApiUser user = new ApiUser();
        user.setUsername("john@gmail.com");

        when(userService.registerAndGenerateToken("john@gmail.com")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("oauth-token");

        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of("email", "john@gmail.com"),
                "email"
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                List.of(),
                "google"
        );

        MockHttpServletResponse response = new MockHttpServletResponse();
        securityService.onAuthenticationSuccessOauth(
                new MockHttpServletRequest(),
                response,
                authentication
        );
        assertThat(response.getContentAsString()).contains("oauth-token");
        verify(userService).registerAndGenerateToken("john@gmail.com");
    }

    @Test
    void shouldGenerateJwtForGithubOauthLogin() throws Exception {
        ApiUser user = new ApiUser();
        user.setUsername("octocat");

        when(userService.registerAndGenerateToken("octocat")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("github-token");

        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of("login", "octocat"),
                "login"
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                List.of(),
                "github"
        );

        MockHttpServletResponse response = new MockHttpServletResponse();
        securityService.onAuthenticationSuccessOauth(
                new MockHttpServletRequest(),
                response,
                authentication
        );
        verify(userService).registerAndGenerateToken("octocat");
        assertThat(response.getContentAsString()).contains("github-token");
    }
}