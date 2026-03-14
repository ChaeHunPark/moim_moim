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
  const handleLogout = async () => {
      if (!window.confirm("로그아웃 하시겠습니까?")) return;

      try {
        // 공통 api 인스턴스를 사용하여 /logout 호출
        // 인터셉터에서 토큰을 자동으로 넣어준다면 두 번째 인자는 비워둬도 됩니다.
        await api.post('/logout'); 
        
      } catch (error) {
        // 서버에서 이미 토큰이 만료되었거나 에러가 나도 클라이언트 정리는 강행
        console.error("로그아웃 API 호출 실패:", error);
      } finally {
        // 저장소 정리 및 상태 업데이트
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
            <li className="nav-item" onClick={() => navigate('/meetings')}>모임 찾기</li>
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