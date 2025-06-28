import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ChatPage from './pages/ChatPage';
import ProjectChatPage from './pages/ProjectChatPage';
import { useSystemNotifications } from './hooks/useSystemNotifications';
import SystemNotificationBanner from './components/SystemNotificationBanner';
import { logout } from './services/authService';
import { AuthProvider, useAuth } from './context/AuthContext';


function AppRoutes() {
  const { isLoggedIn } = useAuth();
  const [notifications, removeNotification] = useSystemNotifications();

  return (
    <>
      <SystemNotificationBanner
        notifications={notifications}
        onClose={removeNotification}
      />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/chat" element={isLoggedIn ? <ChatPage /> : <Navigate to="/login" />} />
        <Route
          path="/projects/:projectId/chat"
          element={isLoggedIn ? <ProjectChatPage /> : <Navigate to="/login" />}
        />
        <Route path="*" element={<Navigate to={isLoggedIn ? "/chat" : "/login"} />} />
      </Routes>
    </>
  );
}

export default function App() {
  useEffect(() => {
    const handler = () => {
      logout(true);
    };
    window.addEventListener('unload', handler);
    return () => window.removeEventListener('unload', handler);
  }, []);

  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}
