import React from 'react';
import './HomePage.css';

const HomePage = () => {
  // 나중에 API로 받아올 더미 데이터
  const dummyMeetings = [
    { id: 1, title: '🏃 아침 조깅 하실 분!', category: '운동', host: '러너킴', members: 3, capacity: 5 },
    { id: 2, title: '📚 퇴근 후 코딩 스터디', category: '자기계발', host: '데브최', members: 2, capacity: 4 },
    { id: 3, title: '☕️ 성수동 핫플 카페 투어', category: '맛집', host: '카페인', members: 1, capacity: 6 },
    { id: 4, title: '🎮 보드게임 하실 파티원!', category: '취미', host: '게임왕', members: 4, capacity: 4 },
  ];

  return (
    <div className="home-wrapper">
      {/* 히어로 섹션 */}
      <section className="hero-section">
        <div className="home-status-badge">취향 기반 모임 플랫폼</div>
        <h1 className="home-title">지금 내 주변에서<br />새로운 모임을 시작하세요</h1>
        <div className="home-loading-bar">
          <div className="home-loading-progress"></div>
        </div>
        <p className="home-description">함께하면 더 즐거운 일상, 모임모임에서 만들어보세요.</p>
      </section>

      {/* 리스트 섹션 */}
      <section className="meeting-list-section">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>🔥 지금 핫한 모임</h2>
          <span style={{ color: '#1c7ed6', cursor: 'pointer', fontWeight: 600 }}>전체보기</span>
        </div>

        <div className="meeting-grid">
          {dummyMeetings.map((meeting) => (
            <div key={meeting.id} className="meeting-card">
              <div className="card-image">No Image</div>
              <div className="card-content">
                <div className="card-category">{meeting.category}</div>
                <h3 className="card-title">{meeting.title}</h3>
                <div className="card-info">
                  <span>👤 {meeting.host}</span>
                  <span>👥 {meeting.members}/{meeting.capacity}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default HomePage;