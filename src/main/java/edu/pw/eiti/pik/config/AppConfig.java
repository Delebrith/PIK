package edu.pw.eiti.pik.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {


    @Bean
    public String secretKey(@Value("${security.jwts.secret}") String secretKey){
        return secretKey;
    }
}
