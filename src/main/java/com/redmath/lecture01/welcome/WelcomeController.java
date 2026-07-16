package com.redmath.lecture01.welcome;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WelcomeController {

    @Value("${sys.welcome.message}")
    private String sysMessage;
    @Value("${ENV_WELCOME_MESSAGE}")
    private String envMessage;
    @Value("${app.welcome.message}")
    private String appMessage;

    @GetMapping("/api/welcome")
    public Map<String, Object> welcome(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("sys", sysMessage);
        response.put("env", envMessage);
        response.put("app", appMessage);
        response.put("user", auth != null ? auth.getName() : "anonymous");
        return response;
    }
}
