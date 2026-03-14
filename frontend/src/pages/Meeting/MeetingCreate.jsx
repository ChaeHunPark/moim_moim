import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
// 💡 중요: 커스텀 api 인스턴스 가져오기
import api from '../../api/axios';
import './MeetingCreate.css';

const MeetingCreate = () => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

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
        tags: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const validateForm = () => {
        const now = new Date();
        const start = new Date(formData.startDate);
        const end = new Date(formData.endDate);

        if (!formData.title.trim()) return "제목을 입력해주세요.";
        if (!formData.categoryId) return "카테고리를 선택해주세요.";
        if (start < now) return "시작 시간은 현재보다 미래여야 합니다.";
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
            // 💡 데이터 가공 (백엔드 DTO 규격)
            const requestBody = {
                title: formData.title,
                categoryId: Number(formData.categoryId),
                capacity: Number(formData.capacity),
                startDate: formData.startDate,
                endDate: formData.endDate,
                description: formData.description,
            };

            // 💡 1. axios.post -> api.post 로 변경
            // 💡 2. headers 설정을 일일이 넣을 필요가 없습니다. (api 인스턴스가 자동 처리)
            const response = await api.post('/meetings', requestBody);

            if (response.status === 201 || response.status === 200) {
                alert("🎉 모임이 성공적으로 개설되었습니다!");
                // 백엔드에서 생성된 ID를 반환한다고 가정
                const newId = response.data.id;
                navigate(`/meetings/${newId}`);
            }
        } catch (error) {
            console.error("Error creating meeting:", error);
            
            // 💡 401(인증에러) 등 공통 에러는 이미 axios.js 인터셉터에서 
            // 처리하고 있으므로, 여기선 '글쓰기 실패'에 대한 특수 처리만 남깁니다.
            const status = error.response?.status;
            if (status === 400) {
                alert(`입력 값을 확인해주세요: ${error.response?.data?.message || "잘못된 요청입니다."}`);
            } else if (status !== 401) {
                // 401은 인터셉터에서 처리하므로 그 외의 에러만 알림
                alert("모임 등록 중 오류가 발생했습니다.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="create-page-wrapper">
            <div className="create-content-container">
                <main className="main-form-section">
                    <header className="create-header">
                        <h1>모임 개설하기</h1>
                    </header>

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">모임 제목</label>
                            <input
                                name="title"
                                className="form-input"
                                placeholder="어떤 모임인지 한눈에 알 수 있는 제목을 입력해주세요"
                                value={formData.title}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">카테고리</label>
                                <select name="categoryId" className="form-select" value={formData.categoryId} onChange={handleChange} required>
                                    <option value="">어떤 종류의 모임인가요?</option>
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
                            <label className="form-label">태그 (쉼표로 구분)</label>
                            <input
                                name="tags"
                                className="form-input"
                                placeholder="예: 축구, 주말, 초보환영"
                                value={formData.tags}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">모임 상세 설명</label>
                            <textarea
                                name="description"
                                className="form-textarea"
                                rows="8"
                                placeholder="모임의 목적, 장소 등을 자유롭게 적어주세요."
                                value={formData.description}
                                onChange={handleChange}
                            ></textarea>
                        </div>

                        <div className="action-area">
                            <button type="button" className="btn-cancel" onClick={() => navigate(-1)}>취소</button>
                            <button
                                type="submit"
                                className="btn-submit"
                                disabled={isLoading}
                            >
                                {isLoading ? "등록 중..." : "모임 등록하기"}
                            </button>
                        </div>
                    </form>
                </main>

                <aside className="side-info-section">
                    <div className="info-box">
                        <h3>📢 가이드</h3>
                        <ul>
                            <li>모임 시간은 신중하게 결정해 주세요.</li>
                            <li>상세 설명이 구체적일수록 참여율이 높습니다.</li>
                        </ul>
                    </div>
                </aside>
            </div>
        </div>
    );
};

export default MeetingCreate;