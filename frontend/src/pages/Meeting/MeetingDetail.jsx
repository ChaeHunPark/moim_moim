import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './MeetingDetail.css'; // 일반 CSS 파일 임포트

const MeetingDetail = () => {
    const { id } = useParams();
    const [meeting, setMeeting] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        axios.get(`/api/meetings/${id}`)
            .then(res => {
                setMeeting(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, [id]);

    if (loading) return <div className="meeting-container">데이터를 불러오는 중입니다...</div>;
    if (!meeting) return <div className="meeting-container">존재하지 않는 모임입니다.</div>;

    const isFull = meeting.currentParticipants >= meeting.capacity;

    return (
        <div className="meeting-container">
            <div className="meeting-header">
                <span className="meeting-category">{meeting.categoryName}</span>
                <h1 className="meeting-title">{meeting.title}</h1>
                <div className="meeting-meta">
                    <span>작성자: {meeting.creatorEmail}</span>
                    <span>작성일: {new Date(meeting.createdAt).toLocaleDateString()}</span>
                </div>
            </div>

            <div className="meeting-info-board">
                <div className="info-box">
                    <label>정원 상태</label>
                    <span>{meeting.currentParticipants} / {meeting.capacity} 명</span>
                </div>
                <div className="info-box">
                    <label>조회수</label>
                    <span>{meeting.viewCount}회</span>
                </div>
                <div className="info-box full">
                    <label>일시</label>
                    <span>
                        {new Date(meeting.startDate).toLocaleString()} ~ {new Date(meeting.endDate).toLocaleString()}
                    </span>
                </div>
            </div>

            <div className="meeting-content">
                {meeting.description}
            </div>

            <div className="meeting-footer">
                <button className="btn-apply" disabled={isFull}>
                    {isFull ? "모집이 완료되었습니다" : "이 모임에 참여하기"}
                </button>
            </div>
        </div>
    );
};

export default MeetingDetail;