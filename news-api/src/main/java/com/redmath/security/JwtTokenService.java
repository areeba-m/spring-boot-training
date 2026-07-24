package com.redmath.security;

import com.redmath.user.ApiUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
public class JwtTokenService implements InitializingBean {

    private NimbusJwtEncoder jwtEncoder;
    private NimbusJwtDecoder jwtDecoder;
    private SignatureAlgorithm algorithm;

    @Override
    public void afterPropertiesSet() throws Exception {
        algorithm = SignatureAlgorithm.PS256;
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        jwtEncoder = NimbusJwtEncoder.withKeyPair(
                        (RSAPublicKey) keyPair.getPublic(),
                        (RSAPrivateKey) keyPair.getPrivate())
                .algorithm(algorithm)
                .build();
        jwtDecoder = NimbusJwtDecoder.withPublicKey(
                        (RSAPublicKey) keyPair.getPublic())
                .signatureAlgorithm(algorithm)
                .build();

        log.info("Public key: {}",
                Base64.getEncoder().encodeToString(
                        keyPair.getPublic().getEncoded()));
    }

    public String generateToken(ApiUser user) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(user.getToken())
                .subject(user.getUsername())
                .claim("scope", user.getRole())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        JwsHeader header = JwsHeader.with(algorithm).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }
}
