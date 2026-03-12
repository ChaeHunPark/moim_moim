import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { REGIONS } from '../../constants/regions';
import './RegisterForm.css';

const RegisterForm = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        nickname: '',
        age: '',
        region_id: '',
        bio: ''
    });

    const [errors, setErrors] = useState({});

    // 실시간 유효성 검사
    const validateField = (name, value) => {
        let error = '';
        if (name === 'email') {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!value) error = '이메일을 입력해주세요.';
            else if (!emailRegex.test(value)) error = '올바른 형식이 아닙니다.';
        }
        if (name === 'password') {
            if (!value) error = '비밀번호를 입력해주세요.';
            else if (value.length < 8) error = '8자 이상 입력해주세요.';
        }
        if (name === 'nickname') {
            if (!value) error = '닉네임을 입력해주세요.';
        }
        if (name === 'age') {
            if (!value || Number(value) < 1) error = '정확한 나이를 입력해주세요.';
        }
        if (name === 'region_id' && !value) {
            error = '지역을 선택해주세요.';
        }

        setErrors(prev => ({ ...prev, [name]: error }));
        return error;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    };

    const handleBlur = (e) => {
        const { name, value } = e.target;
        validateField(name, value);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // 전체 필드 검사
        const newErrors = {};
        Object.keys(formData).forEach(key => {
            const error = validateField(key, formData[key]);
            if (error) newErrors[key] = error;
        });

        if (Object.keys(newErrors).length === 0) {
            try {
                const response = await api.post('/auth/register', formData);

                if (response.status === 200 || response.status === 201) {
                    alert('가입을 축하합니다! 🎉');
                    navigate('/login');
                }
            } catch (error) {
                const msg = error.response?.data?.message || '서버 통신 에러';
                alert(msg);
            }
        } else {
            setErrors(newErrors);
        }
    };

    return (
        <div className="register-container">
            {/* 🎯 로고 클릭 시 홈으로 이동 */}
            <h1 className="register-logo-title" onClick={() => navigate('/')}>
                모임모임
            </h1>
            <p className="register-subtitle">함께 성장할 동료를 만나보세요 😊</p>
            
            <form className="register-form" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>이메일</label>
                    <input 
                        type="email" name="email" placeholder="example@moim.com"
                        value={formData.email} onChange={handleChange} onBlur={handleBlur}
                        className={errors.email ? 'input-error' : ''}
                    />
                    {errors.email && <span className="error-text">{errors.email}</span>}
                </div>

                <div className="form-group">
                    <label>비밀번호</label>
                    <input 
                        type="password" name="password" placeholder="8자 이상"
                        value={formData.password} onChange={handleChange} onBlur={handleBlur}
                        className={errors.password ? 'input-error' : ''}
                    />
                    {errors.password && <span className="error-text">{errors.password}</span>}
                </div>

                <div className="form-row">
                    <div className="form-group flex-2">
                        <label>닉네임</label>
                        <input 
                            type="text" name="nickname" placeholder="닉네임"
                            value={formData.nickname} onChange={handleChange} onBlur={handleBlur}
                            className={errors.nickname ? 'input-error' : ''}
                        />
                        {errors.nickname && <span className="error-text">{errors.nickname}</span>}
                    </div>
                    <div className="form-group flex-1">
                        <label>나이</label>
                        <input 
                            type="number" name="age" placeholder="20"
                            value={formData.age} onChange={handleChange} onBlur={handleBlur}
                            className={errors.age ? 'input-error' : ''}
                        />
                        {errors.age && <span className="error-text">{errors.age}</span>}
                    </div>
                </div>

                <div className="form-group">
                    <label>활동 지역</label>
                    <select 
                        name="region_id" value={formData.region_id} 
                        onChange={handleChange} onBlur={handleBlur}
                        className={errors.region_id ? 'input-error' : ''}
                    >
                        <option value="">지역 선택</option>
                        {REGIONS.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
                    </select>
                    {errors.region_id && <span className="error-text">{errors.region_id}</span>}
                </div>

                <div className="form-group">
                    <label>자기소개 (선택)</label>
                    <textarea 
                        name="bio" placeholder="관심사를 적어주세요!"
                        value={formData.bio} onChange={handleChange}
                    />
                </div>

                <button type="submit" className="submit-button">가입하기</button>
            </form>

            <div className="register-footer">
                이미 회원이신가요? <span onClick={() => navigate('/login')} className="link-text">로그인</span>
            </div>
        </div>
    );
};

export default RegisterForm;