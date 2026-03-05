import React, { useState } from 'react';
import { applyMeeting } from '../../api/participation'
import './ParticipationForm.css';

const ParticipationForm = ({ meetingPostId }) => {
    const [joinReason, setJoinReason] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            await applyMeeting({ meetingPostId, joinReason });
            alert('참여 신청이 성공적으로 접수되었습니다! ✨');
            setJoinReason('');
        } catch (error) {
            const errorMsg = error.response?.data?.message || '신청 중 오류가 발생했습니다.';
            alert(errorMsg);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="participation-wrapper">
            <h2 className="participation-title">모임 참여하기</h2>
            <p className="participation-subtitle">
                주최자에게 전달할 참여 동기와 각오를 자유롭게 적어주세요.
            </p>
            
            <form className="participation-form" onSubmit={handleSubmit}>
                <textarea 
                    className="participation-textarea"
                    value={joinReason}
                    onChange={(e) => setJoinReason(e.target.value)}
                    required
                />
                <button 
                    type="submit" 
                    className="submit-button"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? '신청 중...' : '신청서 제출하기'}
                </button>
            </form>
        </div>
    );
};

export default ParticipationForm;