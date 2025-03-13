package com.conceptune.connect.config;

import com.conceptune.connect.settings.InstanceVariable;
import com.twilio.Twilio;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Log4j2
@Configuration
public class TwilioConfig {

    @Value("classpath:twilio-sdk.json")
    private Resource twilioSdkResource;

    @PostConstruct
    public void init() throws Exception {
        log.info("Connecting to twilio...");
        FileInputStream inputStream = new FileInputStream(twilioSdkResource.getFile());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject options = new JSONObject(bufferedReader.lines().collect(Collectors.joining("\n")));
        Twilio.init(options.getString("accountId"), options.getString("authToken"));
        InstanceVariable.TWILIO_SERVICE_ID = options.getString("serviceId");
        log.info("Connected to twilio successfully.");
    }
}
