package com.interviewai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class QwenInterviewService {

    private static final String DASHSCOPE_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    @Value("${dashscope.api-key}")
    private String apiKey;
    private final String CONVERSATIONS_DIR = "conversations";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String evaluateAnswer(String userAnswer) {
        try {
            // 构建请求体（纯 JSON 字符串）
            String requestBody = buildRequestBody(userAnswer);

            // 发送 HTTP 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DASHSCOPE_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode output = root.get("output");
                JsonNode choices = output.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null) {
                        String aiResponse = message.get("content").asText();
                        saveConversation(userAnswer, aiResponse);
                        return aiResponse;
                    }
                }
                throw new RuntimeException("Invalid response format from DashScope");
            } else {
                return "API 调用失败: " + response.statusCode() + " - " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "系统错误: " + e.getMessage();
        }
    }

    private String buildRequestBody(String userAnswer) {
        return """
                {
                  "model": "qwen-turbo",
                  "input": {
                    "messages": [
                      {"role": "system", "content": "你是一位资深 Java 面试官..."},
                      {"role": "user", "content": "%s"}
                    ]
                  },
                   "parameters": {
                        "max_tokens": 200,
                        "temperature": 0.3,
                        "result_format": "message"
                   }
                }
                """.formatted(userAnswer.replace("\"", "\\\""));
    }

    private void saveConversation(String userAnswer, String aiResponse) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ".json";
        Path filePath = Paths.get(CONVERSATIONS_DIR, filename);

        var conversation = objectMapper.createObjectNode();
        conversation.put("timestamp", timestamp);
        conversation.put("user_answer", userAnswer);
        conversation.put("ai_feedback", aiResponse);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), conversation);
    }
}