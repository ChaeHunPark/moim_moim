import React, { useEffect } from 'react';
import { RouterProvider } from "react-router-dom"
import router from "./router"
import './App.css'

const App = () => {

useEffect(() => {
    const verifyLogin = async () => {
      const token = localStorage.getItem('accessToken');
      
      // 1. 토큰이 아예 없으면 검증할 필요도 없음
      if (!token) return;

      try {
        // 2. 인증이 필요한 API를 호출하여 토큰 검증 시도
        /*
         백엔드에서 permitAll 경로는 만료된 토큰이 와도 그냥 통과 시켜버리는 견우가 간혹 있으니
         이경우에는 401 반환이 안도히기 때문에 리이슈가 동작안할 수 있으므로
         인증 이 필요한 API를 앱 초기 로드시 호출해주는게 가장 확실한 방법이므로
         내 정보 확인 /auth/me 정도 api를 개발하면 바꿀 것.
        */
        await api.post('/auth/logout');
      
      } catch (error) {

        // 여기서 추가적인 초기화 로직추가
        localStorage.removeItem('accessToken');
  
      // 🎯 화면을 갱신하기 위해 강제로 홈이나 로그인 페이지로 보내기
      window.location.href = '/';
      }
    };

    verifyLogin();
  }, []);

  return <RouterProvider router={router} />
}

export default App
