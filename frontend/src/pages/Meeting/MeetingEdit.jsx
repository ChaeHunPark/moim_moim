import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import './MeetingCreate.css'; // 💡 Create와 동일한 CSS 사용

const MeetingEdit = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [isFetching, setIsFetching] = useState(true);

    const CATEGORIES = [
        { id: 1, name: '스포츠/운동', icon: '🏃‍♂️' },
        { id: 2, name: '자기계발/공부', icon: '📚' },
        { id: 3, name: '액티비티/게임', icon: '🎲' },
        { id: 4, name: '음식/맛집', icon: '☕' },
        { id: 5, name: '문화/예술', icon: '🎨' },
        { id: 6, name: '친목/사교', icon: '🤝' },
        { id: 7, name: '기술/IT', icon: '💻' },
    ];

    const [formData, setFormData] = useState({
        title: '',
        categoryId: '',
        capacity: 2,
        startDate: '',
        endDate: '',
        description: '',
    });

    // 💡 기존 데이터 불러오기
    useEffect(() => {
        const fetchMeeting = async () => {
            try {
                const res = await api.get(`/meetings/${id}`);

                const data = res.data;
                
                // datetime-local 입력을 위해 초 단위(.000) 제외 처리
                const formatDateTime = (str) => str ? str.substring(0, 16) : '';

                setFormData({
                    title: data.title,
                    categoryId: data.categoryId,
                    capacity: data.capacity,
                    startDate: formatDateTime(data.startDate),
                    endDate: formatDateTime(data.endDate),
                    description: data.description,
                });
            } catch (err) {
                console.error("데이터 로드 실패:", err);
                alert("게시글을 불러올 수 없습니다.");
                navigate(-1);
            } finally {
                setIsFetching(false);
            }
        };
        fetchMeeting();
    }, [id, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const validateForm = () => {
        const start = new Date(formData.startDate);
        const end = new Date(formData.endDate);

        if (!formData.title.trim()) return "제목을 입력해주세요.";
        if (!formData.categoryId) return "카테고리를 선택해주세요.";
        if (start >= end) return "종료 시간은 시작 시간보다 이후여야 합니다.";
        return null;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const errorMessage = validateForm();
        if (errorMessage) {
            alert(errorMessage);
            return;
        }

        setIsLoading(true);
        try {
            const requestBody = {
                ...formData,
                categoryId: Number(formData.categoryId),
                capacity: Number(formData.capacity),
            };

            // 💡 PUT 요청으로 수정
            await api.put(`/meetings/${id}`, requestBody);
            alert("✅ 모임 정보가 수정되었습니다.");
            navigate(`/meetings/${id}`);
        } catch (error) {
            console.error("Update error:", error);
            const status = error.response?.status;
            if (status === 403) {
                alert("수정 권한이 없습니다.");
            } else {
                alert(error.response?.data?.message || "수정 중 오류가 발생했습니다.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    if (isFetching) return <div className="loading-text">데이터를 불러오는 중...</div>;

    return (
        <div className="create-page-wrapper">
            <div className="create-content-container">
                <main className="main-form-section">
                    <header className="create-header">
                        <h1>모임 정보 수정</h1>
                    </header>

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">모임 제목</label>
                            <input
                                name="title"
                                className="form-input"
                                value={formData.title}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">카테고리</label>
                                <select name="categoryId" className="form-select" value={formData.categoryId} onChange={handleChange} required>
                                    <option value="">카테고리 선택</option>
                                    {CATEGORIES.map(cat => (
                                        <option key={cat.id} value={cat.id}>
                                            {cat.icon} {cat.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label className="form-label">모집 인원 (최대)</label>
                                <input
                                    type="number"
                                    name="capacity"
                                    className="form-input"
                                    min="2"
                                    value={formData.capacity}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">시작 일시</label>
                                <input
                                    type="datetime-local"
                                    name="startDate"
                                    className="form-input"
                                    value={formData.startDate}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">종료 일시</label>
                                <input
                                    type="datetime-local"
                                    name="endDate"
                                    className="form-input"
                                    value={formData.endDate}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">모임 상세 설명</label>
                            <textarea
                                name="description"
                                className="form-textarea"
                                rows="10"
                                value={formData.description}
                                onChange={handleChange}
                                required
                            ></textarea>
                        </div>

                        <div className="action-area">
                            <button type="button" className="btn-cancel" onClick={() => navigate(-1)}>취소</button>
                            <button
                                type="submit"
                                className="btn-submit"
                                disabled={isLoading}
                            >
                                {isLoading ? "수정 중..." : "수정 완료"}
                            </button>
                        </div>
                    </form>
                </main>

                <aside className="side-info-section">
                    <div className="info-box">
                        <h3>💡 수정 안내</h3>
                        <ul>
                            <li>이미 참여한 인원이 있을 경우 시간 변경에 유의하세요.</li>
                            <li>수정 완료 시 상세 페이지로 이동합니다.</li>
                        </ul>
                    </div>
                </aside>
            </div>
        </div>
    );
};

export default MeetingEdit;