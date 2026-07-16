package com.redmath.lecture01.welcome;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/welcome")
public class WelcomeController {

    private final WelcomeConfiguration configuration;

    @Value("${welcome.sys-message}")
    private String sysMessage;
    @Value("${WELCOME_ENV-MESSAGE}")
    private String envMessage;
    @Value("${welcome.app-message}")
    private String appMessage;

    public WelcomeController(WelcomeConfiguration configuration) {
        this.configuration = configuration;
    }

    @GetMapping
    public Map<String, Object> welcome(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("sys", sysMessage);
        response.put("env", envMessage);
        response.put("app", appMessage);
        response.put("user", auth != null ? auth.getName() : "anonymous");
        return response;
    }

    @GetMapping("/config")
    public Map<String, Object> welcomeConfigurationProperties(){
        Map<String, Object> response = new HashMap<>();
        response.put("sys", configuration.getSysMessage());
        response.put("env", configuration.getEnvMessage());
        response.put("app", configuration.getAppMessage());
        return response;
    }
}
