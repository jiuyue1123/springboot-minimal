package com.example.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Application entry point.
 *
 * @author nanak
 */
@SpringBootApplication
public class WebApplication {

    private static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
        SpringApplication.run(WebApplication.class, args);
    }
}
