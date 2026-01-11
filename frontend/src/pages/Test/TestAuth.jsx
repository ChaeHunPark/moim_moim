import React, { useState } from 'react';
import api from '../../api/axios';

const TestAuth = () => {
  const [userInfo, setUserInfo] = useState(null);
  const [error, setError] = useState(null);

  const handleFetchMyInfo = async () => {
    try {
      setError(null);
      // 인가(Authorization) 테스트용 API 호출
      const response = await api.get('/test/me');
      
      // 성공 시 데이터 저장
      setUserInfo(response.data);
      console.log('인가 성공 데이터:', response.data);
    } catch (err) {
      // 실패 시 에러 처리 (403, 401 등)
      setError(err.response?.status === 403 ? '권한이 없습니다 (토큰 오류)' : '정보를 가져오는데 실패했습니다.');
      setUserInfo(null);
      console.error('인가 테스트 에러:', err);
    }
  };

  return (
    <div style={{ padding: '20px', border: '1px solid #ccc', marginTop: '20px' }}>
      <h3>🔐 인가(Authorization) 테스트</h3>
      <button 
        onClick={handleFetchMyInfo}
        style={{
          padding: '10px 20px',
          backgroundColor: '#4CAF50',
          color: 'white',
          border: 'none',
          borderRadius: '5px',
          cursor: 'pointer'
        }}
      >
        내 정보 가져오기 (토큰 필요)
      </button>

      {userInfo && (
        <div style={{ marginTop: '10px', color: 'blue' }}>
          <p>✅ {userInfo.message}</p>
          <p>📧 로그인 유저: {userInfo.loginUser}</p>
        </div>
      )}

      {error && (
        <p style={{ marginTop: '10px', color: 'red' }}>❌ {error}</p>
      )}
    </div>
  );
};

export default TestAuth;