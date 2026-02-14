package com.interviewai.config;

import com.interviewai.ai.DashScopeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public DashScopeClient dashScopeClient(@Value("${dashscope.api-key}") String apiKey) {
        return new DashScopeClient(apiKey);
    }
}