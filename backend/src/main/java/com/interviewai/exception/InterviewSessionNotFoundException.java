package com.interviewai.exception;

public class InterviewSessionNotFoundException extends RuntimeException {
    public InterviewSessionNotFoundException(String sessionId) {
        super("Interview session not found: " + sessionId);
    }
}