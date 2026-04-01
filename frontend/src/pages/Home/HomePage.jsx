import React, { useState, useEffect } from 'react';
// 💡 중요: 커스텀 api 인스턴스를 가져옵니다. 
// (파일 위치에 따라 '../api/axios' 또는 './api/axios'로 조정하세요)
import api from '../../api/axios';
import { useNavigate } from 'react-router-dom'; // window.location 대신 navigate 사용 추천
import './HomePage.css';

const HomePage = () => {
    const [meetings, setMeetings] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate(); // 네비게이트 훅 사용

    useEffect(() => {
        const fetchHomeMeetings = async () => {
            try {
                const response = await api.get('/meetings', {
                    params: { sortBy: 'latest' } 
                });
                
                // 홈 화면에 맞게 상위 8개만 표시
                setMeetings(response.data.slice(0, 8));
            } catch (error) {
                console.error("데이터 로딩 실패:", error);
                // 💡 만약 토큰이 변조되었다면 여기서 인터셉터가 401을 잡아 
                // 알아서 로그아웃 처리를 해줄 겁니다.
            } finally {
                setLoading(false);
            }
        };

        fetchHomeMeetings();
    }, []);

    return (
        <div className="home-wrapper">
            <header className="hero-section">
                <h1>👋 함께 성장할 동료를 찾아보세요</h1>
                {/* 💡 유저 정보가 있다면 이름을 동적으로 띄우는 것도 좋겠네요! */}
                <p>다양한 모임이 기다리고 있어요!</p>
            </header>

            <section className="meeting-list-section">
                <h2>🚀 따끈따끈한 새 모임</h2>
                
                {loading ? (
                    <div className="loading">불러오는 중...</div>
                ) : (
                    <div className="meeting-grid">
                        {meetings.map((meeting) => (
                            <div 
                                key={meeting.id} 
                                className="meeting-card" 
                                // 💡 window.location.href 대신 navigate를 쓰는 것이 React 방식에 더 적합합니다.
                                onClick={() => navigate(`/meetings/${meeting.id}`)}
                            >
                                <div className="card-image">
                                    {meeting.imageUrl ? (
                                        <img src={meeting.imageUrl} alt={meeting.title} />
                                    ) : (
                                        <div className="no-image">No Image</div>
                                    )}
                                </div>
                                <div className="card-content">
                                    <div className="card-category">{meeting.categoryName}</div>
                                    <h3 className="card-title">{meeting.title}</h3>
                                    <div className="card-info">
                                        <span>👤 {meeting.creatorNickname || '작성자'}</span>
                                        <span>👥 {meeting.currentParticipants}/{meeting.capacity}</span>
                                    </div>
                                    <div className="card-sub-info">
                                        <span>📅 {new Date(meeting.startDate).toLocaleDateString()}</span>
                                        <span>👁️ {meeting.viewCount}</span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
};

export default HomePage