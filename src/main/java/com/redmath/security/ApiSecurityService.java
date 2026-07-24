package com.redmath.security;

import com.redmath.lecture06.ApiUser;
import com.redmath.lecture06.ApiUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class ApiSecurityService {

    private final ApiUserService userService;
    private final JwtTokenService jwtService;
    private final ObjectMapper objectMapper;

    public ApiSecurityService(ApiUserService userService,
                              JwtTokenService jwtService,
                              ObjectMapper objectMapper) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    public void onAuthenticationSuccessForm(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException {
        ApiUser user = userService.loginAndGenerateToken(authentication.getName());
        generateJwtToken(response, user);
    }

    public void onAuthenticationSuccessOauth(HttpServletRequest request, HttpServletResponse response,
                                             Authentication authentication) throws IOException {
        ApiUser user = userService.registerAndGenerateToken(getProviderUsername(authentication));
        generateJwtToken(response, user);
    }

    public DefaultOAuth2AuthenticatedPrincipal verify(String token) {
        Jwt jwt = jwtService.decode(token);
        String username = jwt.getSubject();
        String roles = jwt.getClaimAsString("scope");

        return new DefaultOAuth2AuthenticatedPrincipal(
                username,
                Map.of("sub", username),
                AuthorityUtils.createAuthorityList(roles.split(",")));
    }

    private void generateJwtToken(HttpServletResponse response, ApiUser user) throws IOException {
        String token = jwtService.generateToken(user);
        Map<String, String> body = Map.of("access_token", token);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store");
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String getProviderUsername(Authentication authentication) {
        OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
        String provider = oAuth2Token.getAuthorizedClientRegistrationId();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        if(oAuth2User == null)
            return null;
        if("google".equalsIgnoreCase(provider)){
            return oAuth2User.getAttribute("email");
        } else if ("github".equalsIgnoreCase(provider)){
            return oAuth2User.getAttribute("login");
        }

        return oAuth2User.getAttribute("email");
    }
}
