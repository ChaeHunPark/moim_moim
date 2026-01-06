import React from 'react';
import RegisterForm from '../../components/register/RegisterForm';

const RegisterPage = () => {
  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', // 세로 중앙
      justifyContent: 'center', // 가로 중앙
      padding: '20px',
      backgroundColor: '#f8f9fa' 
    }}>
      <RegisterForm />
    </div>
  );
};

export default RegisterPage;