import React, { useState } from 'react';
import { REGIONS } from '../../constants/regions';
import './RegisterForm.css';

const RegisterForm = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    nickname: '',
    age: '',
    region_id: '',
    bio: ''
  });

  const [errors, setErrors] = useState({});

  // 유효성 검사 함수
  const validateField = (name, value) => {
    let error = '';
    if (name === 'email') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!value) error = '이메일을 입력해주세요.';
      else if (!emailRegex.test(value)) error = '형식이 올바르지 않습니다.';
    }
    if (name === 'password') {
      if (!value) error = '비밀번호를 입력해주세요.';
      else if (value.length < 8) error = '8자 이상 입력해주세요.';
    }
    if (name === 'nickname') {
      if (!value) error = '닉네임을 입력해주세요.';
      else if (value.length < 2) error = '2자 이상 입력해주세요.';
    }
    if (name === 'age') {
      if (!value) error = '나이를 입력해주세요.';
      else if (Number(value) < 1) error = '정확한 나이를 입력해주세요.';
    }
    if (name === 'region_id') {
      if (!value) error = '지역을 선택해주세요.';
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

  const handleSubmit = (e) => {
    e.preventDefault();
    const newErrors = {};
    Object.keys(formData).forEach(key => {
      const error = validateField(key, formData[key]);
      if (error) newErrors[key] = error;
    });

    if (Object.keys(newErrors).length === 0) {
      console.log('제출 데이터:', formData);
      alert('가입을 축하합니다! 🎉');
    } else {
      setErrors(newErrors);
    }
  };

  return (
    <div className="register-container">
      <h2 className="register-title">모임모임 회원가입</h2>
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
            type="password" name="password" placeholder="8자 이상 입력"
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
            name="bio" placeholder="관심사를 공유해보세요!"
            value={formData.bio} onChange={handleChange}
          />
        </div>

        <button type="submit" className="submit-button">가입하기</button>
      </form>
    </div>
  );
};

export default RegisterForm;