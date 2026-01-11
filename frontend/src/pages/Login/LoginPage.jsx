import React from 'react';
import LoginForm from '../../components/login/LoginForm';
import './LoginPage.css';

const LoginPage = () => {
    return (
        <div className="login-page-background">
            <div className="login-container">
                <h1 className="login-title">모임모임 로그인</h1>
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