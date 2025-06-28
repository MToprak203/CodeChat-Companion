import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import { login } from '../services/authService';
import { useAuth } from '../context/AuthContext';

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const { setLoggedIn } = useAuth();

  const handleLogin = async () => {
    if (!username || !password) {
      setError('Username and password are required');
      return;
    }
    try {
      const { token, userId, username: returnedUsername } = await login({ username, password });

      localStorage.setItem('auth_token', token);
      localStorage.setItem('user_id', userId.toString());
      localStorage.setItem('username', returnedUsername);
      setLoggedIn(true);
      navigate('/chat');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-900">
      <div className="w-full max-w-md bg-white dark:bg-gray-800 p-8 rounded-xl shadow-md space-y-6">
        <h2 className="text-2xl font-semibold text-center">Login</h2>
        <Input value={username} onChange={setUsername} placeholder="Username" />
        <Input type="password" value={password} onChange={setPassword} placeholder="Password" />
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <div className="flex space-x-2">
          <Button onClick={handleLogin} className="w-1/2">
            Log In
          </Button>
          <Button onClick={() => navigate('/register')} className="w-1/2">
            Register
          </Button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
