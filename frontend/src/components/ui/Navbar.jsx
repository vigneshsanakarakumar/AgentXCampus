import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import Avatar from './Avatar';
import Badge from './Badge';
import api from '../../services/api';
import { Bell, ChevronDown, LogOut, User, Settings, HelpCircle, Shield, Moon, Sun, CheckCircle } from 'lucide-react';

export const Navbar = () => {
  const { user, role, logout } = useAuth();
  const { isDark, toggleTheme } = useTheme();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const fetchNotifications = async () => {
    if (!user) return;
    try {
      const res = await api.get('/notifications');
      setNotifications(res.data);
    } catch (err) {
      // ignore if unauthenticated
    }
  };

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 15000);
    return () => clearInterval(interval);
  }, [user]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
        setNotifOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleMarkAsRead = async (id) => {
    try {
      await api.put(`/notifications/${id}/read`);
      fetchNotifications();
    } catch (err) {}
  };

  const getRoleBadgeVariant = (r) => {
    switch (r) {
      case 'ADMIN': return 'danger';
      case 'HOD': return 'primary';
      case 'FACULTY': return 'info';
      case 'STAFF': return 'warning';
      default: return 'primary';
    }
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  return (
    <header className="sticky top-0 z-40 w-full border-b border-[var(--color-border)] bg-[var(--color-card)]/95 backdrop-blur-sm">
      <div className="flex h-16 items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Brand / Logo */}
        <div className="flex items-center gap-3">
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-8 h-8 rounded-lg bg-[var(--color-primary)] flex items-center justify-center text-white font-bold text-sm shadow-sm transition-transform group-hover:scale-105">
              AX
            </div>
            <span className="font-bold text-lg text-[var(--color-foreground)] tracking-tight">
              AgentX <span className="text-[var(--color-primary)] font-semibold">Campus</span>
            </span>
          </Link>
        </div>

        {/* Right Section */}
        <div className="flex items-center gap-3" ref={dropdownRef}>
          {/* Dark mode toggle */}
          <button
            onClick={toggleTheme}
            className="p-2 rounded-lg text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] hover:bg-[var(--color-border)]/50 transition-colors"
            title={`Switch to ${isDark ? 'light' : 'dark'} mode`}
          >
            {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
          </button>

          {/* Database-backed Notifications */}
          <div className="relative">
            <button
              onClick={() => { setNotifOpen(!notifOpen); fetchNotifications(); }}
              className="p-2 rounded-lg text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] hover:bg-[var(--color-border)]/50 transition-colors relative"
            >
              <Bell className="w-4 h-4" />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 px-1 min-w-[16px] h-4 rounded-full bg-[var(--color-primary)] text-white text-[9px] font-bold flex items-center justify-center">
                  {unreadCount}
                </span>
              )}
            </button>

            {notifOpen && (
              <div className="absolute right-0 mt-2 w-80 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-xl py-2 z-50 animate-fade-in">
                <div className="px-4 py-2 border-b border-[var(--color-border)] flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wider text-[var(--color-muted-foreground)]">
                    Campus Notifications ({unreadCount})
                  </span>
                </div>
                <div className="divide-y divide-[var(--color-border)] max-h-72 overflow-y-auto">
                  {notifications.length === 0 ? (
                    <div className="p-4 text-center text-xs text-[var(--color-muted-foreground)]">
                      No notifications at this time.
                    </div>
                  ) : (
                    notifications.map((n) => (
                      <div
                        key={n.id}
                        onClick={() => handleMarkAsRead(n.id)}
                        className={`p-3 transition-colors cursor-pointer ${n.read ? 'opacity-60 hover:bg-[var(--color-background)]/30' : 'bg-[var(--color-primary)]/5 hover:bg-[var(--color-primary)]/10'}`}
                      >
                        <div className="flex items-center justify-between">
                          <p className="text-xs font-semibold text-[var(--color-foreground)]">{n.title}</p>
                          <Badge variant={n.type === 'TIMETABLE_UPDATE' ? 'primary' : 'warning'} size="sm">
                            {n.type === 'TIMETABLE_UPDATE' ? 'Schedule' : 'Notice'}
                          </Badge>
                        </div>
                        <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1">{n.message}</p>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          {/* User Profile Dropdown */}
          {user && (
            <div className="relative">
              <button
                onClick={() => setDropdownOpen(!dropdownOpen)}
                className="flex items-center gap-2.5 p-1.5 rounded-lg hover:bg-[var(--color-border)]/50 transition-colors"
              >
                <Avatar name={`${user.firstName} ${user.lastName}`} size="sm" />
                <div className="hidden md:flex flex-col items-start text-left">
                  <span className="text-xs font-semibold text-[var(--color-foreground)] leading-none">
                    {user.firstName} {user.lastName}
                  </span>
                  <span className="text-[10px] text-[var(--color-muted-foreground)] capitalize mt-0.5">
                    {role ? role.toLowerCase() : 'user'}
                  </span>
                </div>
                <ChevronDown className="w-3.5 h-3.5 text-[var(--color-muted-foreground)]" />
              </button>

              {dropdownOpen && (
                <div className="absolute right-0 mt-2 w-56 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-xl py-2 z-50 animate-fade-in">
                  <div className="px-4 py-2.5 border-b border-[var(--color-border)]">
                    <p className="text-xs font-semibold text-[var(--color-foreground)] truncate">{user.firstName} {user.lastName}</p>
                    <p className="text-[11px] text-[var(--color-muted-foreground)] truncate mt-0.5">{user.email}</p>
                    <div className="mt-2">
                      <Badge variant={getRoleBadgeVariant(role)} size="sm">
                        {role}
                      </Badge>
                    </div>
                  </div>

                  <div className="border-t border-[var(--color-border)] pt-1">
                    <button
                      onClick={() => {
                        setDropdownOpen(false);
                        logout();
                      }}
                      className="w-full flex items-center gap-2.5 px-4 py-2 text-xs text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors"
                    >
                      <LogOut className="w-3.5 h-3.5" />
                      <span>Sign out</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
