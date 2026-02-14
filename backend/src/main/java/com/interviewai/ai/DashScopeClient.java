package com.interviewai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewai.model.InterviewSession;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class DashScopeClient {

    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DashScopeClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String callWithHistory(String systemPrompt, List<InterviewSession.Message> history)
            throws Exception {

        // 构造完整消息列表（确保第一个是system）
        new java.util.ArrayList<>(List.of(
                Map.of("role", "system", "content", systemPrompt)
        )).addAll(history.stream()
                .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                .toList());

        // 移除可能重复的system（保留第一个）
        java.util.List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        for (InterviewSession.Message msg : history) {
            if (!"system".equals(msg.getRole())) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        // 构造请求体
        Map<String, Object> requestBody = Map.of(
                "model", "qwen-turbo",
                "input", Map.of("messages", messages),
                "parameters", Map.of(
                        "max_tokens", 300,
                        "temperature", 0.7, // 稍微提高创造性
                        "result_format", "message"
                )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 发送请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("DashScope error: " + response.body());
        }

        // 提取 content
        Map<String, Object> root = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> output = (Map<String, Object>) root.get("output");
        List<Map<String, Object>> choices = (List<Map<String, Object>>) output.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}