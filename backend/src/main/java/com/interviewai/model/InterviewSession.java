package com.interviewai.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class InterviewSession {
    private final String sessionId;
    private final List<Message> conversation; // 存储完整对话历史
    private boolean completed = false;

    public InterviewSession(String sessionId) {
        this.sessionId = sessionId;
        this.conversation = new ArrayList<>();
    }

    // ===== Getters =====
    public String getSessionId() { return sessionId; }
    public List<Message> getConversation() { return conversation; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    // ===== 工具方法 =====
    public void addMessage(String role, String content) {
        this.conversation.add(new Message(role, content));
    }

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getters (for Jackson serialization)
        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}