package com.interviewai.repository;

import com.interviewai.model.InterviewSession;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// 临时方案（仅用于开发测试）
//@Component
//public class InMemoryInterviewRepository {
//    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();
//
//    // 添加过期机制（例如 1 小时后自动清理）
//    @PostConstruct
//    public void startCleanup() {
//        Executors.newSingleThreadScheduledExecutor()
//                .scheduleAtFixedRate(() -> {
//                    long now = System.currentTimeMillis();
//                    sessions.entrySet().removeIf(entry ->
//                            (now - entry.getValue().getCreatedAt()) > 3600_000);
//                }, 1, 10, TimeUnit.MINUTES);
//    }
//}