import { createBrowserRouter, Navigate } from "react-router-dom"
import Layout from "../components/common/Layout"
import RegisterPage from "../pages/Register/RegisterPage";
import LoginPage from "../pages/Login/LoginPage";
import TestAuth from "../pages/Test/TestAuth";
import MeetingCreate from "../pages/Meeting/MeetingCreate";
import Preparing from "../pages/Home/preparing";
import HomePage from "../pages/Home/HomePage";
import MeetingDetail from "../pages/Meeting/MeetingDetail";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />, // 상단/하단 바가 있는 부모
    children: [
      {
        index: true,
        // element: <Preparing />,
        element:<HomePage/>,
      },
      {
        path: "/meeting-create",
        element: <MeetingCreate />,
      },
      // [추가] 모임 상세 페이지 라우트
      {
        path: "/meetings/:id", // :id가 useParams()의 id와 매칭됩니다.
        element: <MeetingDetail />,
      },

      {
        path: "/testauth",
        element: <TestAuth />,

      },
    ]
  },
  {
    path: "/register",
    // 현재는 리다이렉트
    // element: <Navigate to="/" replace />,
    // 운영, 테스트시 주석 해제
    element: <RegisterPage />,


  },
  {
    path: "/login",
    element: <LoginPage />,
  },

]);

export default router
