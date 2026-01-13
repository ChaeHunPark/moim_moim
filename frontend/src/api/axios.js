import axios from 'axios';

const api = axios.create({
    // Vite 프록시 설정 덕분에 '/api'만 적어도 8080으로 알아서 날아갑니다.
    baseURL: '/api', 
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // 💡 쿠키 전송 및 수신 허용
    timeout: 5000,
});

/**
 * [1] 요청 인터셉터: 서버로 보내기 직전에 가로채서 토큰을 넣어줍니다.
 * 모든 API 호출 시 헤더에 Authorization을 자동으로 추가합니다.
 */
// [수정] 요청 인터셉터
api.interceptors.request.use(
    (config) => {
        // 토큰이 필요 없는 경로 리스트
        const skipUrls = ['/auth/login', '/auth/signup', '/auth/reissue'];
        
        // 현재 요청 URL이 skipUrls에 포함되어 있는지 확인
        const isSkipped = skipUrls.some(url => config.url.includes(url));

        if (!isSkipped) {
            const token = localStorage.getItem('accessToken');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

/**
 * [2] 응답 인터셉터: 서버 응답을 받을 때 가로채서 성공/실패를 처리합니다.
 * 401 에러(토큰 만료) 발생 시 자동으로 토큰 재발급 로직을 수행합니다.
 */
api.interceptors.response.use(
    (response) => response, // 2xx 범위의 상태 코드는 그대로 반환
    async (error) => {
        const originalRequest = error.config;

        // 에러 코드가 401(Unauthorized)이고, 아직 재시도를 하지 않은 요청인 경우
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true; // 무한 루프 방지

            try {
                console.log("🔄 토큰 만료 감지: 재발급 시도 중...");
                
                // 서버에 재발급 요청 (쿠키의 Refresh Token이 전송되도록 설정)
                const res = await axios.post('/api/auth/reissue', {}, { withCredentials: true });

                if (res.status === 200) {
                    const newAccessToken = res.data.accessToken;
                    
                    // 새 토큰 저장
                    localStorage.setItem('accessToken', newAccessToken);
                    
                    // 원래 요청의 헤더를 새 토큰으로 교체 후 다시 실행
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return api(originalRequest);
                }
            } catch (reissueError) {
                // 리프레시 토큰까지 만료된 경우 (진짜 세션 만료)
                console.error("❌ 세션이 완전히 만료되었습니다. 다시 로그인해주세요.");
                localStorage.removeItem('accessToken');
                window.location.href = '/login'; 
                return Promise.reject(reissueError);
            }
        }

        // 401 이외의 에러 처리 (Edge Case: 403, 404, 500 등)
        return Promise.reject(error);
    }
);

export default api;