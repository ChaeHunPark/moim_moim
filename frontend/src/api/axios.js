import axios from 'axios';

const api = axios.create({
    // Nginx 설정과 맞추기 위해 baseURL을 '/api'로 설정
    // 호출 시 api.post('/auth/login') 처럼 앞의 /api는 생략해야 합니다.
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // 💡 중요: EC2(HTTPS) 환경에서 쿠키를 주고받기 위해 필수
    timeout: 5000,
});

/**
 * [1] 요청 인터셉터
 */
api.interceptors.request.use(
    (config) => {
        // 토큰이 필요 없는 경로 리스트
        const skipUrls = ['/auth/login', '/auth/signup', '/auth/reissue'];

        // 현재 요청 URL에 위 경로가 포함되어 있는지 확인
        const isSkipped = skipUrls.some(url => config.url && config.url.includes(url));

        if (!isSkipped) {
            const token = localStorage.getItem('accessToken');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

/**
 * [2] 응답 인터셉터
 */
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // 💡 중요: 로그인(/auth/login)이나 재발급(/auth/reissue) 요청 자체가 401이 난 경우에는
        // 여기서 재시도(reissue)를 하지 않고 바로 에러를 던져야 무한 루프가 안 생깁니다.
        const isAuthRequest = originalRequest.url.includes('/auth/login') ||
                              originalRequest.url.includes('/auth/reissue');

        if (error.response?.status === 401 && !originalRequest._retry && !isAuthRequest) {
            originalRequest._retry = true; // 재시도 플래그 설정

            try {
                console.log("🔄 토큰 만료 감지: 재발급 시도 중...");

                // 💡 axios 대신 설정이 완료된 api 인스턴스를 사용 (경로 중복 주의)
                const res = await api.post('/auth/reissue', {});

                if (res.status === 200) {
                    const newAccessToken = res.data.accessToken;
                    localStorage.setItem('accessToken', newAccessToken);

                    // 새 토큰으로 헤더 교체 후 원래 요청 재시도
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return api(originalRequest);
                }
            } catch (reissueError) {
                console.error("❌ 세션 만료: 다시 로그인해주세요.");
                localStorage.removeItem('accessToken');

                // 현재 페이지가 로그인이 아닐 때만 이동
                if (!window.location.pathname.includes('/login')) {
                    window.location.href = '/login';
                }
                return Promise.reject(reissueError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;