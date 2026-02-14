package com.interviewai.controller;

import com.interviewai.service.QwenInterviewService;
import org.springframework.web.bind.annotation.*;

import com.interviewai.model.InterviewSession;
import com.interviewai.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/interviews")
//@CrossOrigin(origins = "http://localhost:5173")
public class InterviewController {

    private final InterviewService interviewService;

    // 内存存储会话（MVP 阶段）
    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * 开始新面试
     */
    @PostMapping
    public ResponseEntity<?> startInterview() {
        InterviewSession session = interviewService.startNewSession();
        sessions.put(session.getSessionId(), session);

        String firstQuestion = session.getConversation().get(1).getContent(); // 第二条是AI问题
        return ResponseEntity.ok(Map.of(
                "sessionId", session.getSessionId(),
                "question", firstQuestion
        ));
    }

    /**
     * 提交回答
     */
    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<?> submitAnswer(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {

        InterviewSession session = sessions.get(sessionId);
        if (session == null || session.isCompleted()) {
            return ResponseEntity.badRequest().body("Invalid session");
        }

        Object result = interviewService.submitAnswer(session, request.get("answer"));

        if (result instanceof String) {
            return ResponseEntity.ok(Map.of("question", result));
        } else {
            return ResponseEntity.ok(result); // 返回 report
        }
    }
}