package com.redmath;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;

public final class OAuth2TestUsers {

    private OAuth2TestUsers() {}

    public static RequestPostProcessor user(String username, String... authorities) {
        return opaqueToken()
                .principal(new DefaultOAuth2AuthenticatedPrincipal(
                        username,
                        Map.of("sub", username),
                        AuthorityUtils.createAuthorityList(authorities)
                ));
    }
}
