import api from "./axios";

/**
 * 모임 참여 신청 API
 * @param {Object} data - { meetingPostId, joinReason }
 */
export const applyMeeting = async (data) => {
    try {
        const response = await api.post('/participation/apply', data);
        return response.data;
    } catch (error) {
        // 에러를 상위 컴포넌트로 던져서 처리하게 함
        throw error;
    }
}