package com.conceptune.connect.config;

import com.conceptune.connect.settings.InstanceVariable;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.TimeZone;
import java.util.UUID;

@Log4j2
@Configuration
public class AppConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("System Timezone: UTC");

        InstanceVariable.ID = UUID.randomUUID().toString();
        log.info("System Instance Id: {}", InstanceVariable.ID);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
