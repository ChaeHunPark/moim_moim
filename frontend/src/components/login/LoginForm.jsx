import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../api/axios'; // 작성하신 axios 인스턴스 경로
import './LoginForm.css';

const LoginForm = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const { email, password } = formData;

  // 입력값 변경 핸들러
  const onChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // 로그인 제출 핸들러
  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      // 백엔드 로그인 API 호출 (Vite 프록시 덕분에 /api/auth/login 등으로 날아감)
      const response = await api.post('/auth/login', {
        email,
        password,
      });

      // 서버의 응답 구조에 따라 수정 (예: response.data.accessToken)
      const token = response.data.token || response.data.accessToken;

      if (token) {
        localStorage.setItem('accessToken', token);
        alert('로그인에 성공했습니다! 🥳');
        navigate('/'); // 메인 페이지로 리다이렉트
      }
    } catch (err) {
      console.error('Login Error:', err);
      setError(err.response?.data?.message || '이메일 또는 비밀번호가 올바르지 않습니다.');
    }
  };

  return (
    <form className="login-form" onSubmit={onSubmit}>
      <div className="form-group">
        <label htmlFor="email">이메일</label>
        <input
          type="email"
          id="email"
          name="email"
          value={email}
          onChange={onChange}
          placeholder="example@moim.com"
          required
        />
      </div>

      <div className="form-group">
        <label htmlFor="password">비밀번호</label>
        <input
          type="password"
          id="password"
          name="password"
          value={password}
          onChange={onChange}
          placeholder="비밀번호를 입력하세요"
          required
        />
      </div>

      {error && <p className="error-text">{error}</p>}

      <button type="submit" className="submit-button">
        로그인
      </button>
    </form>
  );
};

export default LoginForm;