import React, { createContext, useContext, useState } from 'react';
import { isTokenExpired } from '../utils/auth';

interface AuthContextValue {
  isLoggedIn: boolean;
  setLoggedIn: (v: boolean) => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isLoggedIn, setLoggedIn] = useState(() => {
    const token = localStorage.getItem('auth_token');
    return token !== null && !isTokenExpired(token);
  });

  return (
    <AuthContext.Provider value={{ isLoggedIn, setLoggedIn }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
