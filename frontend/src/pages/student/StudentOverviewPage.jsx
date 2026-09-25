import React, { useState } from 'react';
import { useOutletContext, Link } from 'react-router-dom';
import { useToast } from '../../context/ToastContext';
import StatCard from '../../components/ui/StatCard';
import Card, { CardHeader, CardBody } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import AgentXAssistant from '../../components/ui/AgentXAssistant';
import api from '../../services/api';
import {
  Clock, BookOpen, Calendar, AlertCircle, Plus, MapPin, TrendingUp,
  GraduationCap, Mail, CheckCircle2, Circle, Trash2, CheckSquare,
  ArrowRight
} from 'lucide-react';

export const StudentOverviewPage = () => {
  const { data, refreshData } = useOutletContext();
  const { addToast } = useToast();

  // Task creation state
  const [showTaskForm, setShowTaskForm] = useState(false);
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const [newTaskPriority, setNewTaskPriority] = useState('MEDIUM');
  const [newTaskDueDate, setNewTaskDueDate] = useState('');
  const [taskSubmitting, setTaskSubmitting] = useState(false);

  const nextClass = data?.nextClass;
  const mentor = data?.mentor;
  const todayClasses = data?.todayClasses || [];
  const assignments = data?.assignments || [];
  const attendanceRecords = data?.attendanceRecords || [];
  const tasks = data?.tasks || [];
  const notices = data?.announcements || [];
  const events = data?.events || [];

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
      refreshData();
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
      refreshData();
    } catch (err) {
      addToast('Unable to update task status.', 'error');
    }
  };

  const handleDeleteTask = async (taskId) => {
    try {
      await api.delete(`/student/tasks/${taskId}`);
      addToast('Task deleted.', 'info');
      refreshData();
    } catch (err) {
      addToast('Unable to delete task.', 'error');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Academic KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Attendance Rate"
          value={(data?.attendance != null ? data.attendance : 85) + '%'}
          subtitle="Minimum 75% required"
          icon={TrendingUp}
          badge={<Badge variant={(data?.attendance != null ? data.attendance : 85) >= 75 ? 'success' : 'danger'}>{(data?.attendance != null ? data.attendance : 85) >= 75 ? 'Good Standing' : 'Critical'}</Badge>}
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

      {/* Main Grid: Left (2 cols) & Right (1 col) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column */}
        <div className="lg:col-span-2 space-y-6">
          {/* Today's Schedule Card */}
          <Card>
            <CardHeader
              title="Today's Classes & Academic Schedule"
              subtitle={`Confirmed timetable for Section ${data?.section || 'C'} • Room CS-204`}
              action={
                <Link to="/timetable" className="text-xs font-semibold text-[var(--color-primary)] hover:underline flex items-center gap-1">
                  Full Timetable <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              }
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
              subtitle="Coursework submissions & lab assignments"
              action={
                <Link to="/academics" className="text-xs font-semibold text-[var(--color-primary)] hover:underline flex items-center gap-1">
                  View All ({assignments.length}) <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              }
            />
            <CardBody className="p-0">
              {assignments.length === 0 ? (
                <div className="p-6 text-center text-xs text-[var(--color-muted-foreground)]">
                  <p>No pending assignments.</p>
                </div>
              ) : (
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
              )}
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
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    <div>
                      <label className="block text-[11px] font-medium text-[var(--color-foreground)] mb-1">Priority</label>
                      <select
                        value={newTaskPriority}
                        onChange={(e) => setNewTaskPriority(e.target.value)}
                        className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-1.5 text-[var(--color-foreground)]"
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
                  <div className="flex justify-end gap-2 pt-1">
                    <Button size="sm" variant="ghost" onClick={() => setShowTaskForm(false)}>Cancel</Button>
                    <Button size="sm" type="submit" loading={taskSubmitting}>Add Task</Button>
                  </div>
                </form>
              )}

              {tasks.length === 0 ? (
                <p className="text-xs text-center text-[var(--color-muted-foreground)] py-4">No tasks yet. Create one or ask AgentX to plan your study schedule!</p>
              ) : (
                <div className="space-y-2">
                  {tasks.slice(0, 5).map((t) => (
                    <div
                      key={t.id}
                      className={`p-2.5 rounded-lg border border-[var(--color-border)] flex items-center justify-between gap-2.5 transition-colors ${
                        t.status === 'COMPLETED' ? 'bg-[var(--color-muted)]/20 opacity-70' : 'bg-[var(--color-background)]/50'
                      }`}
                    >
                      <div className="flex items-center gap-2.5">
                        <button onClick={() => handleToggleTaskStatus(t.id, t.status)} className="text-[var(--color-primary)]">
                          {t.status === 'COMPLETED' ? (
                            <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                          ) : (
                            <Circle className="w-4 h-4 text-stone-400" />
                          )}
                        </button>
                        <div>
                          <p className={`text-xs ${t.status === 'COMPLETED' ? 'line-through text-[var(--color-muted-foreground)]' : 'text-[var(--color-foreground)] font-medium'}`}>
                            {t.title}
                          </p>
                          <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5">
                            Priority: <span className="font-semibold">{t.priority}</span> • Due: {t.dueDate || 'Flexible'}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge variant={t.priority === 'HIGH' ? 'danger' : 'neutral'} size="sm">
                          {t.priority}
                        </Badge>
                        <button
                          onClick={() => handleDeleteTask(t.id)}
                          className="text-stone-400 hover:text-red-500 transition-colors p-1"
                          title="Delete task"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardBody>
          </Card>

          {/* Subject-Wise Attendance Breakdown Quick Glance */}
          <Card>
            <CardHeader
              title="Subject-Wise Attendance Standing"
              subtitle="Verified academic records (Minimum 75% mandatory)"
              action={
                <Link to="/academics" className="text-xs font-semibold text-[var(--color-primary)] hover:underline flex items-center gap-1">
                  Full Details <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              }
            />
            <CardBody className="p-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {attendanceRecords.slice(0, 4).map((rec) => (
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

        {/* Right Column */}
        <div className="space-y-6">
          {/* Prominent AI Assistant Widget */}
          <AgentXAssistant onActionCompleted={() => refreshData()} />

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
                  <p>No mentor assigned to Section {data?.section || 'C'} yet.</p>
                </div>
              )}
            </CardBody>
          </Card>

          {/* Recent Notices Card */}
          <Card>
            <CardHeader
              title="Recent Notices & Circulars"
              subtitle="Official announcements"
              action={
                <Link to="/events" className="text-xs font-semibold text-[var(--color-primary)] hover:underline flex items-center gap-1">
                  View All <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              }
            />
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
            <CardHeader
              title="Campus Events"
              subtitle="Upcoming activities"
              action={
                <Link to="/events" className="text-xs font-semibold text-[var(--color-primary)] hover:underline flex items-center gap-1">
                  View All <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              }
            />
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
    </div>
  );
};

export default StudentOverviewPage;
