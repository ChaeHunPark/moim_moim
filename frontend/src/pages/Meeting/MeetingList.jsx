import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import Swal from 'sweetalert2';
import './MeetingList.css';

const MeetingList = () => {
    const navigate = useNavigate();

    // 상태 관리
    const [meetings, setMeetings] = useState([]);
    const [categories, setCategories] = useState([]);
    const [status, setStatus] = useState('loading'); 
    
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
                // api 인스턴스 사용 (baseURL이 /api라면 '/categories'만 작성)
                const response = await api.get('/categories');
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
                // api 인스턴스 사용
                const response = await api.get('/meetings', {
                    params: { categoryId, sortBy }
                });
                
                if (response.status === 200) {
                    const data = response.data;
                    setMeetings(data);
                    setStatus(data.length === 0 ? 'empty' : 'success');
                }
            } catch (error) {
                console.error("모임 데이터 로딩 실패:", error);
                // 인터셉터에서 401/403을 처리하겠지만, 여기서도 에러 상태를 표시해줍니다.
                setStatus('error');
            }
        };
        fetchMeetings();
    }, [categoryId, sortBy]);

    // 새 모임 만들기 클릭 핸들러 (인증 체크)
    const handleCreateClick = () => {
        // 키 이름을 'accessToken'으로 통일
        const token = localStorage.getItem('accessToken'); 

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
            // 토큰이 있다면 작성 페이지로 이동
            navigate('/meeting-create');
        }
    };

    return (
        <div className="page-wrapper">
            <div className="meeting-list-container">
                <header className="list-hero-section">
                    <div className="hero-content">
                        <h1>👋 함께 성장할 동료를 찾아보세요</h1>
                        <p>원하는 카테고리의 모임을 선택하고 참여해 보세요.</p>
                    </div>
                    <button className="create-btn" onClick={handleCreateClick}>
                        <span className="plus-icon">+</span> 새 모임 만들기
                    </button>
                </header>

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
            </div>
        </div>
    );
};

export default MeetingList;