import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios'; // axios 설치 필요: npm install axios
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
            // 1. 토큰 가져오기 (로그인 시 저장한 키값 확인 필요)
            const token = localStorage.getItem('accessToken'); 
            
            if (!token) {
                alert("로그인이 필요합니다.");
                navigate('/login');
                return;
            }

            // 2. 백엔드 DTO 규격에 맞게 데이터 가공
            const requestBody = {
                title: formData.title,
                categoryId: Number(formData.categoryId), // Long 타입 대응
                capacity: Number(formData.capacity),     // int 타입 대응
                startDate: formData.startDate,           // ISO 8601 포맷
                endDate: formData.endDate,
                description: formData.description,
                // 태그는 현재 백엔드 DTO에 없으므로 우선 제외하거나 필요시 추가
            };

            // 3. 실제 API 호출
            const response = await axios.post('/api/meetings', requestBody, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.status === 201 || response.status === 200) {
                alert("🎉 모임이 성공적으로 개설되었습니다!");
                navigate(`/meetings/${response.data}`); // 상세 페이지로 이동 (ID 반환 가정)
            }
        } catch (error) {
            // 4. 에러 상황 처리 (Edge Cases)
            console.error("Error creating meeting:", error);
            const status = error.response?.status;
            
            if (status === 401) {
                alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                navigate('/login');
            } else if (status === 400) {
                alert(`입력 값을 확인해주세요: ${error.response?.data?.message || "잘못된 요청입니다."}`);
            } else {
                alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    // ... (return 부분은 이전과 동일하므로 생

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
                                <select name="categoryId" className="form-select" onChange={handleChange} required>
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