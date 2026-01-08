import axios from 'axios';

const api = axios.create({
  // Vite 프록시 설정 덕분에 '/api'만 적어도 8080으로 알아서 날아갑니다.
  baseURL: '/api', 
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 5000, // 5초 동안 응답 없으면 중단
});

export default api;