package com.conceptune.connect.config;

import com.conceptune.connect.database.installer.Tables;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Log4j2
@Configuration
public class DatabaseConfig {

    @Autowired
    private Tables tables;

    @PostConstruct
    public void init() {
        tables.createIfNotExists();
    }
}
