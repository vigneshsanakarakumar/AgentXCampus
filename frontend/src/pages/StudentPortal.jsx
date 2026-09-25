import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api from '../services/api';
import Navbar from '../components/ui/Navbar';
import Sidebar from '../components/ui/Sidebar';
import StatCard from '../components/ui/StatCard';
import Card, { CardHeader, CardBody } from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import AgentXAssistant from '../components/ui/AgentXAssistant';
import DigitalCampusView from '../components/ui/DigitalCampusView';
import {
  Clock, BookOpen, Calendar, AlertCircle, Plus, MapPin, TrendingUp,
  UserCheck, GraduationCap, Mail, Building, CheckCircle2, Circle,
  Trash2, Bell, FileText, CheckSquare, Award
} from 'lucide-react';

export const StudentPortal = () => {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [activeTab, setActiveTab] = useState('overview');
  const [portalView, setPortalView] = useState('overview'); // 'overview' | 'timetable' | 'assignments' | 'tasks' | 'resources' | 'agent'
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  // Task creation state
  const [showTaskForm, setShowTaskForm] = useState(false);
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const [newTaskPriority, setNewTaskPriority] = useState('MEDIUM');
  const [newTaskDueDate, setNewTaskDueDate] = useState('');
  const [taskSubmitting, setTaskSubmitting] = useState(false);

  // Grievance state
  const [showComplaintForm, setShowComplaintForm] = useState(false);
  const [complaintLocation, setComplaintLocation] = useState('');
  const [complaintDesc, setComplaintDesc] = useState('');
  const [complaintSubmitting, setComplaintSubmitting] = useState(false);

  const fetchDashboard = async () => {
    try {
      const res = await api.get('/student/dashboard');
      setData(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    if (!newTaskTitle.trim()) return;

    setTaskSubmitting(true);
    try {
      await api.post('/student/tasks', {
        title: newTaskTitle.trim(),
        priority: newTaskPriority,
        dueDate: newTaskDueDate || null,
      });
      addToast('Task created successfully!', 'success');
      setNewTaskTitle('');
      setShowTaskForm(false);
      fetchDashboard();
    } catch (err) {
      addToast('Failed to create task.', 'error');
    } finally {
      setTaskSubmitting(false);
    }
  };

  const handleToggleTaskStatus = async (taskId, currentStatus) => {
    const nextStatus = currentStatus === 'COMPLETED' ? 'TODO' : 'COMPLETED';
    try {
      await api.patch(`/student/tasks/${taskId}/status`, { status: nextStatus });
      addToast(nextStatus === 'COMPLETED' ? 'Task marked as completed!' : 'Task reopened.', 'info');
      fetchDashboard();
    } catch (err) {
      addToast('Unable to update task status.', 'error');
    }
  };

  const handleDeleteTask = async (taskId) => {
    try {
      await api.delete(`/student/tasks/${taskId}`);
      addToast('Task deleted.', 'info');
      fetchDashboard();
    } catch (err) {
      addToast('Unable to delete task.', 'error');
    }
  };

  const handleCreateComplaint = async (e) => {
    e.preventDefault();
    if (!complaintDesc.trim()) return;

    setComplaintSubmitting(true);
    try {
      const res = await api.post('/student/grievances', {
        category: 'CAMPUS_FACILITIES',
        department: 'Campus Infrastructure & Maintenance',
        urgency: 'HIGH',
        location: complaintLocation || 'CS-204',
        description: complaintDesc,
      });
      addToast('Grievance ticket #' + res.data.ticketNumber + ' created successfully.', 'success');
      setComplaintDesc('');
      setComplaintLocation('');
      setShowComplaintForm(false);
      fetchDashboard();
    } catch (err) {
      addToast('Unable to submit grievance ticket.', 'error');
    } finally {
      setComplaintSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[var(--color-background)]">
        <Navbar />
        <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
          <LoadingSpinner size="lg" message="Loading student workspace & academic context..." />
        </div>
      </div>
    );
  }

  const nextClass = data?.nextClass;
  const mentor = data?.mentor;
  const timetables = data?.timetables || [];
  const todayClasses = data?.todayClasses || [];
  const assignments = data?.assignments || [];
  const attendanceRecords = data?.attendanceRecords || [];
  const tasks = data?.tasks || [];
  const notices = data?.announcements || [];
  const events = data?.events || [];
  const grievances = data?.grievances || [];

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col">
      <Navbar />
      <div className="flex-1 flex flex-col md:flex-row">
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />

        <main className="flex-1 p-6 md:p-8 overflow-y-auto max-w-7xl">
          {/* Header & Student Details */}
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
                <span>CGPA: <strong className="text-emerald-600 dark:text-emerald-400 font-mono">{data?.cgpa || 8.64}</strong></span>
              </p>
            </div>

            {/* View Switcher Pills */}
            <div className="flex items-center gap-1.5 p-1 rounded-xl bg-[var(--color-muted)]/40 border border-[var(--color-border)] overflow-x-auto no-scrollbar">
              <button
                onClick={() => setPortalView('overview')}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  portalView === 'overview'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Dashboard
              </button>
              <button
                onClick={() => setPortalView('timetable')}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  portalView === 'timetable'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Timetable
              </button>
              <button
                onClick={() => setPortalView('assignments')}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  portalView === 'assignments'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Assignments ({assignments.length})
              </button>
              <button
                onClick={() => setPortalView('tasks')}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  portalView === 'tasks'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Tasks ({tasks.length})
              </button>
              <button
                onClick={() => setPortalView('resources')}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  portalView === 'resources'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Campus Map
              </button>
            </div>
          </div>

          {/* Academic Overview KPI Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              title="Attendance Rate"
              value={(data?.attendance != null ? data.attendance : 85) + '%'}
              subtitle="Minimum 75% required"
              icon={TrendingUp}
              badge={<Badge variant="success">Good Standing</Badge>}
            />
            <StatCard
              title="Next Scheduled Class"
              value={nextClass ? nextClass.subject : 'No Classes Today'}
              subtitle={nextClass ? (nextClass.time + ' • ' + nextClass.room) : 'Free period'}
              icon={Clock}
              badge={<Badge variant={nextClass ? 'primary' : 'neutral'}>{nextClass ? nextClass.day : 'Relax'}</Badge>}
            />
            <StatCard
              title="Active Assignments"
              value={'' + assignments.length}
              subtitle="Course practical deliverables"
              icon={BookOpen}
              badge={<Badge variant={assignments.length > 0 ? 'warning' : 'neutral'}>
                {assignments.length > 0 ? 'Action Req' : 'Up to Date'}
              </Badge>}
            />
            <StatCard
              title="Actionable Tasks"
              value={'' + tasks.filter(t => t.status !== 'COMPLETED').length}
              subtitle="Pending personal & AI tasks"
              icon={CheckSquare}
              badge={<Badge variant="info">Planner</Badge>}
            />
          </div>

          {/* Sub-Views */}
          {activeTab === 'agent' ? (
            <div className="max-w-3xl">
              <AgentXAssistant onActionCompleted={() => fetchDashboard()} />
            </div>
          ) : portalView === 'resources' ? (
            <DigitalCampusView />
          ) : portalView === 'timetable' ? (
            /* ================= FULL TIMETABLE VIEW ================= */
            <Card>
              <CardHeader
                title={`Weekly Master Timetable: ${data?.department} (Section ${data?.section})`}
                subtitle="Official semester lecture & laboratory allocations"
              />
              <CardBody className="p-0">
                <div className="divide-y divide-[var(--color-border)]">
                  {timetables.map((item, idx) => (
                    <div key={idx} className="p-4 flex items-center justify-between hover:bg-[var(--color-background)]/50 transition-colors">
                      <div className="flex items-start gap-3.5">
                        <div className="p-2 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] font-mono text-xs font-semibold">
                          {item.startTime} - {item.endTime}
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <span className="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded bg-[var(--color-muted)] text-[var(--color-foreground)]">
                              {item.subjectCode}
                            </span>
                            <p className="text-xs font-bold text-[var(--color-foreground)]">{item.subjectName}</p>
                          </div>
                          <p className="text-[11px] text-[var(--color-muted-foreground)] mt-0.5">
                            {item.facultyName} • <span className="font-medium text-[var(--color-foreground)]">{item.classroom}</span> • <span className="text-[var(--color-primary)]">{item.dayOfWeek}</span>
                          </p>
                        </div>
                      </div>
                      <Badge variant="neutral" size="sm">{item.dayOfWeek}</Badge>
                    </div>
                  ))}
                </div>
              </CardBody>
            </Card>
          ) : portalView === 'assignments' ? (
            /* ================= FULL ASSIGNMENTS VIEW ================= */
            <Card>
              <CardHeader
                title="Academic Assignments & Coursework"
                subtitle="Track pending submissions and evaluated practical problem sets"
              />
              <CardBody className="p-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {assignments.map((asg) => (
                    <div key={asg.id} className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="font-mono text-xs font-bold text-[var(--color-primary)]">{asg.subjectCode}</span>
                        <Badge variant={asg.priority === 'HIGH' ? 'danger' : 'neutral'} size="sm">
                          {asg.priority} Priority
                        </Badge>
                      </div>
                      <h4 className="text-xs font-bold text-[var(--color-foreground)]">{asg.title}</h4>
                      <p className="text-[11px] text-[var(--color-muted-foreground)]">{asg.description}</p>
                      <div className="pt-2 border-t border-[var(--color-border)]/60 flex items-center justify-between text-[11px]">
                        <span className="text-[var(--color-muted-foreground)]">Due: <strong>{asg.dueDate}</strong></span>
                        <span className="font-semibold text-emerald-600 dark:text-emerald-400">Max: {asg.maxMarks} Marks</span>
                      </div>
                    </div>
                  ))}
                </div>
              </CardBody>
            </Card>
          ) : portalView === 'tasks' ? (
            /* ================= FULL TASKS VIEW ================= */
            <Card>
              <CardHeader
                title="Student Task Planner"
                subtitle="Manage tasks generated by the AI multi-agent planner and your personal study items"
                action={
                  <Button size="sm" icon={Plus} onClick={() => setShowTaskForm(!showTaskForm)}>
                    Add Task
                  </Button>
                }
              />
              <CardBody className="p-5">
                {showTaskForm && (
                  <form onSubmit={handleCreateTask} className="mb-6 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] space-y-3">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-primary)]">New Task</h4>
                    <Input
                      label="Task Description"
                      placeholder="e.g. Revise Computer Networks socket programming..."
                      value={newTaskTitle}
                      onChange={(e) => setNewTaskTitle(e.target.value)}
                      required
                    />
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Priority</label>
                        <select
                          value={newTaskPriority}
                          onChange={(e) => setNewTaskPriority(e.target.value)}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2 text-[var(--color-foreground)]"
                        >
                          <option value="HIGH">High Priority</option>
                          <option value="MEDIUM">Medium Priority</option>
                          <option value="LOW">Low Priority</option>
                        </select>
                      </div>
                      <Input
                        label="Due Date"
                        type="date"
                        value={newTaskDueDate}
                        onChange={(e) => setNewTaskDueDate(e.target.value)}
                      />
                    </div>
                    <div className="flex justify-end gap-2 pt-2">
                      <Button size="sm" variant="ghost" onClick={() => setShowTaskForm(false)}>Cancel</Button>
                      <Button size="sm" type="submit" loading={taskSubmitting}>Save Task</Button>
                    </div>
                  </form>
                )}

                <div className="space-y-2.5">
                  {tasks.map((t) => (
                    <div
                      key={t.id}
                      className={`p-3 rounded-lg border border-[var(--color-border)] flex items-center justify-between gap-3 transition-colors ${
                        t.status === 'COMPLETED' ? 'bg-[var(--color-muted)]/20 opacity-70' : 'bg-[var(--color-background)]/50'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <button
                          onClick={() => handleToggleTaskStatus(t.id, t.status)}
                          className="text-[var(--color-primary)] hover:opacity-80"
                        >
                          {t.status === 'COMPLETED' ? (
                            <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                          ) : (
                            <Circle className="w-4 h-4 text-[var(--color-muted-foreground)]" />
                          )}
                        </button>
                        <div>
                          <p className={`text-xs font-medium ${t.status === 'COMPLETED' ? 'line-through text-[var(--color-muted-foreground)]' : 'text-[var(--color-foreground)]'}`}>
                            {t.title}
                          </p>
                          <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5">
                            Priority: <span className="font-semibold">{t.priority}</span> • Due: {t.dueDate || 'Flexible'}
                          </p>
                        </div>
                      </div>
                      <button
                        onClick={() => handleDeleteTask(t.id)}
                        className="text-[var(--color-muted-foreground)] hover:text-red-500 transition-colors p-1"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ))}
                </div>
              </CardBody>
            </Card>
          ) : (
            /* ================= DEFAULT OVERVIEW DASHBOARD ================= */
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Left Column (2 Cols) */}
              <div className="lg:col-span-2 space-y-6">
                {/* Today's Schedule Card */}
                <Card>
                  <CardHeader
                    title="Today's Classes & Academic Schedule"
                    subtitle={`Confirmed timetable for Section ${data?.section} • Room CS-204`}
                  />
                  <CardBody className="p-0">
                    {todayClasses.length === 0 ? (
                      <div className="p-6 text-center text-xs text-[var(--color-muted-foreground)]">
                        <p>No classes scheduled for today.</p>
                      </div>
                    ) : (
                      <div className="divide-y divide-[var(--color-border)]">
                        {todayClasses.map((item, idx) => (
                          <div key={idx} className="p-3.5 flex items-center justify-between hover:bg-[var(--color-background)]/50 transition-colors">
                            <div className="flex items-start gap-3">
                              <div className="p-1.5 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] font-mono text-[11px] font-semibold">
                                {item.startTime} - {item.endTime}
                              </div>
                              <div>
                                <div className="flex items-center gap-1.5">
                                  <span className="font-mono text-[10px] font-bold px-1 rounded bg-[var(--color-muted)] text-[var(--color-foreground)]">
                                    {item.subjectCode}
                                  </span>
                                  <p className="text-xs font-bold text-[var(--color-foreground)]">{item.subjectName}</p>
                                </div>
                                <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5">
                                  {item.facultyName} • <span className="font-semibold text-[var(--color-foreground)]">{item.classroom}</span>
                                </p>
                              </div>
                            </div>
                            <Badge variant="neutral" size="sm">Active</Badge>
                          </div>
                        ))}
                      </div>
                    )}
                  </CardBody>
                </Card>

                {/* Upcoming Assignments Card */}
                <Card>
                  <CardHeader
                    title="Upcoming Assignments & Deadlines"
                    subtitle="Institutional coursework submissions"
                    action={
                      <Button size="sm" variant="ghost" onClick={() => setPortalView('assignments')}>
                        View All
                      </Button>
                    }
                  />
                  <CardBody className="p-0">
                    <div className="divide-y divide-[var(--color-border)]">
                      {assignments.slice(0, 3).map((asg) => (
                        <div key={asg.id} className="p-3.5 flex items-center justify-between hover:bg-[var(--color-background)]/50 transition-colors">
                          <div>
                            <div className="flex items-center gap-2">
                              <span className="font-mono text-[10px] font-bold text-[var(--color-primary)]">{asg.subjectCode}</span>
                              <p className="text-xs font-bold text-[var(--color-foreground)]">{asg.title}</p>
                            </div>
                            <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5">
                              Due: <strong className="text-[var(--color-foreground)]">{asg.dueDate}</strong> • Max: {asg.maxMarks} Marks
                            </p>
                          </div>
                          <Badge variant={asg.priority === 'HIGH' ? 'danger' : 'neutral'} size="sm">
                            {asg.priority}
                          </Badge>
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>

                {/* Student Tasks Planner */}
                <Card>
                  <CardHeader
                    title="Student Tasks & Multi-Agent Action Items"
                    subtitle="Generated by Task Planning Agent or added manually"
                    action={
                      <Button size="sm" icon={Plus} onClick={() => setShowTaskForm(!showTaskForm)}>
                        New Task
                      </Button>
                    }
                  />
                  <CardBody className="p-4 space-y-3">
                    {showTaskForm && (
                      <form onSubmit={handleCreateTask} className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] space-y-2">
                        <Input
                          label="Task Name"
                          placeholder="e.g. Review DBMS Normalization..."
                          value={newTaskTitle}
                          onChange={(e) => setNewTaskTitle(e.target.value)}
                          required
                        />
                        <div className="flex justify-end gap-2 pt-1">
                          <Button size="sm" variant="ghost" onClick={() => setShowTaskForm(false)}>Cancel</Button>
                          <Button size="sm" type="submit" loading={taskSubmitting}>Add</Button>
                        </div>
                      </form>
                    )}

                    <div className="space-y-2">
                      {tasks.slice(0, 4).map((t) => (
                        <div
                          key={t.id}
                          className={`p-2.5 rounded-lg border border-[var(--color-border)] flex items-center justify-between gap-2.5 ${
                            t.status === 'COMPLETED' ? 'bg-[var(--color-muted)]/20 opacity-70' : 'bg-[var(--color-background)]/50'
                          }`}
                        >
                          <div className="flex items-center gap-2.5">
                            <button onClick={() => handleToggleTaskStatus(t.id, t.status)}>
                              {t.status === 'COMPLETED' ? (
                                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                              ) : (
                                <Circle className="w-4 h-4 text-[var(--color-muted-foreground)]" />
                              )}
                            </button>
                            <p className={`text-xs ${t.status === 'COMPLETED' ? 'line-through text-[var(--color-muted-foreground)]' : 'text-[var(--color-foreground)] font-medium'}`}>
                              {t.title}
                            </p>
                          </div>
                          <Badge variant={t.priority === 'HIGH' ? 'danger' : 'neutral'} size="sm">
                            {t.priority}
                          </Badge>
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>

                {/* Subject-Wise Attendance Breakdown */}
                <Card>
                  <CardHeader
                    title="Subject-Wise Attendance Standing"
                    subtitle="Verified academic records (Minimum 75% mandatory)"
                  />
                  <CardBody className="p-4">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      {attendanceRecords.map((rec) => (
                        <div key={rec.id} className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-background)]/40 flex items-center justify-between">
                          <div>
                            <span className="font-mono text-[10px] font-bold text-[var(--color-primary)]">{rec.courseCode}</span>
                            <p className="text-xs font-semibold text-[var(--color-foreground)]">{rec.courseName}</p>
                            <p className="text-[10px] text-[var(--color-muted-foreground)]">
                              {rec.attendedClasses}/{rec.totalClasses} classes attended
                            </p>
                          </div>
                          <div className="text-right">
                            <span className={`text-sm font-bold font-mono ${rec.percentage >= 75 ? 'text-emerald-600 dark:text-emerald-400' : 'text-amber-500'}`}>
                              {rec.percentage}%
                            </span>
                            <p className="text-[9px] text-[var(--color-muted-foreground)]">
                              {rec.percentage >= 75 ? 'Eligible' : 'Warning'}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>
              </div>

              {/* Right Column (1 Col) */}
              <div className="space-y-6">
                {/* Prominent AI Assistant */}
                <AgentXAssistant onActionCompleted={() => fetchDashboard()} />

                {/* Assigned Faculty Mentor Card */}
                <Card>
                  <CardHeader title="Faculty Mentor" subtitle="Assigned section advisor" />
                  <CardBody className="p-4">
                    {mentor ? (
                      <div className="space-y-2 text-xs">
                        <div className="flex items-center gap-2">
                          <GraduationCap className="w-4 h-4 text-[var(--color-primary)]" />
                          <span className="font-bold text-[var(--color-foreground)]">{mentor.name}</span>
                        </div>
                        <p className="text-[11px] text-[var(--color-muted-foreground)]">
                          {mentor.designation} • {mentor.department}
                        </p>
                        <div className="p-2.5 rounded-lg bg-[var(--color-background)] text-[11px] space-y-1 border border-[var(--color-border)]/60">
                          <p className="flex items-center gap-1.5 text-[var(--color-foreground)]">
                            <Mail className="w-3 h-3 text-[var(--color-primary)]" />
                            <span>{mentor.email}</span>
                          </p>
                          <p className="text-[var(--color-muted-foreground)]">
                            Cabin: <span className="font-medium text-[var(--color-foreground)]">{mentor.cabinNumber}</span>
                          </p>
                        </div>
                      </div>
                    ) : (
                      <div className="text-xs text-[var(--color-muted-foreground)]">
                        <p>No mentor assigned to Section {data?.section} yet.</p>
                      </div>
                    )}
                  </CardBody>
                </Card>

                {/* Recent Notices Card */}
                <Card>
                  <CardHeader title="Recent Notices & Circulars" subtitle="Official college announcements" />
                  <CardBody className="p-0">
                    <div className="divide-y divide-[var(--color-border)]">
                      {notices.slice(0, 3).map((n) => (
                        <div key={n.id} className="p-3.5 hover:bg-[var(--color-background)]/50 transition-colors">
                          <div className="flex items-center justify-between mb-1">
                            <span className="text-[10px] uppercase font-bold text-[var(--color-primary)]">{n.authorRole || 'ADMIN'}</span>
                            <Badge variant={n.priority === 'URGENT' ? 'danger' : 'neutral'} size="sm">{n.priority}</Badge>
                          </div>
                          <p className="text-xs font-bold text-[var(--color-foreground)]">{n.title}</p>
                          <p className="text-[11px] text-[var(--color-muted-foreground)] line-clamp-2 mt-1">{n.content}</p>
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>

                {/* Upcoming Events Card */}
                <Card>
                  <CardHeader title="Campus Events & Hackathons" subtitle="Upcoming institutional activities" />
                  <CardBody className="p-0">
                    <div className="divide-y divide-[var(--color-border)]">
                      {events.slice(0, 3).map((e) => (
                        <div key={e.id} className="p-3.5 hover:bg-[var(--color-background)]/50 transition-colors">
                          <div className="flex items-center justify-between mb-1">
                            <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400">{e.category}</span>
                            <span className="text-[10px] text-[var(--color-muted-foreground)] font-mono">{e.eventDate}</span>
                          </div>
                          <p className="text-xs font-bold text-[var(--color-foreground)]">{e.title}</p>
                          <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5 flex items-center gap-1">
                            <MapPin className="w-3 h-3" />
                            <span>{e.location}</span>
                          </p>
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default StudentPortal;
