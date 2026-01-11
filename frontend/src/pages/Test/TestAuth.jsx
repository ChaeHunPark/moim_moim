import React, { useState } from 'react';
import api from '../../api/axios';

const TestAuth = () => {
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  // 🚩 공통 API 호출 함수
  const handleTestAccess = async (roleType) => {
    try {
      setError(null);
      setResult(null);

      // 백엔드 SecurityConfig에 설정한 경로에 맞게 호출
      // 일반유저용: /api/test/user, 관리자용: /api/test/admin
      const response = await api.get(`/test/${roleType}`);
      
      setResult({
        role: roleType,
        data: response.data
      });
      console.log(`${roleType} 권한 호출 성공:`, response.data);
    } catch (err) {
      const status = err.response?.status;
      let message = '정보를 가져오는데 실패했습니다.';
      
      if (status === 403) {
        message = `권한이 없습니다. (현재 계정은 ${roleType} 접근 불가)`;
      } else if (status === 401) {
        message = '로그인이 필요합니다. (토큰 없음)';
      }

      setError(message);
      console.error(`${roleType} 테스트 에러:`, err);
    }
  };

  return (
    <div style={{ padding: '20px', border: '1px solid #ccc', marginTop: '20px', borderRadius: '10px' }}>
      <h3>🔐 Role 기반 인가(Authorization) 테스트</h3>
      
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        {/* 일반 유저 테스트 버튼 */}
        <button 
          onClick={() => handleTestAccess('user')}
          style={buttonStyle('#4CAF50')}
        >
          USER 권한 테스트
        </button>

        {/* 관리자 테스트 버튼 */}
        <button 
          onClick={() => handleTestAccess('admin')}
          style={buttonStyle('#f44336')}
        >
          ADMIN 권한 테스트
        </button>
      </div>

      <hr />

      {/* 결과창 */}
      {result && (
        <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#e8f5e9', borderRadius: '5px' }}>
          <p style={{ color: '#2e7d32', fontWeight: 'bold' }}>✅ 접근 성공!</p>
          <pre>{JSON.stringify(result.data, null, 2)}</pre>
        </div>
      )}

      {/* 에러창 */}
      {error && (
        <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#ffeecf', borderRadius: '5px' }}>
          <p style={{ color: '#d32f2f', fontWeight: 'bold' }}>❌ 접근 거부 (Error)</p>
          <p>{error}</p>
        </div>
      )}
    </div>
  );
};

// 버튼 스타일 헬퍼 함수
const buttonStyle = (color) => ({
  padding: '10px 20px',
  backgroundColor: color,
  color: 'white',
  border: 'none',
  borderRadius: '5px',
  cursor: 'pointer',
  fontWeight: 'bold'
});

export default TestAuth;