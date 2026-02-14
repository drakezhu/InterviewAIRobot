package com.interviewai.service;

import com.interviewai.ai.DashScopeClient;
import com.interviewai.ai.PromptLoader;
import com.interviewai.model.InterviewSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InterviewServiceImpl implements InterviewService {

    private final DashScopeClient dashScopeClient;
    private final String systemPrompt;
    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();

    public InterviewServiceImpl(
            @Value("${dashscope.api-key}") String apiKey,
            PromptLoader promptLoader) {
        this.dashScopeClient = new DashScopeClient(apiKey);
        this.systemPrompt = promptLoader.getSystemPrompt();
    }

    @Override
    public InterviewSession startNewSession() {
        String sessionId = java.util.UUID.randomUUID().toString();
        InterviewSession session = new InterviewSession(sessionId);

        // 添加 system prompt
        session.addMessage("system", systemPrompt);

        // 首次调用获取第一个问题
        String firstQuestion = generateNextQuestion(session);
        session.addMessage("assistant", firstQuestion);

        sessions.put(sessionId, session);
        return session;
    }

    @Override
    public Object submitAnswer(InterviewSession session, String userAnswer) {
        // 添加用户回答
        session.addMessage("user", userAnswer);

        // 检查是否达到最大轮数（防止无限循环）
        int userMessageCount = (int) session.getConversation().stream()
                .filter(msg -> "user".equals(msg.getRole()))
                .count();

        if (userMessageCount >= 5) {
            session.setCompleted(true);
            return Map.of(
                    "report", Map.of(
                            "message", "感谢参与面试！AI面试官已完成评估。",
                            "conversation", session.getConversation()
                    )
            );
        }

        // 获取AI的下一个问题/追问
        String nextQuestion = generateNextQuestion(session);
        session.addMessage("assistant", nextQuestion);

        return nextQuestion;
    }

    private String generateNextQuestion(InterviewSession session) {
        try {
            return dashScopeClient.callWithHistory(systemPrompt, session.getConversation());
        } catch (Exception e) {
            // 降级策略：返回固定问题
            return "看起来有些技术问题。请解释一下Java中的垃圾回收机制？";
        }
    }
}