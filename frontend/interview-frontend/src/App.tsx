// src/App.tsx
import { useState } from 'react';
import axios from 'axios';

function App() {
  const [userAnswer, setUserAnswer] = useState('');
  const [aiFeedback, setAiFeedback] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userAnswer.trim()) return;

    setLoading(true);
    setAiFeedback('');

    try {
      // 👇 关键：调用你的 Java 后端
    const response = await axios.post('http://localhost:8080/api/interview/evaluate', {
      answer: userAnswer
    });

    setAiFeedback(response.data); // ✅ 安全！因为 data 是 string
    } catch (error: any) {
      console.error('Error:', error);
      if (error.code === 'ERR_NETWORK') {
        setAiFeedback('❌ 无法连接到后端服务。请确保 Java 应用正在运行（端口 8080）');
      } else if (error.response?.status === 404) {
        setAiFeedback('❌ 后端接口路径错误，请检查 Controller 的 @PostMapping 路径');
      } else {
        setAiFeedback(`❌ 错误: ${error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl bg-white rounded-xl shadow-lg p-6 space-y-6">
        <h1 className="text-3xl font-bold text-center text-gray-800">
          Java 面试官 AI
        </h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="answer" className="block text-sm font-medium text-gray-700 mb-1">
              请输入你的回答：
            </label>
            <textarea
              id="answer"
              value={userAnswer}
              onChange={(e) => setUserAnswer(e.target.value)}
              placeholder="例如：HashMap 是线程安全的吗？为什么？"
              className="w-full h-32 p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none resize-none"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            disabled={loading || !userAnswer.trim()}
            className={`w-full py-3 px-4 rounded-lg font-semibold text-white ${
              loading || !userAnswer.trim()
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-blue-600 hover:bg-blue-700'
            } transition-colors`}
          >
            {loading ? 'AI 正在思考...' : '提交回答'}
          </button>
        </form>

        {aiFeedback && (
          <div className="mt-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <h2 className="font-bold text-gray-800 mb-2">AI 面试官反馈：</h2>
            <div className="whitespace-pre-wrap text-gray-700">{aiFeedback}</div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;