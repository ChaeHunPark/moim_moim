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
api.interceptors.response.use(
    (response) => response, // 성공 시 그대로 반환
    async (error) => {
        const originalRequest = error.config;

        // 에러 코드가 401이고, 아직 재시도를 하지 않은 요청이라면
        if (error.response.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true; // 무한 루프 방지 플래그

            try {
                // 서버에 재발급 요청 (쿠키의 Refresh Token이 자동으로 전송됨)
                const res = await axios.post('/api/auth/reissue', {}, { withCredentials: true });

                if (res.status === 200) {
                    const newAccessToken = res.data.accessToken;
                    
                    // 새 토큰 저장
                    localStorage.setItem('accessToken', newAccessToken);
                    
                    // 원래 요청의 헤더를 새 토큰으로 교체 후 다시 시도
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return api(originalRequest);
                }
            } catch (reissueError) {
                // 리프레시 토큰도 만료된 경우
                console.error("세션이 만료되었습니다. 다시 로그인해주세요.");
                localStorage.removeItem('accessToken');
                window.location.href = '/login'; 
                return Promise.reject(reissueError);
            }
        }
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