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
 * 
 * 토큰이 필요 없는 경로는 토큰을 실어 보내지 않는 인터셉터
 * 
 */
// [1] 요청 인터셉터
api.interceptors.request.use(
    (config) => {
        const skipUrls = ['/auth/login', '/auth/signup', '/auth/reissue'];
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
        const status = error.response?.status;
        const errorCode = error.response?.data?.code;
        const serverMessage = error.response?.data?.message; // 백엔드의 부드러운 메시지

        console.log(`🚩 [Interceptor] 에러 발생: ${status} (${errorCode})`);

        // 인증 에러(401, 403) 처리
        if ((status === 401 || status === 403) && !originalRequest._retry) {
            
            // 1. 변조된 토큰일 경우 (INVALID, MALFORMED 등)
            if (errorCode === 'INVALID_TOKEN' || errorCode === 'MALFORMED_TOKEN') {
                console.error("🚨 토큰 변조 감지: 강제 로그아웃");
                localStorage.removeItem('accessToken');
                // 서버에서 보낸 부드러운 메시지("인증 정보가 유효하지 않습니다...") 출력
                alert(serverMessage || "로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.");
                window.location.replace('/login');
                return Promise.reject(error);
            }

            // 2. 앱 진입 시 토큰 검증(/auth/verify) 실패 시
            if (originalRequest.url.includes('/auth/verify')) {
                localStorage.removeItem('accessToken');
                window.location.replace('/login');
                return Promise.reject(error);
            }

            // 3. 토큰 만료인 경우 (EXPIRED_TOKEN) -> 조용히 리이슈 시도
            if (errorCode === 'EXPIRED_TOKEN') {
                originalRequest._retry = true; // 재시도 플래그 설정

                try {
                    console.log("🔄 토큰 만료: 재발급(reissue) 시도 중...");
                    const res = await api.post('/auth/reissue', {});
                    
                    if (res.status === 200) {
                        const newAccessToken = res.data.accessToken;
                        localStorage.setItem('accessToken', newAccessToken);
                        
                        // 원래 요청의 헤더를 새 토큰으로 교체 후 재요청
                        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                        console.log("✅ 토큰 재발급 성공: 기존 요청 재실행");
                        return api(originalRequest); 
                    }
                } catch (reissueError) {
                    console.error("❌ 리이슈 실패: 세션 완전히 만료");
                    localStorage.removeItem('accessToken');
                    // 리이슈조차 실패하면 리프레시 토큰도 죽은 것이므로 알림 후 로그인행
                    alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
                    window.location.replace('/login');
                    return Promise.reject(reissueError);
                }
            }
        }

        // 그 외 일반 에러 응답
        return Promise.reject(error);
    }
);

export default api;