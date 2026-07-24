package com.redmath.welcome;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.redmath.OAuth2TestUsers.user;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WelcomeController.class)
@TestPropertySource(properties = {
        "welcome.sys-message=System Message",
        "welcome.app-message=Application Message",
        "WELCOME_ENV-MESSAGE=Environment Message"
})
//@ImportAutoConfiguration(CacheAutoConfiguration.class)
class WelcomeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WelcomeConfiguration configuration;

    @Test
    void welcome_ShouldReturnAuthenticatedUsername() throws Exception {

        mockMvc.perform(get("/api/v1/welcome")
                        .with(user("reporter", "ROLE_REPORTER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("reporter"));
    }

    @Test
    void welcomeConfiguration_ShouldReturnConfigurationProperties() throws Exception {

        when(configuration.getSysMessage()).thenReturn("Config Sys");
        when(configuration.getEnvMessage()).thenReturn("Config Env");
        when(configuration.getAppMessage()).thenReturn("Config App");

        mockMvc.perform(get("/api/v1/welcome/config")
                        .with(user("reporter", "ROLE_REPORTER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sys").value("Config Sys"))
                .andExpect(jsonPath("$.env").value("Config Env"))
                .andExpect(jsonPath("$.app").value("Config App"));
    }
}
