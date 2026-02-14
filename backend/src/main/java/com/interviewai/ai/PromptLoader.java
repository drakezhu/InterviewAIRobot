package com.interviewai.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptLoader {

    private String systemPrompt;
    private String evaluationTemplate;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource systemRes = new ClassPathResource("prompts/system_interviewer.txt");
            ClassPathResource evalRes = new ClassPathResource("prompts/evaluation_template.txt");

            this.systemPrompt = StreamUtils.copyToString(systemRes.getInputStream(), StandardCharsets.UTF_8);
            this.evaluationTemplate = StreamUtils.copyToString(evalRes.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt files from classpath", e);
        }
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getEvaluationTemplate() {
        return evaluationTemplate;
    }
}