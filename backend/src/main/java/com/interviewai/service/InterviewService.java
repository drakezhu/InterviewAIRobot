package com.interviewai.service;

import com.interviewai.model.InterviewSession;

public interface InterviewService {
    InterviewSession startNewSession();
    Object submitAnswer(InterviewSession session, String userAnswer);
}