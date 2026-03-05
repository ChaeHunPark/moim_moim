import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from '../../api/axios';
import ParticipationForm from '../../components/meeting/ParticipationForm';
import './MeetingDetail.css';

const formatDateTime = (dateString) => {
    const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'short', // '월', '화' 등 요일 표시
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
        axios.get(`/meetings/${id}`)
            .then(res => {
                setMeeting(res.data);
                setLoading(false);
            })
            .catch(err => {
                // 3. 에러(404 등) 발생 시 아무 창 안 띄우고 바로 홈으로 이동
                navigate('/', { replace: true });
            });
    }, [id, navigate]);

    const handleApplyClick = () => {
        setShowForm(true);
        setTimeout(() => {
            formRef.current?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
    };

    if (loading) return <div className="loading-container">데이터를 불러오는 중입니다...</div>;
    if (!meeting) return null;

    const isFull = meeting.currentParticipants >= meeting.capacity;

    return (
        <div className="meeting-page-bg"> {/* 배경색을 위한 래퍼 */}
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

                {/* 상세 설명 섹션 (하얀 배경 안에 구분선으로 분리) */}
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