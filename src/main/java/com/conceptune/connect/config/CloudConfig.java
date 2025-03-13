package com.conceptune.connect.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;

@Log4j2
@Configuration
public class CloudConfig {

    @Value("classpath:gcp-sdk.json")
    private Resource gcpSdkResource;

    @PostConstruct
    public void init() throws Exception {
        log.info("Connecting to cloud...");
        FileInputStream inputStream = new FileInputStream(gcpSdkResource.getFile());
        FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(inputStream)).build();
        FirebaseApp.initializeApp(options);
        log.info("Connected to cloud successfully.");
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
