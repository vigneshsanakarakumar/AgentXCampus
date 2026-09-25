import React, { useState, useEffect } from 'react';
import Navbar from '../components/ui/Navbar';
import Card, { CardHeader, CardBody } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import {
  Building2,
  Users,
  Calendar,
  Clock,
  FileText,
  UploadCloud,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Search,
  FileUp,
  Sparkles,
  BookOpen,
  Send,
  Eye,
  UserCheck,
  GraduationCap,
  Layers,
  ArrowRight,
  Filter
} from 'lucide-react';

export const HodPortal = () => {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'timetables' | 'faculty' | 'ai-ingestion'
  const [loading, setLoading] = useState(true);
  const [dashboard, setDashboard] = useState(null);
  const [facultyRoster, setFacultyRoster] = useState([]);
  const [classTimetables, setClassTimetables] = useState({});
  const [facultyTimetables, setFacultyTimetables] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState([]);

  // Timetable view state
  const [timetableMode, setTimetableMode] = useState('class'); // 'class' | 'faculty'
  const [selectedSection, setSelectedSection] = useState('A');
  const [selectedFacultyId, setSelectedFacultyId] = useState('');

  // AI Document Upload state
  const [selectedFile, setSelectedFile] = useState(null);
  const [textOverride, setTextOverride] = useState('');
  const [sectionHint, setSectionHint] = useState('');
  const [uploading, setUploading] = useState(false);
  const [ingestionResult, setIngestionResult] = useState(null);

  // Leave approval modal/state
  const [processingLeaveId, setProcessingLeaveId] = useState(null);
  const [resolutionNotes, setResolutionNotes] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const [dashRes, facultyRes, classTtRes, facTtRes, leavesRes] = await Promise.all([
        api.get('/hod/dashboard'),
        api.get('/hod/faculty'),
        api.get('/hod/timetables/classes'),
        api.get('/hod/timetables/faculty'),
        api.get('/hod/faculty-leave-requests')
      ]);

      setDashboard(dashRes.data);
      setFacultyRoster(facultyRes.data);
      setClassTimetables(classTtRes.data);
      setFacultyTimetables(facTtRes.data);
      setLeaveRequests(leavesRes.data);

      if (facultyRes.data && facultyRes.data.length > 0) {
        setSelectedFacultyId(String(facultyRes.data[0].id));
      }
    } catch (err) {
      console.error('Failed to load HOD dashboard:', err);
      addToast('Error loading HOD departmental data', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateLeaveStatus = async (id, status) => {
    setProcessingLeaveId(id);
    try {
      await api.patch(`/hod/faculty-leave-requests/${id}/status`, {
        status,
        notes: resolutionNotes || `Processed by HOD (${status})`
      });
      addToast(`Faculty leave request marked as ${status}`, 'success');
      setResolutionNotes('');
      // Refresh leaves
      const updated = await api.get('/hod/faculty-leave-requests');
      setLeaveRequests(updated.data);
    } catch (err) {
      console.error('Leave status update error:', err);
      addToast('Failed to update leave status', 'error');
    } finally {
      setProcessingLeaveId(null);
    }
  };

  const handleUploadDocument = async (e) => {
    e.preventDefault();
    if (!selectedFile && !textOverride.trim()) {
      addToast('Please select a file or provide notice text', 'warning');
      return;
    }

    setUploading(true);
    setIngestionResult(null);

    try {
      let res;
      if (selectedFile) {
        const formData = new FormData();
        formData.append('file', selectedFile);
        if (textOverride) formData.append('textOverride', textOverride);
        if (sectionHint) formData.append('sectionHint', sectionHint);
        res = await api.post('/hod/documents/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      } else {
        res = await api.post('/hod/documents/upload', {
          textOverride,
          sectionHint
        });
      }

      setIngestionResult(res.data);
      addToast(`AI processed: ${res.data.actionTaken || 'Circular routed successfully'}`, 'success');
      // refresh dashboard in background
      fetchDashboardData();
    } catch (err) {
      console.error('Ingestion failed:', err);
      addToast(err.response?.data?.error || 'Document extraction and ingestion failed', 'error');
    } finally {
      setUploading(false);
    }
  };

  const pendingLeaves = leaveRequests.filter(l => l.status === 'PENDING');
  const resolvedLeaves = leaveRequests.filter(l => l.status !== 'PENDING');

  // Filtered timetable data
  const currentClassEntries = (classTimetables && classTimetables[selectedSection]) || [];

  const selectedFacultyObj = facultyRoster.find(f => String(f.id) === String(selectedFacultyId)) || facultyRoster[0];
  const selectedFacultyName = selectedFacultyObj ? selectedFacultyObj.name : '';

  const currentFacultySchedule = (() => {
    if (!facultyTimetables) return [];
    if (typeof facultyTimetables === 'object' && !Array.isArray(facultyTimetables)) {
      if (selectedFacultyName && facultyTimetables[selectedFacultyName]) {
        return facultyTimetables[selectedFacultyName];
      }
      const key = Object.keys(facultyTimetables).find(k =>
        k.toLowerCase().includes((selectedFacultyName || '').toLowerCase()) ||
        (selectedFacultyName || '').toLowerCase().includes(k.toLowerCase())
      );
      if (key && facultyTimetables[key]) return facultyTimetables[key];
      return [];
    }
    if (Array.isArray(facultyTimetables)) {
      const match = facultyTimetables.find(f => String(f.facultyId) === String(selectedFacultyId) || f.facultyName === selectedFacultyName);
      return match ? (match.schedule || match) : [];
    }
    return [];
  })();

  const daysOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Top Header Card */}
        <div className="rounded-2xl border border-[var(--color-border)] bg-gradient-to-r from-blue-900/10 via-indigo-900/10 to-purple-900/10 dark:from-blue-950/40 dark:via-indigo-950/40 dark:to-purple-950/40 p-6 sm:p-8 backdrop-blur-sm">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div className="space-y-2">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20">
                <Building2 className="w-3.5 h-3.5" />
                Department Head Portal
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-[var(--color-foreground)] tracking-tight">
                {dashboard?.department || 'Computer Science & Engineering'}
              </h1>
              <p className="text-sm text-[var(--color-muted-foreground)] flex items-center gap-2">
                <span>Head of Department:</span>
                <span className="font-semibold text-[var(--color-foreground)]">
                  {dashboard?.hodProfile?.name || 'Dr. Arulmozhi V'}
                </span>
                <span className="text-xs px-2 py-0.5 rounded bg-[var(--color-border)]">
                  {dashboard?.hodProfile?.cabin || 'Block A - Room 100'}
                </span>
              </p>
            </div>

            {/* Quick KPI stats */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 shrink-0">
              <div className="p-3.5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-xs">
                <div className="text-xs text-[var(--color-muted-foreground)] flex items-center gap-1.5">
                  <Users className="w-3.5 h-3.5 text-blue-500" /> Faculty
                </div>
                <div className="text-xl font-bold text-[var(--color-foreground)] mt-1">
                  {dashboard?.facultyCount ?? facultyRoster.length}
                </div>
              </div>

              <div className="p-3.5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-xs">
                <div className="text-xs text-[var(--color-muted-foreground)] flex items-center gap-1.5">
                  <Layers className="w-3.5 h-3.5 text-indigo-500" /> Sections
                </div>
                <div className="text-xl font-bold text-[var(--color-foreground)] mt-1">
                  {dashboard?.sectionCount ?? 4} (A-D)
                </div>
              </div>

              <div className="p-3.5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-xs">
                <div className="text-xs text-[var(--color-muted-foreground)] flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-emerald-500" /> Today Classes
                </div>
                <div className="text-xl font-bold text-[var(--color-foreground)] mt-1">
                  {dashboard?.todayClassesCount ?? 16}
                </div>
              </div>

              <div className="p-3.5 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-xs">
                <div className="text-xs text-[var(--color-muted-foreground)] flex items-center gap-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-amber-500" /> Pending Leaves
                </div>
                <div className="text-xl font-bold text-amber-600 dark:text-amber-400 mt-1">
                  {pendingLeaves.length}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="flex border-b border-[var(--color-border)] overflow-x-auto gap-2">
          <button
            onClick={() => setActiveTab('overview')}
            className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
              activeTab === 'overview'
                ? 'border-[var(--color-primary)] text-[var(--color-primary)]'
                : 'border-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
            }`}
          >
            <Building2 className="w-4 h-4" />
            Overview & Sections
          </button>

          <button
            onClick={() => setActiveTab('timetables')}
            className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
              activeTab === 'timetables'
                ? 'border-[var(--color-primary)] text-[var(--color-primary)]'
                : 'border-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
            }`}
          >
            <Calendar className="w-4 h-4" />
            Class & Faculty Timetables
          </button>

          <button
            onClick={() => setActiveTab('faculty')}
            className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
              activeTab === 'faculty'
                ? 'border-[var(--color-primary)] text-[var(--color-primary)]'
                : 'border-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
            }`}
          >
            <UserCheck className="w-4 h-4" />
            Faculty Roster & Leave Approval
            {pendingLeaves.length > 0 && (
              <span className="px-1.5 py-0.5 text-[10px] font-bold rounded-full bg-amber-500 text-white">
                {pendingLeaves.length}
              </span>
            )}
          </button>

          <button
            onClick={() => setActiveTab('ai-ingestion')}
            className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
              activeTab === 'ai-ingestion'
                ? 'border-[var(--color-primary)] text-[var(--color-primary)]'
                : 'border-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
            }`}
          >
            <Sparkles className="w-4 h-4 text-purple-500" />
            AI Document Ingestion & Targeted Dispatcher
          </button>
        </div>

        {/* TAB 1: OVERVIEW */}
        {activeTab === 'overview' && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Mentored Sections Map */}
              <div className="lg:col-span-2">
                <Card>
                  <CardHeader className="flex items-center justify-between pb-3">
                    <div className="flex items-center gap-2">
                      <GraduationCap className="w-4 h-4 text-[var(--color-primary)]" />
                      <h3 className="font-bold text-base text-[var(--color-foreground)]">
                        Department Sections & Assigned Mentors
                      </h3>
                    </div>
                    <Badge variant="primary" size="sm">4 Active Sections</Badge>
                  </CardHeader>
                  <CardBody className="p-0">
                    <div className="overflow-x-auto">
                      <table className="w-full text-xs text-left">
                        <thead className="bg-[var(--color-border)]/20 border-y border-[var(--color-border)] text-[var(--color-muted-foreground)]">
                          <tr>
                            <th className="py-2.5 px-4 font-semibold">Section</th>
                            <th className="py-2.5 px-4 font-semibold">Semester / Year</th>
                            <th className="py-2.5 px-4 font-semibold">Faculty Mentor</th>
                            <th className="py-2.5 px-4 font-semibold">Designation</th>
                            <th className="py-2.5 px-4 font-semibold">Cabin</th>
                            <th className="py-2.5 px-4 font-semibold text-right">Action</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-[var(--color-border)]/50">
                          {facultyRoster.filter(f => f.isMentor).map((fac) => (
                            <tr key={fac.id} className="hover:bg-[var(--color-border)]/10 transition-colors">
                              <td className="py-3 px-4 font-bold text-[var(--color-primary)]">
                                Section {fac.mentorSection || fac.assignedSection}
                              </td>
                              <td className="py-3 px-4 text-[var(--color-muted-foreground)]">
                                Sem {fac.mentorSemester || '3'} (AY 2025-26)
                              </td>
                              <td className="py-3 px-4 font-semibold text-[var(--color-foreground)]">
                                {fac.name}
                              </td>
                              <td className="py-3 px-4 text-[var(--color-muted-foreground)]">
                                {fac.designation}
                              </td>
                              <td className="py-3 px-4 text-[var(--color-muted-foreground)]">
                                {fac.cabinNumber}
                              </td>
                              <td className="py-3 px-4 text-right">
                                <button
                                  onClick={() => {
                                    setSelectedSection(fac.mentorSection || fac.assignedSection);
                                    setTimetableMode('class');
                                    setActiveTab('timetables');
                                  }}
                                  className="inline-flex items-center gap-1 text-xs text-[var(--color-primary)] hover:underline font-medium"
                                >
                                  View Timetable <ArrowRight className="w-3 h-3" />
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </CardBody>
                </Card>
              </div>

              {/* Department Notice Board & Stats */}
              <div className="space-y-6">
                <Card>
                  <CardHeader className="pb-2">
                    <h3 className="font-bold text-sm text-[var(--color-foreground)] flex items-center gap-2">
                      <FileText className="w-4 h-4 text-indigo-500" />
                      Department Directives
                    </h3>
                  </CardHeader>
                  <CardBody className="space-y-3 text-xs">
                    <div className="p-3 rounded-lg border border-blue-500/20 bg-blue-500/5">
                      <div className="font-semibold text-blue-700 dark:text-blue-300">Automated Circular Scoping</div>
                      <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1">
                        Use the <strong>AI Document Ingestion</strong> tab to upload notices. The AgentX system parses document scope and automatically sends Section-specific seminars strictly to that Section's students.
                      </p>
                    </div>

                    <div className="p-3 rounded-lg border border-emerald-500/20 bg-emerald-500/5">
                      <div className="font-semibold text-emerald-700 dark:text-emerald-300">Leave Approvals</div>
                      <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1">
                        All faculty casual leave and ON-DUTY requests are routed directly to HOD for approval with substitute faculty tracking.
                      </p>
                    </div>

                    <div className="p-3 rounded-lg border border-purple-500/20 bg-purple-500/5">
                      <div className="font-semibold text-purple-700 dark:text-purple-300">Universal Timetable Transparency</div>
                      <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1">
                        Both Section schedules (Sections A-D) and individual faculty schedules are synchronized from the master database.
                      </p>
                    </div>
                  </CardBody>
                </Card>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: TIMETABLES */}
        {activeTab === 'timetables' && (
          <div className="space-y-6">
            {/* View Mode Toggle & Selectors */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)]">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setTimetableMode('class')}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    timetableMode === 'class'
                      ? 'bg-[var(--color-primary)] text-white shadow-xs'
                      : 'bg-[var(--color-border)]/50 text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                  }`}
                >
                  Class Section Timetables
                </button>
                <button
                  onClick={() => setTimetableMode('faculty')}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    timetableMode === 'faculty'
                      ? 'bg-[var(--color-primary)] text-white shadow-xs'
                      : 'bg-[var(--color-border)]/50 text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                  }`}
                >
                  Faculty Individual Timetables
                </button>
              </div>

              {/* Selector according to mode */}
              <div className="flex items-center gap-3">
                {timetableMode === 'class' ? (
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-[var(--color-muted-foreground)] font-medium">Select Section:</span>
                    <div className="flex gap-1.5">
                      {['A', 'B', 'C', 'D'].map(sec => (
                        <button
                          key={sec}
                          onClick={() => setSelectedSection(sec)}
                          className={`w-8 h-8 rounded-lg text-xs font-bold transition-all ${
                            selectedSection === sec
                              ? 'bg-[var(--color-primary)] text-white ring-2 ring-[var(--color-primary)]/30'
                              : 'border border-[var(--color-border)] hover:bg-[var(--color-border)]/50 text-[var(--color-foreground)]'
                          }`}
                        >
                          {sec}
                        </button>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-[var(--color-muted-foreground)] font-medium">Select Faculty:</span>
                    <select
                      value={selectedFacultyId}
                      onChange={(e) => setSelectedFacultyId(e.target.value)}
                      className="px-3 py-1.5 text-xs rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] text-[var(--color-foreground)] font-medium focus:ring-1 focus:ring-[var(--color-primary)]"
                    >
                      {facultyRoster.map(f => (
                        <option key={f.id} value={f.id}>
                          {f.name} ({f.designation})
                        </option>
                      ))}
                    </select>
                  </div>
                )}
              </div>
            </div>

            {/* Timetable Grid View */}
            <Card>
              <CardHeader className="flex items-center justify-between pb-3">
                <div className="flex items-center gap-2">
                  <Calendar className="w-4 h-4 text-[var(--color-primary)]" />
                  <h3 className="font-bold text-base text-[var(--color-foreground)]">
                    {timetableMode === 'class'
                      ? `Master Weekly Schedule — CSE Section ${selectedSection}`
                      : `Individual Schedule — ${selectedFacultyName || 'Faculty Member'}`}
                  </h3>
                </div>
                <Badge variant="info" size="sm">
                  {timetableMode === 'class' ? `${currentClassEntries.length} Classes / Week` : `${currentFacultySchedule.length} Periods`}
                </Badge>
              </CardHeader>
              <CardBody className="p-0">
                <div className="overflow-x-auto">
                  <table className="w-full text-xs text-left">
                    <thead className="bg-[var(--color-border)]/20 border-y border-[var(--color-border)] text-[var(--color-muted-foreground)]">
                      <tr>
                        <th className="py-2.5 px-4 font-semibold">Day</th>
                        <th className="py-2.5 px-4 font-semibold">Time Slot</th>
                        <th className="py-2.5 px-4 font-semibold">Subject</th>
                        <th className="py-2.5 px-4 font-semibold">Venue / Room</th>
                        <th className="py-2.5 px-4 font-semibold">
                          {timetableMode === 'class' ? 'Faculty Instructor' : 'Target Section'}
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--color-border)]/50">
                      {daysOrder.map(day => {
                        const entries = (timetableMode === 'class' ? currentClassEntries : currentFacultySchedule)
                          .filter(e => e.dayOfWeek?.toLowerCase() === day.toLowerCase());

                        if (entries.length === 0) {
                          return (
                            <tr key={day} className="text-[var(--color-muted-foreground)] opacity-60">
                              <td className="py-2.5 px-4 font-medium">{day}</td>
                              <td colSpan={4} className="py-2.5 px-4 italic">No scheduled periods</td>
                            </tr>
                          );
                        }

                        return entries.map((entry, idx) => (
                          <tr key={`${day}-${entry.id || idx}`} className="hover:bg-[var(--color-border)]/10 transition-colors">
                            {idx === 0 ? (
                              <td rowSpan={entries.length} className="py-2.5 px-4 font-bold text-[var(--color-foreground)] align-top border-r border-[var(--color-border)]/30 bg-[var(--color-border)]/5">
                                {day}
                              </td>
                            ) : null}
                            <td className="py-2.5 px-4 font-mono font-medium text-[var(--color-foreground)]">
                              {entry.startTime} - {entry.endTime}
                            </td>
                            <td className="py-2.5 px-4">
                              <div className="font-semibold text-[var(--color-foreground)]">{entry.subjectName}</div>
                              <div className="text-[10px] text-[var(--color-muted-foreground)] font-mono">{entry.subjectCode}</div>
                            </td>
                            <td className="py-2.5 px-4">
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded font-medium bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-500/20">
                                📍 {entry.classroom || 'Room CS-101'}
                              </span>
                            </td>
                            <td className="py-2.5 px-4 font-medium text-[var(--color-foreground)]">
                              {timetableMode === 'class' ? (
                                entry.facultyName || 'Staff'
                              ) : (
                                <Badge variant="primary" size="sm">Sec {entry.section || 'All'}</Badge>
                              )}
                            </td>
                          </tr>
                        ));
                      })}
                    </tbody>
                  </table>
                </div>
              </CardBody>
            </Card>
          </div>
        )}

        {/* TAB 3: FACULTY ROSTER & LEAVE APPROVAL */}
        {activeTab === 'faculty' && (
          <div className="space-y-6">
            {/* Pending Faculty Leave Requests */}
            <Card>
              <CardHeader className="flex items-center justify-between pb-3">
                <div className="flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 text-amber-500" />
                  <h3 className="font-bold text-base text-[var(--color-foreground)]">
                    Pending Faculty Leave & Permission Requests
                  </h3>
                </div>
                <Badge variant={pendingLeaves.length > 0 ? 'warning' : 'success'} size="sm">
                  {pendingLeaves.length} Pending Approval
                </Badge>
              </CardHeader>
              <CardBody className="p-0">
                {pendingLeaves.length === 0 ? (
                  <div className="p-8 text-center text-xs text-[var(--color-muted-foreground)]">
                    <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-80" />
                    All faculty leave requests have been reviewed and resolved.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-xs text-left">
                      <thead className="bg-[var(--color-border)]/20 border-y border-[var(--color-border)] text-[var(--color-muted-foreground)]">
                        <tr>
                          <th className="py-2.5 px-4 font-semibold">Faculty Member</th>
                          <th className="py-2.5 px-4 font-semibold">Type</th>
                          <th className="py-2.5 px-4 font-semibold">Duration</th>
                          <th className="py-2.5 px-4 font-semibold">Substitute Staff</th>
                          <th className="py-2.5 px-4 font-semibold">Reason</th>
                          <th className="py-2.5 px-4 font-semibold text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--color-border)]/50">
                        {pendingLeaves.map(leave => (
                          <tr key={leave.id} className="hover:bg-[var(--color-border)]/10 transition-colors">
                            <td className="py-3 px-4">
                              <div className="font-bold text-[var(--color-foreground)]">{leave.facultyName}</div>
                              <div className="text-[10px] text-[var(--color-muted-foreground)]">{leave.facultyEmail}</div>
                            </td>
                            <td className="py-3 px-4">
                              <Badge variant={leave.leaveType === 'ON_DUTY' ? 'primary' : 'warning'} size="sm">
                                {leave.leaveType}
                              </Badge>
                            </td>
                            <td className="py-3 px-4 font-mono">
                              {leave.fromDate} to {leave.toDate}
                            </td>
                            <td className="py-3 px-4 text-[var(--color-foreground)] font-medium">
                              {leave.substituteFacultyName || 'None assigned'}
                            </td>
                            <td className="py-3 px-4 text-[var(--color-muted-foreground)] max-w-xs truncate" title={leave.reason}>
                              {leave.reason}
                            </td>
                            <td className="py-3 px-4 text-right space-x-2">
                              <button
                                onClick={() => handleUpdateLeaveStatus(leave.id, 'APPROVED')}
                                disabled={processingLeaveId === leave.id}
                                className="px-2.5 py-1 text-xs font-semibold rounded bg-emerald-600 hover:bg-emerald-700 text-white transition-colors disabled:opacity-50"
                              >
                                Approve
                              </button>
                              <button
                                onClick={() => handleUpdateLeaveStatus(leave.id, 'REJECTED')}
                                disabled={processingLeaveId === leave.id}
                                className="px-2.5 py-1 text-xs font-semibold rounded bg-red-600 hover:bg-red-700 text-white transition-colors disabled:opacity-50"
                              >
                                Reject
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </CardBody>
            </Card>

            {/* Department Faculty Roster */}
            <Card>
              <CardHeader className="flex items-center justify-between pb-3">
                <div className="flex items-center gap-2">
                  <Users className="w-4 h-4 text-[var(--color-primary)]" />
                  <h3 className="font-bold text-base text-[var(--color-foreground)]">
                    Department Faculty Directory ({facultyRoster.length} Members)
                  </h3>
                </div>
              </CardHeader>
              <CardBody className="p-0">
                <div className="overflow-x-auto">
                  <table className="w-full text-xs text-left">
                    <thead className="bg-[var(--color-border)]/20 border-y border-[var(--color-border)] text-[var(--color-muted-foreground)]">
                      <tr>
                        <th className="py-2.5 px-4 font-semibold">Faculty Name</th>
                        <th className="py-2.5 px-4 font-semibold">Employee ID</th>
                        <th className="py-2.5 px-4 font-semibold">Designation</th>
                        <th className="py-2.5 px-4 font-semibold">Role / Mentorship</th>
                        <th className="py-2.5 px-4 font-semibold">Cabin</th>
                        <th className="py-2.5 px-4 font-semibold">Email</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--color-border)]/50">
                      {facultyRoster.map(fac => (
                        <tr key={fac.id} className="hover:bg-[var(--color-border)]/10 transition-colors">
                          <td className="py-3 px-4 font-bold text-[var(--color-foreground)]">
                            {fac.name}
                          </td>
                          <td className="py-3 px-4 font-mono text-[var(--color-muted-foreground)]">
                            {fac.employeeId}
                          </td>
                          <td className="py-3 px-4 text-[var(--color-muted-foreground)]">
                            {fac.designation}
                          </td>
                          <td className="py-3 px-4">
                            {fac.isMentor ? (
                              <Badge variant="primary" size="sm">
                                Mentor (Sec {fac.mentorSection || fac.assignedSection})
                              </Badge>
                            ) : (
                              <Badge variant="neutral" size="sm">Course Faculty</Badge>
                            )}
                          </td>
                          <td className="py-3 px-4 text-[var(--color-muted-foreground)]">
                            {fac.cabinNumber}
                          </td>
                          <td className="py-3 px-4 font-mono text-[var(--color-muted-foreground)]">
                            {fac.email}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </CardBody>
            </Card>

            {/* Resolved Leave History */}
            {resolvedLeaves.length > 0 && (
              <Card>
                <CardHeader className="pb-2">
                  <h3 className="font-bold text-sm text-[var(--color-foreground)]">
                    Resolved Leave History ({resolvedLeaves.length})
                  </h3>
                </CardHeader>
                <CardBody className="p-0">
                  <div className="overflow-x-auto">
                    <table className="w-full text-xs text-left">
                      <thead className="bg-[var(--color-border)]/20 border-y border-[var(--color-border)] text-[var(--color-muted-foreground)]">
                        <tr>
                          <th className="py-2 px-4 font-semibold">Faculty</th>
                          <th className="py-2 px-4 font-semibold">Type</th>
                          <th className="py-2 px-4 font-semibold">Dates</th>
                          <th className="py-2 px-4 font-semibold">Reason</th>
                          <th className="py-2 px-4 font-semibold">Status</th>
                          <th className="py-2 px-4 font-semibold">HOD Notes</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--color-border)]/50">
                        {resolvedLeaves.map(r => (
                          <tr key={r.id}>
                            <td className="py-2 px-4 font-medium text-[var(--color-foreground)]">{r.facultyName}</td>
                            <td className="py-2 px-4">{r.leaveType}</td>
                            <td className="py-2 px-4 font-mono">{r.fromDate} - {r.toDate}</td>
                            <td className="py-2 px-4 text-[var(--color-muted-foreground)] truncate max-w-xs">{r.reason}</td>
                            <td className="py-2 px-4">
                              <Badge variant={r.status === 'APPROVED' ? 'success' : 'danger'} size="sm">
                                {r.status}
                              </Badge>
                            </td>
                            <td className="py-2 px-4 text-[var(--color-muted-foreground)]">{r.resolutionNotes || '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </CardBody>
              </Card>
            )}
          </div>
        )}

        {/* TAB 4: AI DOCUMENT INGESTION & TARGETED DISPATCHER */}
        {activeTab === 'ai-ingestion' && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Upload Form */}
              <Card>
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <Sparkles className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                    <div>
                      <h3 className="font-bold text-base text-[var(--color-foreground)]">
                        AI Document Ingestion & Targeted Dispatcher
                      </h3>
                      <p className="text-xs text-[var(--color-muted-foreground)]">
                        Upload notice PDF or image. The AI classifies circular scope and routes notices accurately.
                      </p>
                    </div>
                  </div>
                </CardHeader>
                <CardBody>
                  <form onSubmit={handleUploadDocument} className="space-y-4">
                    {/* File Upload Box */}
                    <div className="border-2 border-dashed border-[var(--color-border)] rounded-xl p-6 text-center hover:border-[var(--color-primary)] transition-colors">
                      <FileUp className="w-10 h-10 text-[var(--color-muted-foreground)] mx-auto mb-2" />
                      <p className="text-xs font-semibold text-[var(--color-foreground)]">
                        {selectedFile ? selectedFile.name : 'Choose PDF or Image circular file'}
                      </p>
                      <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1">
                        Supports PDF, PNG, JPG documents
                      </p>
                      <input
                        type="file"
                        accept=".pdf,image/*,.txt"
                        onChange={(e) => setSelectedFile(e.target.files[0] || null)}
                        className="mt-3 text-xs"
                      />
                    </div>

                    {/* Direct Text input option */}
                    <div>
                      <label className="block text-xs font-semibold text-[var(--color-foreground)] mb-1">
                        Notice / Circular Text (Direct paste or override)
                      </label>
                      <textarea
                        rows={4}
                        placeholder="e.g.: SEMINAR NOTICE: Guest lecture on Agentic AI Systems for Section A students on Monday 10:00 AM at Seminar Hall 1..."
                        value={textOverride}
                        onChange={(e) => setTextOverride(e.target.value)}
                        className="w-full text-xs p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] focus:ring-1 focus:ring-[var(--color-primary)]"
                      />
                    </div>

                    {/* Section Hint override */}
                    <div>
                      <label className="block text-xs font-semibold text-[var(--color-foreground)] mb-1">
                        Target Section Hint (Optional — AI auto-detects by default)
                      </label>
                      <select
                        value={sectionHint}
                        onChange={(e) => setSectionHint(e.target.value)}
                        className="w-full text-xs p-2.5 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)]"
                      >
                        <option value="">Auto-Detect via AI (Recommended)</option>
                        <option value="A">Section A (Only Sec A students receive alerts)</option>
                        <option value="B">Section B (Only Sec B students receive alerts)</option>
                        <option value="C">Section C (Only Sec C students receive alerts)</option>
                        <option value="D">Section D (Only Sec D students receive alerts)</option>
                      </select>
                    </div>

                    {/* Quick Demo Pre-fills */}
                    <div className="space-y-1.5 pt-1">
                      <span className="text-[11px] font-semibold text-[var(--color-muted-foreground)]">
                        Quick Demo Pre-fills:
                      </span>
                      <div className="flex flex-wrap gap-2">
                        <button
                          type="button"
                          onClick={() => {
                            setTextOverride("DEPARTMENT NOTICE: Guest seminar on Cloud Architecture for Section A students on Friday 10:00 AM at Seminar Hall 1.");
                            setSectionHint("A");
                          }}
                          className="px-2.5 py-1 text-[11px] rounded bg-purple-500/10 text-purple-700 dark:text-purple-300 border border-purple-500/20 hover:bg-purple-500/20"
                        >
                          🧪 Section A Seminar
                        </button>

                        <button
                          type="button"
                          onClick={() => {
                            setTextOverride("INSTITUTIONAL CIRCULAR: College will remain closed on Friday for Annual Day celebrations. All classes suspended.");
                            setSectionHint("");
                          }}
                          className="px-2.5 py-1 text-[11px] rounded bg-blue-500/10 text-blue-700 dark:text-blue-300 border border-blue-500/20 hover:bg-blue-500/20"
                        >
                          🧪 Leave Circular (Broadcast)
                        </button>
                      </div>
                    </div>

                    <Button
                      type="submit"
                      loading={uploading}
                      className="w-full"
                      icon={UploadCloud}
                    >
                      {uploading ? 'Analyzing Document with AI...' : 'Analyze & Dispatch Document'}
                    </Button>
                  </form>
                </CardBody>
              </Card>

              {/* Ingestion & Scoping Result Card */}
              <div className="space-y-6">
                <Card>
                  <CardHeader>
                    <div className="flex items-center gap-2">
                      <CheckCircle2 className="w-5 h-5 text-emerald-500" />
                      <h3 className="font-bold text-base text-[var(--color-foreground)]">
                        Extraction & Routing Intelligence
                      </h3>
                    </div>
                  </CardHeader>
                  <CardBody>
                    {ingestionResult ? (
                      <div className="space-y-4">
                        <div className="p-4 rounded-xl border border-emerald-500/30 bg-emerald-500/10 space-y-2">
                          <div className="flex items-center justify-between">
                            <Badge variant="success" size="sm">✓ Ingestion Completed</Badge>
                            <span className="text-[11px] font-mono text-[var(--color-muted-foreground)]">
                              {ingestionResult.latencyMs}ms
                            </span>
                          </div>
                          <h4 className="font-bold text-sm text-[var(--color-foreground)]">
                            {ingestionResult.actionTaken}
                          </h4>
                          <p className="text-xs text-[var(--color-muted-foreground)]">
                            {ingestionResult.summary}
                          </p>
                        </div>

                        <div className="grid grid-cols-2 gap-3 text-xs">
                          <div className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)]">
                            <span className="text-[10px] text-[var(--color-muted-foreground)] block">Document Classification</span>
                            <span className="font-bold text-[var(--color-foreground)]">
                              {ingestionResult.documentType}
                            </span>
                          </div>

                          <div className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)]">
                            <span className="text-[10px] text-[var(--color-muted-foreground)] block">Target Scope</span>
                            <span className="font-bold text-[var(--color-primary)]">
                              {ingestionResult.targetSection ? `Section ${ingestionResult.targetSection} ONLY` : 'ALL Students & Faculty'}
                            </span>
                          </div>

                          <div className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)]">
                            <span className="text-[10px] text-[var(--color-muted-foreground)] block">Venue / Room</span>
                            <span className="font-semibold text-[var(--color-foreground)]">
                              {ingestionResult.venue || 'Campus Wide'}
                            </span>
                          </div>

                          <div className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)]">
                            <span className="text-[10px] text-[var(--color-muted-foreground)] block">Recipients Notified</span>
                            <span className="font-bold text-emerald-600 dark:text-emerald-400">
                              {ingestionResult.recipientsNotified} users
                            </span>
                          </div>
                        </div>
                      </div>
                    ) : (
                      <div className="p-8 text-center text-xs text-[var(--color-muted-foreground)] space-y-2">
                        <Sparkles className="w-8 h-8 text-purple-400 mx-auto opacity-70" />
                        <p className="font-medium text-[var(--color-foreground)]">
                          Waiting for Document Upload
                        </p>
                        <p className="text-[11px] leading-relaxed">
                          Once uploaded, the Document Ingestion Agent analyzes keywords and syntax. Leave circulars are broadcast to all users, whereas Section notices (e.g. Section A) are isolated strictly to students of that section.
                        </p>
                      </div>
                    )}
                  </CardBody>
                </Card>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default HodPortal;
