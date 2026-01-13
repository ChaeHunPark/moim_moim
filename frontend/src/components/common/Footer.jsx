import React from 'react';
import './Layout.css';

const Footer = () => {
  return (
    <footer className="main-footer">
      <div className="footer-inner">
        <div className="footer-info">
          <h2>MOIM MOIM</h2>
          <p style={{ color: '#868e96', lineHeight: '1.6' }}>
            관심사 기반으로 만나는 우리 동네 모임 커뮤니티.<br />
            당신의 새로운 인연과 취미를 모임모임에서 찾아보세요.
          </p>
        </div>
        
        <div className="footer-links">
          <h4>서비스</h4>
          <ul>
            <li><a href="#">이용약관</a></li>
            <li><a href="#">개인정보처리방침</a></li>
            <li><a href="#">공지사항</a></li>
          </ul>
        </div>

        <div className="footer-links">
          <h4>고객지원</h4>
          <ul>
            <li><a href="#">자주 묻는 질문</a></li>
            <li><a href="#">1:1 문의</a></li>
            <li><a href="#">카테고리 제안</a></li>
          </ul>
        </div>
      </div>
      <div className="footer-bottom">
        &copy; 2026 MOIM MOIM. All rights reserved.
      </div>
    </footer>
  );
};

export default Footer;