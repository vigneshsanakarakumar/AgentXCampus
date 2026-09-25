import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

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
    } catch (err) {
      console.error('Session validation failed:', err);
      localStorage.removeItem('agentx_token');
      localStorage.removeItem('agentx_user');
      setUser(null);
      setToken(null);
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
        isAuthenticated: !!user,
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
