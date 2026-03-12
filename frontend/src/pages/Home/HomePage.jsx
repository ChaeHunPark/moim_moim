import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './HomePage.css';

const HomePage = () => {
    const [meetings, setMeetings] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchHomeMeetings = async () => {
            try {
                // 최신순으로 상위 4개 혹은 8개만 가져오기
                const response = await axios.get('/api/meetings', {
                    params: { sortBy: 'latest' } 
                });
                // 홈 화면에 맞게 상위 8개만 표시
                setMeetings(response.data.slice(0, 8));
            } catch (error) {
                console.error("데이터 로딩 실패:", error);
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
                <p>다양한 모임이 채훈님을 기다리고 있어요!</p>
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
                                onClick={() => window.location.href=`/meetings/${meeting.id}`}
                            >
                                <div className="card-image">
                                    {meeting.imageUrl ? <img src={meeting.imageUrl} alt={meeting.title} /> : "No Image"}
                                </div>
                                <div className="card-content">
                                    <div className="card-category">{meeting.categoryName}</div>
                                    <h3 className="card-title">{meeting.title}</h3>
                                    <div className="card-info">
                                        {/* 백엔드 필드명에 맞춰서 수정 (hostName, currentParticipants 등) */}
                                        <span>👤 {meeting.hostName || '작성자'}</span>
                                        <span>👥 {meeting.currentParticipants}/{meeting.capacity}</span>
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

export default HomePage;