import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
// 💡 경로 확인: 프로젝트 구조에 맞춰 '../api/axios' 또는 '../../api/axios'로 설정
import api from '../../api/axios'; 
import ParticipationForm from '../../components/meeting/ParticipationForm';
import './MeetingDetail.css';

const formatDateTime = (dateString) => {
    const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'short',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleTimeString('ko-KR', options);
};

const MeetingDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [meeting, setMeeting] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const formRef = useRef(null);

    useEffect(() => {
        const fetchMeetingDetail = async () => {
            try {
                // 💡 api 인스턴스 사용
                const res = await api.get(`/meetings/${id}`);
                setMeeting(res.data);
            } catch (err) {
                console.error("상세 데이터 로딩 실패:", err);
                // 💡 404나 서버 에러 시 홈으로 리다이렉트
                // 인터셉터에서 401을 처리하겠지만, 페이지 자체가 없는 경우 등에 대비합니다.
                navigate('/', { replace: true });
            } finally {
                setLoading(false);
            }
        };

        fetchMeetingDetail();
    }, [id, navigate]);

    const handleApplyClick = () => {
        // 💡 참여 신청 전 로그인이 되어있는지 확인하는 로직을 추가하면 더 좋습니다.
        const token = localStorage.getItem('accessToken');
        if (!token) {
            alert("참여 신청을 하려면 로그인이 필요합니다.");
            navigate('/login');
            return;
        }

        setShowForm(true);
        setTimeout(() => {
            formRef.current?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
    };

    if (loading) return <div className="loading-container">데이터를 불러오는 중입니다...</div>;
    if (!meeting) return null;

    const isFull = meeting.currentParticipants >= meeting.capacity;

    return (
        <div className="meeting-page-bg">
            <div className="meeting-container">
                {/* 헤더 섹션 */}
                <div className="meeting-header">
                    <span className="meeting-category-tag">{meeting.categoryName}</span>
                    <h1 className="meeting-title">{meeting.title}</h1>
                    <div className="meeting-meta">
                        <span>작성자: <strong>{meeting.creatorEmail}</strong></span>
                        <span className="meta-divider">|</span>
                        <span>작성일: {new Date(meeting.createdAt).toLocaleDateString()}</span>
                    </div>
                </div>

                {/* 주요 정보 보드 */}
                <div className="meeting-stats-board">
                    <div className="stat-card">
                        <label className="stat-label">모집 현황</label>
                        <div className="stat-value">
                            <span className={isFull ? "text-full" : "text-open"}>
                                {meeting.currentParticipants}
                            </span>
                            <span className="text-total"> / {meeting.capacity} 명</span>
                        </div>
                    </div>

                    <div className="stat-card">
                        <label className="stat-label">조회수</label>
                        <div className="stat-value">
                            <span>{meeting.viewCount}</span>
                            <span className="unit"> 회</span>
                        </div>
                    </div>

                    <div className="stat-card full-width">
                        <label className="stat-label">모임 일정</label>
                        <div className="stat-value date-range">
                            <span className="date-text">{formatDateTime(meeting.startDate)}</span>
                            <span className="date-separator">~</span>
                            <span className="date-text">{formatDateTime(meeting.endDate)}</span>
                        </div>
                    </div>
                </div>

                {/* 상세 설명 섹션 */}
                <div className="meeting-content-section">
                    <h3 className="content-label">모임 상세 설명</h3>
                    <div className="meeting-content">
                        {meeting.description}
                    </div>
                </div>

                {/* 하단 버튼 섹션 */}
                <div className="meeting-footer">
                    {!showForm ? (
                        <button
                            className={`btn-apply-main ${isFull ? "disabled" : ""}`}
                            disabled={isFull}
                            onClick={handleApplyClick}
                        >
                            {isFull ? "모집이 완료되었습니다" : "이 모임에 참여하기"}
                        </button>
                    ) : (
                        <div className="apply-notice-box">아래 신청서를 작성해주세요 👇</div>
                    )}
                </div>

                {/* 참여 신청 폼 */}
                {showForm && (
                    <div ref={formRef} className="form-section-wrapper">
                        {/* 💡 참여 신청 폼 내부의 API 호출도 api 인스턴스를 쓰고 있는지 확인하세요! */}
                        <ParticipationForm meetingPostId={id} />
                        <button className="btn-cancel-action" onClick={() => setShowForm(false)}>
                            신청 취소하기
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MeetingDetail;