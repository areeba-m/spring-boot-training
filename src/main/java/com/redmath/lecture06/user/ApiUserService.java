package com.redmath.lecture06.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiUserService implements UserDetailsService {

    private ApiUserRepository userRepository;
    private JwtEncoder jwtEncoder;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${security.jwt.issuer}")
    private String issuer;
    @Value("${security.jwt.expiration}")
    private long expirationMillis;

    public ApiUserService(ApiUserRepository userRepository, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApiUser user = findUser(username);
        return User.withUsername(username)
                .password(user.getPassword())
                .roles(user.getRole().split(","))
                .build();
    }

    public String registerAndGenerateToken(String username){
        Optional<ApiUser> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            user = Optional.of(registerOAuthUser(username));
        }
        return generateJwt(user.get());
    }

    public String generateToken(String username){
        ApiUser user = findUser(username);
        return generateJwt(user);
    }

    private ApiUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private ApiUser registerOAuthUser(String username) {
        ApiUser newUser = new ApiUser();
        newUser.setUserId(System.currentTimeMillis());
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole("REPORTER");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(newUser);
    }

    private String generateJwt(ApiUser user) {
        Instant now = Instant.now();
        List<String> roles = Arrays.stream(user.getRole().split(","))
                .map(String::trim)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(expirationMillis, ChronoUnit.MILLIS))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
