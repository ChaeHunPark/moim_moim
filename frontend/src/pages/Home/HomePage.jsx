import React from 'react';
import './HomePage.css';

const HomePage = () => {
  return (
    <main className="home-container">
      <div className="home-card">
        <div className="home-status-badge">Launching Soon 🚀</div>
        
        <h1 className="home-title">모임모임</h1>
        
        <p className="home-description">
          서로를 이해하고 존중하며 함께 성장하는 공간.<br />
          더 나은 만남의 경험을 위해 현재 정식 서비스를 준비하고 있습니다.
        </p>

        <div className="home-loading-bar">
          <div className="home-loading-progress"></div>
        </div>

        <p className="home-footer-text">
          조금만 기다려주세요. 곧 찾아뵙겠습니다!
        </p>
      </div>
    </main>
  );
};

export default HomePage;