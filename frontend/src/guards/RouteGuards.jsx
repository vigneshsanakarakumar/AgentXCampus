import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/ui/LoadingSpinner';

export const RequireAuth = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--color-background)]">
        <LoadingSpinner size="lg" message="Validating campus session..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

export const RequireRole = ({ allowedRoles, children }) => {
  const { user, role, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--color-background)]">
        <LoadingSpinner size="lg" message="Verifying role authorizations..." />
      </div>
    );
  }

  if (!user || !allowedRoles.includes(role)) {
    // Redirect to their respective authorized portal
    if (role === 'STUDENT') return <Navigate to="/dashboard" replace />;
    if (role === 'FACULTY') return <Navigate to="/faculty/dashboard" replace />;
    if (role === 'STAFF') return <Navigate to="/staff/dashboard" replace />;
    if (role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
    return <Navigate to="/login" replace />;
  }

  return children;
};

export const RequireGuest = ({ children }) => {
  const { isAuthenticated, role, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (isAuthenticated && role) {
    if (role === 'STUDENT') return <Navigate to="/dashboard" replace />;
    if (role === 'FACULTY') return <Navigate to="/faculty/dashboard" replace />;
    if (role === 'STAFF') return <Navigate to="/staff/dashboard" replace />;
    if (role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
  }

  return children;
};
