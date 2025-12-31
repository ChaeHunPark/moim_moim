import React, { useEffect, useState } from "react";
import axios from "axios";

function DataFetcher() {
  const [message, setMessage] = useState('백엔드 메시지를 불러오는 중...');
  const [error, setError] = useState(null);

  useEffect(() => {
    // Spring Boot 서버의 주소와 엔드포인트
    fetch('/api/data') 
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP Error! Status: ${response.status}`);
        }
        return response.text(); 
      })
      .then(data => {
        setMessage(`${data}`);
      })
      .catch(err => {
        console.error('API 호출 중 오류 발생:', err);
        setError(`API 호출 실패: ${err.message}. CORS 설정 또는 서버 상태 확인 필요.`);
        setMessage('통신 실패');
      });
  }, []);

  return (
    <div>
      <h1>Spring Boot API 통신 결과: 깃 액션 자동화 체크6</h1>
      {error ? (
        <p style={{ color: 'red' }}>{error}</p>
      ) : (
        <p style={{ color: 'green', fontWeight: 'bold' }}>{message}</p>
      )}
    </div>
  );
}
export default DataFetcher;

