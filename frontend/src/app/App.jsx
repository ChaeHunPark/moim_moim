import api from '../api/axios';
import React, { useEffect } from 'react';
import { RouterProvider } from "react-router-dom"
import router from "./router"
import './App.css'

const App = () => {

useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('accessToken');
      
      // 1. 토큰이 아예 없는 경우: 비로그인 상태로 유지 (필요 시 /login 이동)
      if (!token) {
        console.log("비로그인 사용자");
        return;
      }

      // 2. 토큰이 있는 경우: 변조 여부 확인을 위해 백엔드 호출
      try {
        await api.get('/auth/verify');
        console.log("정상 로그인 사용자 인증 완료");
      } catch (error) {
        // 변조된 토큰일 경우 axios 인터셉터가 401/403을 잡아
        // 자동으로 localStorage 삭제 및 로그아웃 페이지로 이동시킴
        console.error("인증 검증 실패: 자동 로그아웃 로직 가동");
      }
    };

    initAuth();
  }, []);

  return <RouterProvider router={router} />
}

export default App
