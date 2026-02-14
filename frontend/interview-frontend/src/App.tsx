// src/App.tsx
import React, { useState, useEffect } from 'react';
import { 
  startInterview, 
  submitAnswer,
  type SubmitAnswerResponse,
  type FinalReport 
} from './api/interviewApi';

const App: React.FC = () => {
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [currentQuestion, setCurrentQuestion] = useState<string | null>(null);
  const [userAnswer, setUserAnswer] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [finalReport, setFinalReport] = useState<FinalReport | null>(null);
  const [error, setError] = useState<string | null>(null);

  // 自动聚焦输入框
  useEffect(() => {
    if (currentQuestion) {
      const textarea = document.querySelector('textarea');
      if (textarea) textarea.focus();
    }
  }, [currentQuestion]);

  const handleStart = async () => {
    setError(null);
    try {
      const data = await startInterview();
      setSessionId(data.sessionId);
      setCurrentQuestion(data.question);
      setUserAnswer('');
      setFinalReport(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : '未知错误');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sessionId || !userAnswer.trim()) return;

    setIsSubmitting(true);
    setError(null);

    try {
      const response: SubmitAnswerResponse = await submitAnswer(sessionId, userAnswer.trim());

      if (response.question) {
        // 继续面试
        setCurrentQuestion(response.question);
        setUserAnswer('');
      } else if (response.report) {
        // 面试结束
        setFinalReport(response.report);
        setCurrentQuestion(null);
        setUserAnswer('');
      } else {
        throw new Error('无效的响应格式');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '提交失败，请重试');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 to-purple-100 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl bg-white rounded-xl shadow-lg p-6 md:p-8">
        <h1 className="text-3xl font-bold text-center text-gray-800 mb-2">
          AI 技术面试官
        </h1>
        <p className="text-gray-600 text-center mb-8">
          与 AI 进行真实的多轮技术面试
        </p>

        {/* 错误提示 */}
        {error && (
          <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-lg text-sm">
            {error}
          </div>
        )}

        {/* 面试完成 */}
        {finalReport && (
  <div className="space-y-6">
    {/* 总分展示 */}
    <div className="text-center bg-gradient-to-r from-indigo-500 to-purple-600 text-white p-6 rounded-xl">
      <div className="text-5xl font-bold mb-2">{finalReport.overallScore}/10</div>
      <div className="text-lg opacity-90">综合能力评分</div>
    </div>

    {/* 优点 & 不足 */}
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div className="bg-green-50 border border-green-200 rounded-lg p-4">
        <h3 className="font-bold text-green-800 mb-2 flex items-center">
          ✅ 优势
        </h3>
        <ul className="list-disc pl-5 space-y-1 text-green-700">
          {finalReport.strengths.map((s, i) => (
            <li key={i}>{s}</li>
          ))}
        </ul>
      </div>
      
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <h3 className="font-bold text-red-800 mb-2 flex items-center">
          ⚠️ 待提升
        </h3>
        <ul className="list-disc pl-5 space-y-1 text-red-700">
          {finalReport.weaknesses.map((w, i) => (
            <li key={i}>{w}</li>
          ))}
        </ul>
      </div>
    </div>

    {/* 改进建议 */}
    <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
      <h3 className="font-bold text-blue-800 mb-2">💡 发展建议</h3>
      <ul className="list-disc pl-5 space-y-1 text-blue-700">
        {finalReport.suggestions.map((s, i) => (
          <li key={i}>{s}</li>
        ))}
      </ul>
    </div>

    {/* 详细反馈（可选折叠） */}
    {finalReport.detailedFeedback.length > 0 && (
      <div className="border-t pt-4">
        <h3 className="font-bold text-gray-800 mb-3">📝 分轮次反馈</h3>
        <div className="space-y-4">
          {finalReport.detailedFeedback.map((fb, idx) => (
            <div key={idx} className="border border-gray-200 rounded-lg p-3">
              <div className="font-medium text-gray-700 mb-1">Q{idx + 1}: {fb.question}</div>
              <div className="text-sm text-gray-600 mb-2">你的回答: {fb.userAnswer}</div>
              <div className="flex justify-between items-center">
                <span className="text-xs bg-gray-100 px-2 py-1 rounded">得分: {fb.score}/10</span>
                <span className="text-sm text-gray-700">{fb.comments}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    )}

    {/* 重新开始按钮 */}
    <button
      onClick={() => window.location.reload()}
      className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition"
    >
      重新开始面试
    </button>
  </div>
)}

        {/* 面试进行中 */}
        {currentQuestion && (
          <div className="space-y-6">
            <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
              <h2 className="font-medium text-blue-800 mb-1">面试官问：</h2>
              <p className="text-blue-700">{currentQuestion}</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label htmlFor="answer" className="block text-sm font-medium text-gray-700 mb-1">
                  你的回答
                </label>
                <textarea
                  id="answer"
                  value={userAnswer}
                  onChange={(e) => setUserAnswer(e.target.value)}
                  placeholder="请详细回答..."
                  rows={5}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
                  required
                />
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className={`w-full py-3 font-medium rounded-lg transition ${
                  isSubmitting
                    ? 'bg-gray-400 cursor-not-allowed'
                    : 'bg-indigo-600 hover:bg-indigo-700 text-white'
                }`}
              >
                {isSubmitting ? 'AI 面试官正在思考...' : '提交回答'}
              </button>
            </form>
          </div>
        )}

        {/* 初始状态 */}
        {!currentQuestion && !finalReport && (
          <div className="text-center">
            <button
              onClick={handleStart}
              className="py-3 px-8 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition transform hover:scale-105"
            >
              开始面试
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default App;