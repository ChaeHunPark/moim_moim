import axios from 'axios';

const api = axios.create({
  // Vite 프록시 설정 덕분에 '/api'만 적어도 8080으로 알아서 날아갑니다.
  baseURL: '/api', 
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 5000, // 5초 동안 응답 없으면 중단
});

// [추가] 요청 인터셉터: 서버로 보내기 직전에 가로채서 토큰을 넣어줍니다.
api.interceptors.request.use(
  (config) => {
    // 로컬 스토리지에서 토큰을 가져옵니다.
    const token = localStorage.getItem('accessToken');
    
    // 토큰이 있다면 Authorization 헤더에 Bearer 타입으로 추가합니다.
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// [추가] 응답 인터셉터: 서버 응답을 받을 때 가로채서 에러 처리를 합니다.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // 만약 토큰이 만료되어 401 에러가 발생했다면 로그아웃 처리 등을 할 수 있습니다.
    if (error.response?.status === 401) {
      console.error('인증이 만료되었습니다. 다시 로그인해주세요.');
      localStorage.removeItem('accessToken');
      // window.location.href = '/login'; // 필요시 로그인 페이지로 강제 이동
    }
    return Promise.reject(error);
  }
);

export default api;