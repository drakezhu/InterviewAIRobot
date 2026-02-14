package com.example.aiagent.controller;

import com.example.aiagent.service.QwenInterviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
@CrossOrigin(origins = "http://localhost:5173")
public class InterviewController {

    private final QwenInterviewService interviewService;

    public InterviewController(QwenInterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * 提交面试回答，获取 AI 反馈
     * POST /api/interview/evaluate
     * Body: {"answer": "我的回答内容"}
     */
    @PostMapping("/evaluate")
    public String evaluate(@RequestBody EvaluationRequest request) {
        return interviewService.evaluateAnswer(request.getAnswer());
    }

    // DTO 类
    public static class EvaluationRequest {
        private String answer;

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}