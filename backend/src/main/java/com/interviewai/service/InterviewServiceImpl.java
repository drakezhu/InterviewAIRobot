package com.interviewai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewai.ai.DashScopeClient;
import com.interviewai.ai.PromptLoader;
import com.interviewai.model.InterviewSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
            // 👇 关键：生成AI评估报告
            Map<String, Object> report = generateFinalReport(session);
            return Map.of("report", report);
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

    private Map<String, Object> generateFinalReport(InterviewSession session) {
        // 构造仅包含对话的消息（不含 system）
        List<Map<String, String>> conversationHistory = session.getConversation().stream()
                .filter(msg -> !"system".equals(msg.getRole()))
                .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                .collect(Collectors.toList());

        // 构造评估专用 prompt
        String evaluationPrompt = """
        你刚完成一场Java技术面试。以下是完整对话记录：
        %s

        请基于以上内容，生成一份专业的面试评估报告。要求：
        - 总分范围0-10分
        - 列出2-3个优点和不足
        - 给出2条具体改进建议
        - 对每轮问答给出简短点评
        - 严格按以下JSON格式输出，不要任何额外文字：

        {
          "overallScore": 7,
          "strengths": ["优点1", "优点2"],
          "weaknesses": ["不足1", "不足2"],
          "suggestions": ["建议1", "建议2"],
          "detailedFeedback": [
            {"question": "Q1", "userAnswer": "A1", "score": 8, "comments": "点评"}
          ]
        }
        """.formatted(conversationHistory.stream()
                .map(m -> "%s: %s".formatted(m.get("role").equals("user") ? "候选人" : "面试官", m.get("content")))
                .collect(Collectors.joining("\n")));

        try {
            // 使用更强模型生成报告
            String reportJsonStr = dashScopeClient.callForReport(evaluationPrompt);

            // 提取 JSON
            int start = reportJsonStr.indexOf('{');
            int end = reportJsonStr.lastIndexOf('}');
            if (start != -1 && end > start) {
                String cleanJson = reportJsonStr.substring(start, end + 1);
                return new ObjectMapper().readValue(cleanJson, Map.class);
            }
        } catch (Exception e) {
            // 降级
        }

        // 默认报告
        return Map.of(
                "overallScore", 6,
                "strengths", List.of("完成了全部面试流程"),
                "weaknesses", List.of("AI评估生成失败"),
                "suggestions", List.of("请确保网络畅通后重试"),
                "detailedFeedback", List.of()
        );
    }
}