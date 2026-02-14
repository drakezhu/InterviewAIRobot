// src/api/interviewApi.ts

const API_PREFIX = '/api/v1/interviews';

export interface StartInterviewResponse {
  sessionId: string;
  question: string;
}

export interface FinalReport {
  message: string;
  conversation: Array<{ role: string; content: string }>;
}

export interface SubmitAnswerResponse {
  question?: string;    // 下一个问题（继续面试）
  report?: FinalReport; // 面试结束报告
}

/**
 * 开始一场新的面试
 */
export const startInterview = async (): Promise<StartInterviewResponse> => {
  const response = await fetch(API_PREFIX, {
    method: 'POST',
    credentials: 'include', // 如果需要 cookie 认证
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`开始面试失败: ${response.status} - ${errorText}`);
  }

  return response.json();
};

/**
 * 提交用户回答
 */
export const submitAnswer = async (
  sessionId: string,
  answer: string
): Promise<SubmitAnswerResponse> => {
  const response = await fetch(`${API_PREFIX}/${sessionId}/answers`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ answer }),
    credentials: 'include',
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`提交回答失败: ${response.status} - ${errorText}`);
  }

  return response.json();
};