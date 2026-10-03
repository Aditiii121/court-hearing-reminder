import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (token && userData) {
      try {
        setUser(JSON.parse(userData));
      } catch {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await authAPI.login({ email, password });
    const { token, role, fullName, userId, email: userEmail } = response.data;
    const userData = { token, role, fullName, userId, email: userEmail };
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const isAdmin = () => user?.role === 'ADMIN';
  const isLawyer = () => user?.role === 'LAWYER';
  const isClerk = () => user?.role === 'CLERK';
  const canDelete = () => user?.role === 'ADMIN';
  const canEdit = () => ['ADMIN', 'LAWYER'].includes(user?.role);
  const canCreate = () => ['ADMIN', 'LAWYER', 'CLERK'].includes(user?.role);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isAdmin, isLawyer, isClerk, canDelete, canEdit, canCreate }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
