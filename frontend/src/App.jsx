import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { RequireAuth, RequireRole, RequireGuest } from './guards/RouteGuards';

import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

import StudentLayout from './layouts/StudentLayout';
import StudentOverviewPage from './pages/student/StudentOverviewPage';
import StudentAcademicsPage from './pages/student/StudentAcademicsPage';
import StudentTimetablePage from './pages/student/StudentTimetablePage';
import StudentEventsPage from './pages/student/StudentEventsPage';
import StudentComplaintsPage from './pages/student/StudentComplaintsPage';
import StudentAssistantPage from './pages/student/StudentAssistantPage';

import FacultyPortal from './pages/FacultyPortal';
import FacultyClassRosterPage from './pages/FacultyClassRosterPage';
import StaffPortal from './pages/StaffPortal';
import AdminPortal from './pages/AdminPortal';
import ActivateStaffPage from './pages/ActivateStaffPage';

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<RequireGuest><LoginPage /></RequireGuest>} />
            <Route path="/signup" element={<RequireGuest><SignupPage /></RequireGuest>} />
            <Route path="/forgot-password" element={<RequireGuest><ForgotPasswordPage /></RequireGuest>} />
            <Route path="/reset-password" element={<RequireGuest><ResetPasswordPage /></RequireGuest>} />
            <Route path="/activate-staff" element={<ActivateStaffPage />} />

            {/* Student Dedicated Routes within Persistent StudentLayout */}
            <Route
              element={
                <RequireAuth>
                  <RequireRole allowedRoles={['STUDENT']}>
                    <StudentLayout />
                  </RequireRole>
                </RequireAuth>
              }
            >
              <Route path="/dashboard" element={<StudentOverviewPage />} />
              <Route path="/academics" element={<StudentAcademicsPage />} />
              <Route path="/timetable" element={<StudentTimetablePage />} />
              <Route path="/events" element={<StudentEventsPage />} />
              <Route path="/complaints" element={<StudentComplaintsPage />} />
              <Route path="/assistant" element={<StudentAssistantPage />} />
            </Route>

            {/* Backwards compatibility aliases */}
            <Route path="/student/dashboard" element={<Navigate to="/dashboard" replace />} />
            <Route path="/student" element={<Navigate to="/dashboard" replace />} />
            <Route path="/student/*" element={<Navigate to="/dashboard" replace />} />

            <Route
              path="/faculty/dashboard"
              element={
                <RequireAuth>
                  <RequireRole allowedRoles={['FACULTY']}>
                    <FacultyPortal />
                  </RequireRole>
                </RequireAuth>
              }
            />

            <Route
              path="/faculty/class"
              element={
                <RequireAuth>
                  <RequireRole allowedRoles={['FACULTY']}>
                    <FacultyClassRosterPage />
                  </RequireRole>
                </RequireAuth>
              }
            />
            <Route path="/faculty" element={<Navigate to="/faculty/dashboard" replace />} />

            <Route
              path="/staff/dashboard"
              element={
                <RequireAuth>
                  <RequireRole allowedRoles={['STAFF']}>
                    <StaffPortal />
                  </RequireRole>
                </RequireAuth>
              }
            />

            <Route
              path="/admin/dashboard"
              element={
                <RequireAuth>
                  <RequireRole allowedRoles={['ADMIN']}>
                    <AdminPortal />
                  </RequireRole>
                </RequireAuth>
              }
            />

            {/* Catch-all redirect */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}
