import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // Synchronous initialization from localStorage prevents flash-of-unauthenticated state on refresh
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem('agentx_user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => {
    return localStorage.getItem('agentx_token') || null;
  });

  const [loading, setLoading] = useState(false);

  const checkAuth = useCallback(async () => {
    const savedToken = localStorage.getItem('agentx_token');
    if (!savedToken) {
      setUser(null);
      setToken(null);
      setLoading(false);
      return;
    }

    try {
      const response = await api.get('/auth/me');
      setUser(response.data);
      setToken(savedToken);
      localStorage.setItem('agentx_user', JSON.stringify(response.data));
    } catch (err) {
      // Only clear credentials if backend explicitly reports unauthenticated or token expired
      if (err.response && (err.response.status === 401 || err.response.status === 403)) {
        console.warn('Session expired or unauthorized, clearing local credentials:', err);
        localStorage.removeItem('agentx_token');
        localStorage.removeItem('agentx_user');
        setUser(null);
        setToken(null);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const login = async (identifier, password, rememberMe = false) => {
    const response = await api.post('/auth/login', {
      identifier,
      password,
      rememberMe,
    });
    const { token: receivedToken, ...userData } = response.data;
    localStorage.setItem('agentx_token', receivedToken);
    localStorage.setItem('agentx_user', JSON.stringify(userData));
    setToken(receivedToken);
    setUser(userData);
    return userData;
  };

  const signup = async (signupData) => {
    const response = await api.post('/auth/signup', signupData);
    return response.data;
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // Ignore network errors on logout
    } finally {
      localStorage.removeItem('agentx_token');
      localStorage.removeItem('agentx_user');
      setUser(null);
      setToken(null);
      window.location.href = '/login';
    }
  };

  const forgotPassword = async (email) => {
    const response = await api.post('/auth/forgot-password', { email });
    return response.data;
  };

  const resetPassword = async (token, newPassword, confirmPassword) => {
    const response = await api.post('/auth/reset-password', {
      token,
      newPassword,
      confirmPassword,
    });
    return response.data;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        role: user ? user.role : null,
        token,
        isAuthenticated: !!user && !!token,
        loading,
        login,
        signup,
        logout,
        forgotPassword,
        resetPassword,
        checkAuth,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
