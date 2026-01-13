import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Layout.css';

const Header = () => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // 1. 컴포넌트 마운트 시 토큰 확인
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    setIsLoggedIn(!!token); // 토큰이 있으면 true, 없으면 false
  }, []);

  // 2. 로그아웃 핸들러
  const handleLogout = () => {
    // 엣지 케이스: 로그아웃 확인 컨펌
    if (window.confirm("로그아웃 하시겠습니까?")) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setIsLoggedIn(false);
      alert("로그아웃 되었습니다. 👋");
      navigate('/');
    }
  };

  return (
    <header className="main-header">
      <div className="header-inner">
        <Link to="/" className="logo">MOIM MOIM</Link>
        
        <nav>
          <ul className="nav-menu">
            <li className="nav-item">모임 찾기</li>
            <li className="nav-item">커뮤니티</li>
            {/* 로그인했을 때만 '내 모임' 메뉴 노출 */}
            {isLoggedIn && <li className="nav-item">내 모임</li>}
          </ul>
        </nav>

        <div className="user-actions">
          {isLoggedIn ? (
            <>
              {/* 로그인 상태 UI */}
              <button className="btn-create" onClick={() => navigate('/meeting-create')}>
                모임 만들기
              </button>
              <button className="btn-login" onClick={handleLogout}>
                로그아웃
              </button>
            </>
          ) : (
            <>
              {/* 비로그인 상태 UI */}
              <button className="btn-login" onClick={() => navigate('/login')}>
                로그인
              </button>
              <button className="btn-create" onClick={() => navigate('/register')}>
                회원가입
              </button>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;