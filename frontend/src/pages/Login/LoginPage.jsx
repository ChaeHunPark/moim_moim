import React from 'react';
import { useNavigate } from 'react-router-dom'; // 🎯 이동을 위해 추가
import LoginForm from '../../components/login/LoginForm';
import './LoginPage.css';

const LoginPage = () => {
    const navigate = useNavigate(); // 🎯 홈 이동 함수

    return (
        <div className="login-page-background">
            <div className="login-container">
                {/* 🎯 로고 역할을 하는 타이틀: 클릭 시 홈('/')으로 이동 */}
                <h1 className="login-logo-title" onClick={() => navigate('/')}>
                    모임모임
                </h1>
                <p className="login-subtitle">반가운 동료들이 기다리고 있어요! 😊</p>

                <LoginForm />

                <div className="login-footer">
                    아직 회원이 아니신가요? <a href="/register">회원가입</a>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;