import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  BookOpen,
  Calendar,
  Clock,
  FileText,
  AlertCircle,
  Building2,
  Bot,
  Users,
  CheckSquare,
  ShieldAlert,
  BarChart3,
  Bell,
  Settings
} from 'lucide-react';

export const Sidebar = ({ activeTab, onTabChange }) => {
  const { role } = useAuth();

  const studentLinks = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard, to: '/dashboard' },
    { id: 'academics', label: 'My Academics', icon: BookOpen, to: '/academics' },
    { id: 'timetable', label: 'Timetable', icon: Clock, to: '/timetable' },
    { id: 'events', label: 'Events & Clubs', icon: Calendar, to: '/events' },
    { id: 'complaints', label: 'Complaints', icon: AlertCircle, to: '/complaints' },
    { id: 'agent', label: 'AgentX Assistant', icon: Bot, highlight: true, to: '/assistant' },
  ];

  const facultyLinks = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard, to: '/faculty/dashboard' },
    { id: 'myTimetable', label: 'My Teaching Schedule', icon: Clock },
    { id: 'sectionTimetable', label: 'Section Timetable Editor', icon: BookOpen },
    { id: 'uploadTimetable', label: 'Upload Timetable (AI)', icon: FileText },
    { id: 'quickAttendance', label: 'Quick Mark Attendance', icon: CheckSquare },
    { id: 'mentorClass', label: 'Mentor Section Roster', icon: Users, to: '/faculty/class' },
    { id: 'leaveRequests', label: 'My Leave to HOD', icon: FileText },
    { id: 'deptEvents', label: 'Department Events', icon: Calendar },
    { id: 'complaints', label: 'Student Complaints', icon: AlertCircle },
    { id: 'announcements', label: 'Announcements', icon: Bell },
    { id: 'agent', label: 'AgentX Assistant', icon: Bot, highlight: true },
  ];

  const staffLinks = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'tickets', label: 'Grievance Tickets', icon: AlertCircle },
    { id: 'tasks', label: 'Department Tasks', icon: CheckSquare },
    { id: 'agent', label: 'AgentX Assistant', icon: Bot, highlight: true },
  ];

  const adminLinks = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'approvals', label: 'Approval Center', icon: ShieldAlert, badge: '4' },
    { id: 'complaints', label: 'Complaints', icon: AlertCircle },
    { id: 'workflows', label: 'Agent Workflows', icon: Bot },
    { id: 'analytics', label: 'Analytics', icon: BarChart3 },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  const links =
    role === 'FACULTY' ? facultyLinks :
    role === 'STAFF' ? staffLinks :
    role === 'ADMIN' ? adminLinks :
    studentLinks;

  return (
    <aside className="w-64 shrink-0 border-r border-[var(--color-border)] bg-[var(--color-card)] flex flex-col min-h-[calc(100vh-4rem)]">
      <div className="p-4 flex-1">
        <p className="text-[10px] uppercase font-bold tracking-wider text-[var(--color-muted-foreground)] px-3 mb-2">
          {role} Workspace
        </p>
        <nav className="space-y-1">
          {links.map((item) => {
            const Icon = item.icon;

            if (item.to) {
              return (
                <NavLink
                  key={item.id}
                  to={item.to}
                  className={({ isActive }) =>
                    `w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-xs font-medium transition-all ${
                      isActive
                        ? 'bg-[var(--color-primary)] text-white shadow-sm'
                        : item.highlight
                        ? 'text-[var(--color-primary)] bg-[var(--color-primary)]/10 hover:bg-[var(--color-primary)]/20'
                        : 'text-[var(--color-foreground)] hover:bg-[var(--color-border)]/50'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      <div className="flex items-center gap-2.5">
                        <Icon className={`w-4 h-4 ${isActive ? 'text-white' : item.highlight ? 'text-[var(--color-primary)]' : 'text-[var(--color-muted-foreground)]'}`} />
                        <span>{item.label}</span>
                      </div>
                      {item.badge && (
                        <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full ${isActive ? 'bg-white text-[var(--color-primary)]' : 'bg-red-500 text-white'}`}>
                          {item.badge}
                        </span>
                      )}
                    </>
                  )}
                </NavLink>
              );
            }

            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onTabChange && onTabChange(item.id)}
                className={`w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-xs font-medium transition-all ${
                  isActive
                    ? 'bg-[var(--color-primary)] text-white shadow-sm'
                    : item.highlight
                    ? 'text-[var(--color-primary)] bg-[var(--color-primary)]/10 hover:bg-[var(--color-primary)]/20'
                    : 'text-[var(--color-foreground)] hover:bg-[var(--color-border)]/50'
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <Icon className={`w-4 h-4 ${isActive ? 'text-white' : item.highlight ? 'text-[var(--color-primary)]' : 'text-[var(--color-muted-foreground)]'}`} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full ${isActive ? 'bg-white text-[var(--color-primary)]' : 'bg-red-500 text-white'}`}>
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>

      <div className="p-4 border-t border-[var(--color-border)]">
        <div className="rounded-lg p-3 bg-[var(--color-background)]/60 border border-[var(--color-border)] text-xs text-[var(--color-muted-foreground)]">
          <p className="font-semibold text-[var(--color-foreground)]">AgentX Campus</p>
          <p className="text-[11px] mt-0.5">Production v1.0.0</p>
          <div className="flex items-center gap-1.5 mt-2 text-[10px] text-emerald-600 dark:text-emerald-400">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            <span>Agent Systems Online</span>
          </div>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
