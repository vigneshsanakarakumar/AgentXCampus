import React, { useState, useEffect, useCallback } from 'react';
import { Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/ui/Navbar';
import Sidebar from '../components/ui/Sidebar';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorState from '../components/ui/ErrorState';
import api from '../services/api';

export const StudentLayout = () => {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/student/dashboard');
      setData(res.data);
    } catch (err) {
      console.error('Failed to load student dashboard:', err);
      const errMsg = err.response?.data?.message || err.message || 'Unable to load student context.';
      setError(errMsg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col">
      <Navbar />
      <div className="flex-1 flex flex-col md:flex-row">
        <Sidebar />

        <main className="flex-1 p-6 md:p-8 overflow-y-auto max-w-7xl">
          {/* Persistent Top Student Header & Details Banner */}
          <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-[var(--color-border)]">
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-bold text-[var(--color-foreground)] tracking-tight">
                  {getGreeting()}, {user?.firstName} {user?.lastName}
                </h1>
                <Badge variant="primary" size="sm">Student</Badge>
              </div>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-1 flex flex-wrap items-center gap-2">
                <span>Roll No: <strong className="text-[var(--color-foreground)] font-mono">{data?.rollNumber || '717824P361'}</strong></span>
                <span>•</span>
                <span>Dept: <strong className="text-[var(--color-foreground)]">{data?.department || 'Computer Science & Engineering'}</strong></span>
                <span>•</span>
                <span>Section: <strong className="text-[var(--color-foreground)]">{data?.section || 'C'}</strong></span>
                <span>•</span>
                <span>Sem: <strong className="text-[var(--color-foreground)]">{data?.semester || 5}</strong></span>
                <span>•</span>
                <span>CGPA: <strong className="text-emerald-600 dark:text-emerald-400 font-mono">{data?.cgpa != null ? data.cgpa : 8.64}</strong></span>
                <span>•</span>
                <span>Attendance: <strong className={`font-mono ${(data?.attendance != null ? data.attendance : 85) >= 75 ? 'text-emerald-600 dark:text-emerald-400' : 'text-amber-500'}`}>{(data?.attendance != null ? data.attendance : 85)}%</strong></span>
              </p>
            </div>
          </div>

          {/* Loading, Error, or Outlet Page Content */}
          {loading && !data ? (
            <div className="py-20 flex justify-center items-center">
              <LoadingSpinner size="lg" message="Loading student workspace & academic context..." />
            </div>
          ) : error && !data ? (
            <ErrorState
              title="Unable to connect to Academic Registry"
              message={error}
              onRetry={fetchDashboard}
            />
          ) : (
            <Outlet context={{ data, loading, error, refreshData: fetchDashboard, user }} />
          )}
        </main>
      </div>
    </div>
  );
};

export default StudentLayout;
