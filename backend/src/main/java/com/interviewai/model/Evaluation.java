package com.interviewai.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Evaluation {
    private int score; // 0-10
    private List<String> weaknesses;
    private String suggestion;

    public Evaluation() {
        this.weaknesses = new ArrayList<>();
        this.suggestion = "";
    }
}