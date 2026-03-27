import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../api/axios'; // 💡 공통 api 인스턴스 임포트 확인
import './Layout.css';

const Header = () => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    setIsLoggedIn(!!token);
  }, []);

  const handleLogout = async () => {
    if (!window.confirm("로그아웃 하시겠습니까?")) return;

    try {
      await api.post('/logout'); 
    } catch (error) {
      console.error("로그아웃 API 호출 실패:", error);
    } finally {
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
            {/* 💡 '내 모임' 클릭 시 마이페이지로 이동 */}
            {isLoggedIn && (
              <li className="nav-item" onClick={() => navigate('/mypage')}>
                내 모임
              </li>
            )}
          </ul>
        </nav>

        <div className="user-actions">
          {isLoggedIn ? (
            <>
              {/* 💡 마이페이지 버튼 추가 (아이콘이나 텍스트로) */}
              <button className="btn-mypage" onClick={() => navigate('/mypage')}>
                마이페이지
              </button>
              <button className="btn-create" onClick={() => navigate('/meeting-create')}>
                모임 만들기
              </button>
              <button className="btn-logout-text" onClick={handleLogout}>
                로그아웃
              </button>
            </>
          ) : (
            <>
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