package com.redmath;

import com.redmath.lecture06.user.ApiUser;
import com.redmath.lecture06.user.ApiUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class ApiSecurityService {
    private final ApiUserService userService;
    private final NimbusJwtEncoder jwtEncoder;
    private final NimbusJwtDecoder jwtDecoder;
    private final SignatureAlgorithm jwtAlgorithm;

    public ApiSecurityService(ApiUserService userService) throws NoSuchAlgorithmException {
        this.userService = userService;
        this.jwtAlgorithm = SignatureAlgorithm.PS256;
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.jwtEncoder = NimbusJwtEncoder.withKeyPair((RSAPublicKey) keyPair.getPublic(),
                        (RSAPrivateKey) keyPair.getPrivate())
                .algorithm(this.jwtAlgorithm)
                .build();
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic())
                .signatureAlgorithm(this.jwtAlgorithm)
                .build();
        log.info("public key: ()", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    }

    public void onAuthenticationSuccessForm(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException {
        ApiUser user = userService.generateToken(authentication.getName());
        generateToken(response, user);
    }

    public void onAuthenticationSuccessOauth(HttpServletRequest request, HttpServletResponse response,
                                             Authentication authentication) throws IOException {
        ApiUser user = userService.registerAndGenerateToken(getProviderUsername(authentication));
        generateToken(response, user);
    }

    public DefaultOAuth2AuthenticatedPrincipal verify(String token){
        Jwt jwt = jwtDecoder.decode(token);
        String username = jwt.getSubject();
        String roles = jwt.getClaimAsString("scope");
        return new DefaultOAuth2AuthenticatedPrincipal(username,
                Map.of("sub", username),
                AuthorityUtils.createAuthorityList(roles.split(",")));
    }

    private void generateToken(HttpServletResponse response, ApiUser user) throws IOException {
        response.setContentType("application/json");
        JwsHeader header = JwsHeader.with(this.jwtAlgorithm).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(user.getToken())
                .subject(user.getUsername())
                .claim("scope", user.getRole())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
        response.getWriter().write("{\"access_token\":" + "\"" + jwt.getTokenValue() + "\"}");
    }

    private String getProviderUsername(Authentication authentication) {
        OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
        String provider = oAuth2Token.getAuthorizedClientRegistrationId();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if("google".equalsIgnoreCase(provider)){
            return oAuth2User.getAttribute("email");
        } else if ("github".equalsIgnoreCase(provider)){
            return oAuth2User.getAttribute("login");
        }

        return oAuth2User.getAttribute("email");
    }
}
