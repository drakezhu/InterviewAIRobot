package com.interviewai;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiAgentApplication {
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8"); // 确保 JVM 使用 UTF-8

        Dotenv dotenv = Dotenv.configure()
//                .ignoreIfMissing() // 如果没有 .env 不报错
                .load();

        // 👇 第二步：把 .env 的值设为系统属性（Spring Boot 会读取）
        System.setProperty("DASHSCOPE_API_KEY", dotenv.get("DASHSCOPE_API_KEY"));
        SpringApplication.run(AiAgentApplication.class, args);
    }
}