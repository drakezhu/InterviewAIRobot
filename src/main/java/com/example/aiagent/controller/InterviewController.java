package com.example.aiagent.controller;

import com.example.aiagent.service.QwenInterviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
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
    public EvaluationResponse evaluate(@RequestBody EvaluationRequest request) {
        String feedback = interviewService.evaluateAnswer(request.getAnswer());
        return new EvaluationResponse(feedback);
    }

    // DTO 类
    public static class EvaluationRequest {
        private String answer;

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    public static class EvaluationResponse {
        private String feedback;

        public EvaluationResponse(String feedback) {
            this.feedback = feedback;
        }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}