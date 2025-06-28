import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import { register } from '../services/authService';
import { useAuth } from '../context/AuthContext';

const RegisterPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const { setLoggedIn } = useAuth();

  const handleRegister = async () => {
    if (!username || !email || !password || !confirmPassword) {
      setError('All fields are required');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    try {
      const res = await register({
        username,
        email,
        password,
        confirmPassword,
      });
      localStorage.setItem('auth_token', res.token);
      localStorage.setItem('user_id', res.userId.toString());
      localStorage.setItem('username', res.username);
      setLoggedIn(true);
      navigate('/chat');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-900">
      <div className="w-full max-w-md bg-white dark:bg-gray-800 p-8 rounded-xl shadow-md space-y-5">
        <h2 className="text-2xl font-semibold text-center">Register</h2>

        <Input value={username} onChange={setUsername} placeholder="Username" />
        <Input value={email} onChange={setEmail} placeholder="Email" />
        <Input type="password" value={password} onChange={setPassword} placeholder="Password" />
        <Input type="password" value={confirmPassword} onChange={setConfirmPassword} placeholder="Confirm Password" />

        {error && <p className="text-red-500 text-sm">{error}</p>}

        <Button onClick={handleRegister}>Register</Button>
      </div>
    </div>
  );
};

export default RegisterPage;
