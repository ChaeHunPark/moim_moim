import { createBrowserRouter, Navigate } from "react-router-dom"
import Layout from "../components/common/Layout"
import HomePage from "../pages/Home/HomePage"
import RegisterPage from "../pages/Register/RegisterPage";
import LoginForm from "../components/login/LoginForm";
import LoginPage from "../pages/Login/LoginPage";
import TestAuth from "../pages/Test/TestAuth";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />, // 상단/하단 바가 있는 부모
    children: [
      {
        index: true,
        element: <HomePage /> // Layout의 Outlet 자리에 들어감
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

  { path: "/testauth",
    element: <TestAuth/>,

  }

]);

export default router
