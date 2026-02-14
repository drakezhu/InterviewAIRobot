package com.interviewai.model;

import lombok.Data;

@Data
public class QuestionAnswer {
    private String question;
    private String userAnswer;
    private Evaluation evaluation;

    public QuestionAnswer() {}

    public QuestionAnswer(String question) {
        this.question = question;
        this.userAnswer = "";
        this.evaluation = null;
    }
}