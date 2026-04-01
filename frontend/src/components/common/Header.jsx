import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../api/axios';
import './Layout.css';

const Header = () => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  
  // --- 알림 관련 상태 ---
  const [notifications, setNotifications] = useState([]); 
  const [isNotiOpen, setIsNotiOpen] = useState(false);    
  const notiRef = useRef(null); 

  // 1. 초기 로드 및 로그인 상태 확인
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const loggedIn = !!token;
    setIsLoggedIn(loggedIn);

    if (loggedIn) {
      fetchNotifications();
    }
  }, []);

  // 2. 알림 데이터 가져오기 (조회)
  const fetchNotifications = async () => {
    try {
      const response = await api.get('/notifications');
      setNotifications(response.data);
    } catch (error) {
      console.error("알림 로드 실패:", error);
    }
  };

  // 3. 외부 클릭 시 알림창 닫기
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notiRef.current && !notiRef.current.contains(e.target)) {
        setIsNotiOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // 4. 알림 클릭 시 읽음 처리 및 이동
  const handleNotiClick = async (id, url) => {
    try {
      await api.patch(`/notifications/${id}/read`);
      
      setNotifications(prev => 
        prev.map(n => n.id === id ? { ...n, isRead: true } : n)
      );
      
      setIsNotiOpen(false);
      navigate(url);
    } catch (error) {
      console.error("읽음 처리 실패:", error);
      navigate(url); 
    }
  };

// 5. 알림 삭제 처리 (confirm 창 제거)
  const handleNotiDelete = async (e, id) => {
    e.stopPropagation(); // 알림 클릭 이벤트(이동) 전파 방지

    try {
      // 💡 confirm 없이 바로 API 호출
      await api.delete(`/notifications/${id}`);
      
      // 로컬 상태에서 즉시 제거하여 사용자에게 피드백 제공
      setNotifications(prev => prev.filter(n => n.id !== id));
    } catch (error) {
      console.error("알림 삭제 실패:", error);
      // 사용자에게 최소한의 에러 알림은 주는 것이 좋습니다.
      alert("알림 삭제에 실패했습니다.");
    }
  };

  // 읽지 않은 알림 개수 계산
  const unreadCount = notifications.filter(n => !n.isRead).length;

  // 6. 로그아웃 처리
  const handleLogout = async () => {
    if (!window.confirm("로그아웃 하시겠습니까?")) return;
    try {
      await api.post('/logout'); 
    } catch (error) {
      console.error("로그아웃 실패:", error);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setIsLoggedIn(false);
      setNotifications([]); 
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
            {isLoggedIn && (
              <li className="nav-item" onClick={() => navigate('/mypage')}>내 모임</li>
            )}
          </ul>
        </nav>

        <div className="user-actions">
          {isLoggedIn ? (
            <>
              <div className="noti-container" ref={notiRef}>
                <button 
                  className={`btn-noti ${isNotiOpen ? 'active' : ''}`} 
                  onClick={() => setIsNotiOpen(!isNotiOpen)}
                >
                  <span className="noti-icon">🔔</span>
                  {unreadCount > 0 && <span className="noti-badge">{unreadCount}</span>}
                </button>

                {isNotiOpen && (
                  <div className="noti-dropdown">
                    <div className="noti-header">최근 알림</div>
                    <div className="noti-list">
                      {notifications.length > 0 ? (
                        notifications.map(n => (
                          <div 
                            key={n.id} 
                            className={`noti-item ${n.isRead ? '' : 'unread'}`}
                            onClick={() => handleNotiClick(n.id, n.url)}
                          >
                            <div className="noti-content-wrapper">
                              <p>{n.content}</p>
                              <span className="noti-time">
                                {n.createdAt.split('T')[0]} 
                              </span>
                            </div>
                            {/* 💡 삭제 버튼 추가 */}
                            <button 
                              className="btn-noti-delete"
                              onClick={(e) => handleNotiDelete(e, n.id)}
                            >
                              &times;
                            </button>
                          </div>
                        ))
                      ) : (
                        <div className="noti-empty">새로운 알림이 없습니다.</div>
                      )}
                    </div>
                    <div className="noti-footer" onClick={() => navigate('/mypage')}>
                      전체 보기
                    </div>
                  </div>
                )}
              </div>

              <button className="btn-mypage" onClick={() => navigate('/mypage')}>마이페이지</button>
              <button className="btn-create" onClick={() => navigate('/meeting-create')}>모임 만들기</button>
              <button className="btn-logout-text" onClick={handleLogout}>로그아웃</button>
            </>
          ) : (
            <>
              <button className="btn-login" onClick={() => navigate('/login')}>로그인</button>
              <button className="btn-create" onClick={() => navigate('/register')}>회원가입</button>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;