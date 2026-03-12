import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Swal from 'sweetalert2';
import './MeetingList.css';

const MeetingList = () => {
    const navigate = useNavigate();

    // 상태 관리
    const [meetings, setMeetings] = useState([]);
    const [categories, setCategories] = useState([]);
    const [status, setStatus] = useState('loading'); // 'loading' | 'success' | 'error' | 'empty'
    
    // 필터 및 정렬 상태
    const [sortBy, setSortBy] = useState('latest');
    const [categoryId, setCategoryId] = useState(null);

    // 정렬 옵션 정의
    const sortOptions = [
        { id: 'latest', label: '최신순' },
        { id: 'popular', label: '인기순' },
        { id: 'closing', label: '마감임박순' }
    ];

    // 1. 실제 카테고리 목록 불러오기
    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const response = await axios.get('/api/categories');
                if (response.status === 200) {
                    setCategories(response.data);
                }
            } catch (error) {
                console.error("카테고리 로딩 실패:", error);
            }
        };
        fetchCategories();
    }, []);

    // 2. 모임 데이터 불러오기 (필터 변경 시)
    useEffect(() => {
        const fetchMeetings = async () => {
            setStatus('loading');
            try {
                const response = await axios.get('/api/meetings', {
                    params: { categoryId, sortBy }
                });
                
                if (response.status === 200) {
                    const data = response.data;
                    setMeetings(data);
                    setStatus(data.length === 0 ? 'empty' : 'success');
                }
            } catch (error) {
                console.error("모임 데이터 로딩 실패:", error);
                setStatus('error');
            }
        };
        fetchMeetings();
    }, [categoryId, sortBy]);

    // 새 모임 만들기 클릭 핸들러 (인증 체크)
    const handleCreateClick = () => {
        const token = localStorage.getItem('token'); 

        if (!token) {
            Swal.fire({
                title: '로그인이 필요합니다',
                text: '모임을 만드려면 로그인이 필요해요. 로그인하시겠습니까?',
                icon: 'info',
                showCancelButton: true,
                confirmButtonColor: '#007bff',
                cancelButtonColor: '#868e96',
                confirmButtonText: '로그인하러 가기',
                cancelButtonText: '나중에 할래요',
                customClass: {
                    popup: 'custom-swal-popup'
                }
            }).then((result) => {
                if (result.isConfirmed) {
                    navigate('/login');
                }
            });
        } else {
            navigate('/meetings/create');
        }
    };

    return (
        <div className="page-wrapper">
            <div className="meeting-list-container">
                {/* 헤더 섹션 */}
                <header className="list-hero-section">
                    <div className="hero-content">
                        <h1>👋 함께 성장할 동료를 찾아보세요</h1>
                        <p>원하는 카테고리의 모임을 선택하고 참여해 보세요.</p>
                    </div>
                    <button className="create-btn" onClick={handleCreateClick}>
                        <span className="plus-icon">+</span> 새 모임 만들기
                    </button>
                </header>

                {/* 필터 영역 */}
                <div className="filter-section">
                    <nav className="category-nav">
                        <button 
                            className={categoryId === null ? 'active' : ''} 
                            onClick={() => setCategoryId(null)}
                        >
                            전체
                        </button>
                        {categories.map(cat => (
                            <button 
                                key={cat.id}
                                className={categoryId === cat.id ? 'active' : ''}
                                onClick={() => setCategoryId(cat.id)}
                            >
                                {cat.name}
                            </button>
                        ))}
                    </nav>

                    <div className="sort-tabs">
                        {sortOptions.map(option => (
                            <span 
                                key={option.id}
                                className={sortBy === option.id ? 'active' : ''}
                                onClick={() => setSortBy(option.id)}
                            >
                                {option.label}
                            </span>
                        ))}
                    </div>
                </div>

                {/* 리스트 렌더링 영역 */}
                {status === 'loading' && <div className="status-message">모임을 불러오는 중...</div>}
                
                {status === 'error' && <div className="status-message">데이터 로드 중 에러가 발생했습니다.</div>}
                
                {status === 'empty' && <div className="status-message">해당 카테고리에 등록된 모임이 없습니다.</div>}

                {status === 'success' && (
                    <div className="meeting-grid">
                        {meetings.map((meeting) => (
                            <div 
                                key={meeting.id} 
                                className="meeting-card" 
                                onClick={() => navigate(`/meetings/${meeting.id}`)}
                            >
                                <div className="card-image">
                                    {meeting.imageUrl ? <img src={meeting.imageUrl} alt={meeting.title} /> : "No Image"}
                                    {meeting.capacity - meeting.currentParticipants <= 2 && (
                                        <span className="urgent-badge">마감임박</span>
                                    )}
                                </div>
                                
                                <div className="card-content">
                                    <div className="card-category">{meeting.categoryName}</div>
                                    <h3 className="card-title">{meeting.title}</h3>
                                    <div className="card-info">
                                        <span>👤 {meeting.hostName}</span>
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
            </div>
        </div>
    );
};

export default MeetingList;