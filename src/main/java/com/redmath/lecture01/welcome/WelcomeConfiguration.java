package com.redmath.lecture01.welcome;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "welcome")
@Getter @Setter
public class WelcomeConfiguration {
    private String sysMessage;
    private String envMessage;
    private String appMessage;
}
