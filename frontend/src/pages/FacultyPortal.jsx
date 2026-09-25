import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
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
import {
  BookOpen, Users, Plus, Trash2, Calendar, Clock, MapPin, GraduationCap,
  CheckCircle, ArrowRight, AlertCircle, Wrench, CheckCircle2, MessageSquare,
  AlertTriangle, Edit2, ShieldAlert
} from 'lucide-react';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

export const FacultyPortal = () => {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [activeTab, setActiveTab] = useState('overview');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  // Complaints State
  const [complaints, setComplaints] = useState([]);
  const [complaintsLoading, setComplaintsLoading] = useState(false);
  const [complaintFilter, setComplaintFilter] = useState('ALL'); // 'ALL' | 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
  const [resolvingId, setResolvingId] = useState(null);
  const [resolutionText, setResolutionText] = useState('');
  const [savingResolution, setSavingResolution] = useState(false);

  // Faculty Filing Complaint Form
  const [showFileComplaint, setShowFileComplaint] = useState(false);
  const [facultyComplaintForm, setFacultyComplaintForm] = useState({
    category: 'CAMPUS_FACILITIES',
    location: 'CS-204',
    urgency: 'HIGH',
    description: '',
  });
  const [submittingComplaint, setSubmittingComplaint] = useState(false);

  // Personal Faculty Timetable State
  const [myTimetable, setMyTimetable] = useState([]);
  const [loadingMyTimetable, setLoadingMyTimetable] = useState(false);
  const [myDayFilter, setMyDayFilter] = useState('ALL');

  // Mentor Section Timetable Editor State
  const [mentorSections, setMentorSections] = useState([]);
  const [selectedSectionId, setSelectedSectionId] = useState(null);
  const [sectionTimetable, setSectionTimetable] = useState([]);
  const [loadingSectionTimetable, setLoadingSectionTimetable] = useState(false);
  const [conflictWarnings, setConflictWarnings] = useState([]);

  // Add / Edit Section Period Form
  const [showAddSectionPeriod, setShowAddSectionPeriod] = useState(false);
  const [periodForm, setPeriodForm] = useState({
    subjectCode: '',
    subjectName: '',
    facultyName: '',
    dayOfWeek: 'Monday',
    startTime: '09:00 AM',
    endTime: '10:00 AM',
    classroom: 'CS-204',
  });
  const [submittingPeriod, setSubmittingPeriod] = useState(false);
  const [editingPeriodId, setEditingPeriodId] = useState(null);

  // Timetable Add Form (legacy overview modal)
  const [showAddTimetable, setShowAddTimetable] = useState(false);
  const [timetableForm, setTimetableForm] = useState({
    subjectCode: '',
    subjectName: '',
    dayOfWeek: 'Monday',
    startTime: '10:00 AM',
    endTime: '11:30 AM',
    classroom: 'CS-204',
  });
  const [submittingTimetable, setSubmittingTimetable] = useState(false);

  const fetchFacultyData = async () => {
    try {
      const res = await api.get('/faculty/dashboard');
      setData(res.data);
    } catch (err) {
      console.error(err);
      addToast('Error loading faculty profile.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const fetchComplaints = async () => {
    setComplaintsLoading(true);
    try {
      const res = await api.get('/faculty/complaints');
      setComplaints(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setComplaintsLoading(false);
    }
  };

  const fetchMyTimetable = async () => {
    setLoadingMyTimetable(true);
    try {
      const res = await api.get('/faculty/my-timetable');
      setMyTimetable(res.data || []);
    } catch (err) {
      console.error('Failed to load personal timetable', err);
    } finally {
      setLoadingMyTimetable(false);
    }
  };

  const fetchMentorSections = async () => {
    try {
      const res = await api.get('/faculty/mentor-sections');
      const list = res.data || [];
      setMentorSections(list);
      if (list.length > 0) {
        setSelectedSectionId(prev => prev || list[0].id);
        fetchSectionTimetable(list[0].id);
      }
    } catch (err) {
      console.error('Failed to load mentor sections', err);
    }
  };

  const fetchSectionTimetable = async (secId) => {
    if (!secId) return;
    setLoadingSectionTimetable(true);
    try {
      const res = await api.get(`/faculty/section-timetable/${secId}`);
      setSectionTimetable(res.data || []);
    } catch (err) {
      console.error('Failed to load section timetable', err);
    } finally {
      setLoadingSectionTimetable(false);
    }
  };

  useEffect(() => {
    fetchFacultyData();
    fetchComplaints();
    fetchMyTimetable();
    fetchMentorSections();
  }, []);

  const handleSaveSectionPeriod = async (e) => {
    e.preventDefault();
    if (!selectedSectionId) {
      addToast('No section selected for timetable management.', 'error');
      return;
    }
    setSubmittingPeriod(true);
    try {
      let res;
      if (editingPeriodId) {
        res = await api.put(`/faculty/section-timetable/${selectedSectionId}/${editingPeriodId}`, periodForm);
        addToast('Timetable period updated successfully!', 'success');
      } else {
        res = await api.post(`/faculty/section-timetable/${selectedSectionId}`, periodForm);
        addToast('New period added to section timetable!', 'success');
      }

      if (res.data?.hasConflicts && res.data?.conflicts?.length > 0) {
        setConflictWarnings(res.data.conflicts);
        addToast(`⚠️ Schedule conflict detected (${res.data.conflicts.length} issue(s)). Check the warning banner.`, 'warning');
      } else {
        setConflictWarnings([]);
      }

      setShowAddSectionPeriod(false);
      setEditingPeriodId(null);
      setPeriodForm({
        subjectCode: '',
        subjectName: '',
        facultyName: user ? `${user.firstName} ${user.lastName}` : '',
        dayOfWeek: 'Monday',
        startTime: '09:00 AM',
        endTime: '10:00 AM',
        classroom: 'CS-204',
      });
      fetchSectionTimetable(selectedSectionId);
      fetchMyTimetable();
      fetchFacultyData();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to save timetable period.';
      addToast(msg, 'error');
    } finally {
      setSubmittingPeriod(false);
    }
  };

  const handleDeleteSectionPeriod = async (entryId) => {
    if (!window.confirm('Delete this period from the section timetable?')) return;
    try {
      await api.delete(`/faculty/section-timetable/${selectedSectionId}/${entryId}`);
      addToast('Period removed from timetable.', 'info');
      fetchSectionTimetable(selectedSectionId);
      fetchMyTimetable();
      fetchFacultyData();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to remove period.';
      addToast(msg, 'error');
    }
  };

  const handleOpenEditPeriod = (period) => {
    setEditingPeriodId(period.id);
    setPeriodForm({
      subjectCode: period.subjectCode || '',
      subjectName: period.subjectName || '',
      facultyName: period.facultyName || '',
      dayOfWeek: period.dayOfWeek || 'Monday',
      startTime: period.startTime || '09:00 AM',
      endTime: period.endTime || '10:00 AM',
      classroom: period.classroom || 'CS-204',
    });
    setShowAddSectionPeriod(true);
  };

  const handleUpdateComplaintStatus = async (id, newStatus, notes) => {
    setSavingResolution(true);
    try {
      await api.patch(`/faculty/complaints/${id}/status`, {
        status: newStatus,
        resolutionNotes: notes || undefined,
      });
      addToast(`Grievance updated to ${newStatus}.`, 'success');
      setResolvingId(null);
      setResolutionText('');
      fetchComplaints();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update grievance.';
      addToast(msg, 'error');
    } finally {
      setSavingResolution(false);
    }
  };

  const handleFacultySubmitComplaint = async (e) => {
    e.preventDefault();
    if (!facultyComplaintForm.description.trim()) return;

    setSubmittingComplaint(true);
    try {
      const res = await api.post('/faculty/complaints', {
        category: facultyComplaintForm.category,
        location: facultyComplaintForm.location,
        urgency: facultyComplaintForm.urgency,
        description: facultyComplaintForm.description.trim(),
      });
      addToast(`Facility issue #${res.data.ticketNumber} reported to Administration!`, 'success');
      setShowFileComplaint(false);
      setFacultyComplaintForm({
        category: 'CAMPUS_FACILITIES',
        location: 'CS-204',
        urgency: 'HIGH',
        description: '',
      });
      fetchComplaints();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to report facility issue.';
      addToast(msg, 'error');
    } finally {
      setSubmittingComplaint(false);
    }
  };

  const handleCreateTimetable = async (e) => {
    e.preventDefault();
    if (!data?.assignedDepartment || !data?.assignedSection) {
      addToast('You must be assigned to a department and section to create timetables.', 'error');
      return;
    }

    setSubmittingTimetable(true);
    try {
      await api.post('/faculty/timetable', {
        department: data.assignedDepartment,
        section: data.assignedSection,
        ...timetableForm,
      });
      addToast('Timetable entry created successfully!', 'success');
      setShowAddTimetable(false);
      setTimetableForm({
        subjectCode: '',
        subjectName: '',
        dayOfWeek: 'Monday',
        startTime: '10:00 AM',
        endTime: '11:30 AM',
        classroom: 'CS-204',
      });
      fetchFacultyData();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to add timetable entry.';
      addToast(msg, 'error');
    } finally {
      setSubmittingTimetable(false);
    }
  };

  const handleDeleteTimetable = async (id) => {
    if (!window.confirm('Delete this timetable entry?')) return;
    try {
      await api.delete(`/faculty/timetable/${id}`);
      addToast('Timetable entry deleted.', 'info');
      fetchFacultyData();
    } catch (err) {
      addToast('Failed to delete timetable entry.', 'error');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[var(--color-background)]">
        <Navbar />
        <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
          <LoadingSpinner size="lg" message="Loading faculty portal..." />
        </div>
      </div>
    );
  }

  const mentees = data?.mentees || [];
  const timetables = data?.timetables || [];
  const assignedDept = data?.assignedDepartment;
  const assignedSec = data?.assignedSection;

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col">
      <Navbar />
      <div className="flex-1 flex flex-col md:flex-row">
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />

        <main className="flex-1 p-6 md:p-8 overflow-y-auto max-w-7xl">
          <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-[var(--color-border)]">
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-bold text-[var(--color-foreground)] tracking-tight">
                  Welcome, {user?.firstName} {user?.lastName}
                </h1>
                <Badge variant="primary" size="sm">{data?.designation || 'Faculty'}</Badge>
              </div>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-1 flex items-center gap-2">
                <span>Emp ID: <strong className="text-[var(--color-foreground)] font-mono">{data?.employeeId}</strong></span>
                <span>•</span>
                <span>Dept: <strong className="text-[var(--color-foreground)]">{data?.department}</strong></span>
                <span>•</span>
                <span>Cabin: <strong className="text-[var(--color-foreground)]">{data?.cabinNumber}</strong></span>
              </p>
            </div>

            {/* Mentor Section Badge */}
            <div className="p-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center">
                  <GraduationCap className="w-5 h-5" />
                </div>
                <div className="text-xs">
                  <p className="text-[10px] uppercase font-bold tracking-wider text-[var(--color-muted-foreground)]">
                    Mentorship Assignment
                  </p>
                  {assignedDept && assignedSec ? (
                    <p className="font-bold text-[var(--color-foreground)]">
                      {assignedDept} • Section {assignedSec}
                    </p>
                  ) : (
                    <p className="text-[11px] text-amber-600 dark:text-amber-400 font-medium">
                      No section assigned by Admin
                    </p>
                  )}
                </div>
              </div>
              {assignedDept && assignedSec && (
                <Link to="/faculty/class">
                  <Button size="sm" variant="outline" icon={ArrowRight}>
                    Class Roster
                  </Button>
                </Link>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              title="Assigned Section Mentees"
              value={'' + mentees.length}
              subtitle={assignedSec ? `Enrolled in Section ${assignedSec}` : 'Unassigned'}
              icon={Users}
              badge={<Badge variant={mentees.length > 0 ? 'success' : 'neutral'}>{mentees.length > 0 ? 'Active Roster' : 'Empty'}</Badge>}
            />
            <StatCard
              title="My Teaching Lectures"
              value={'' + myTimetable.length}
              subtitle="Personal lectures across sections"
              icon={Clock}
              badge={
                <Badge
                  variant="primary"
                  className="cursor-pointer"
                  onClick={() => setActiveTab('myTimetable')}
                >
                  My Schedule
                </Badge>
              }
            />
            <StatCard
              title="Section Timetable"
              value={'' + (sectionTimetable.length || timetables.length)}
              subtitle={assignedSec ? `Section ${assignedSec} weekly schedule` : 'Section Schedule'}
              icon={BookOpen}
              badge={
                <Badge
                  variant="info"
                  className="cursor-pointer"
                  onClick={() => setActiveTab('sectionTimetable')}
                >
                  Edit Timetable
                </Badge>
              }
            />
            <StatCard
              title="Student Grievances"
              value={'' + complaints.filter(c => c.status !== 'RESOLVED' && c.routedTo === 'FACULTY_MENTOR').length}
              subtitle="Pending review as Mentor"
              icon={AlertCircle}
              badge={
                <Badge
                  variant={complaints.filter(c => c.status !== 'RESOLVED').length > 0 ? 'warning' : 'success'}
                  className="cursor-pointer"
                  onClick={() => setActiveTab('complaints')}
                >
                  {complaints.filter(c => c.status !== 'RESOLVED').length > 0 ? 'Action Needed' : 'All Resolved'}
                </Badge>
              }
            />
          </div>

          {activeTab === 'agent' ? (
            <div className="max-w-3xl">
              <AgentXAssistant onActionCompleted={() => fetchFacultyData()} />
            </div>
          ) : activeTab === 'complaints' ? (
            /* ================= COMPLAINTS & GRIEVANCES MANAGEMENT VIEW ================= */
            <div className="space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                  <h2 className="text-lg font-bold text-[var(--color-foreground)]">
                    Mentee Grievances & Infrastructure Support Desk
                  </h2>
                  <p className="text-xs text-[var(--color-muted-foreground)]">
                    Review complaints submitted by your students or report departmental facility issues to Administration
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    icon={Wrench}
                    onClick={() => setShowFileComplaint(!showFileComplaint)}
                  >
                    {showFileComplaint ? 'Close Form' : 'Report Facility Issue to Admin'}
                  </Button>
                </div>
              </div>

              {/* Faculty Report Facility Issue Form */}
              {showFileComplaint && (
                <Card>
                  <CardHeader
                    title="Report Facility / Maintenance Issue to Administration"
                    subtitle="Tickets filed here are routed directly to Admin Operations for resolution"
                  />
                  <CardBody className="p-5">
                    <form onSubmit={handleFacultySubmitComplaint} className="space-y-4">
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div>
                          <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Issue Category</label>
                          <select
                            value={facultyComplaintForm.category}
                            onChange={(e) => setFacultyComplaintForm({ ...facultyComplaintForm, category: e.target.value })}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2 text-[var(--color-foreground)]"
                          >
                            <option value="CAMPUS_FACILITIES">Classroom & Lab Equipment</option>
                            <option value="ELECTRICAL_MAINTENANCE">Electrical / AC / Projector</option>
                            <option value="NETWORK_IT">Wi-Fi & IT Infrastructure</option>
                            <option value="ACADEMIC_ADMIN">Academic Administration</option>
                          </select>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Room / Lab Location</label>
                          <Input
                            placeholder="e.g. CS-204, Systems Lab 2"
                            value={facultyComplaintForm.location}
                            onChange={(e) => setFacultyComplaintForm({ ...facultyComplaintForm, location: e.target.value })}
                            required
                          />
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Urgency</label>
                          <select
                            value={facultyComplaintForm.urgency}
                            onChange={(e) => setFacultyComplaintForm({ ...facultyComplaintForm, urgency: e.target.value })}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2 text-[var(--color-foreground)]"
                          >
                            <option value="HIGH">High (Lecture Blocking)</option>
                            <option value="URGENT">Urgent / Critical</option>
                            <option value="MEDIUM">Medium Priority</option>
                            <option value="LOW">Low</option>
                          </select>
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Detailed Description</label>
                        <textarea
                          rows={3}
                          value={facultyComplaintForm.description}
                          onChange={(e) => setFacultyComplaintForm({ ...facultyComplaintForm, description: e.target.value })}
                          placeholder="Describe the issue in detail..."
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2.5 text-[var(--color-foreground)] focus:ring-1 focus:ring-[var(--color-primary)] outline-none"
                          required
                        />
                      </div>
                      <div className="flex justify-end gap-2">
                        <Button size="sm" variant="ghost" onClick={() => setShowFileComplaint(false)}>
                          Cancel
                        </Button>
                        <Button size="sm" type="submit" loading={submittingComplaint}>
                          Submit Ticket to Admin
                        </Button>
                      </div>
                    </form>
                  </CardBody>
                </Card>
              )}

              {/* Status Filter Tabs */}
              <div className="flex items-center gap-1.5 border-b border-[var(--color-border)] pb-2 overflow-x-auto no-scrollbar">
                {[
                  { id: 'ALL', label: 'All Complaints' },
                  { id: 'OPEN', label: 'Open' },
                  { id: 'IN_PROGRESS', label: 'In Progress' },
                  { id: 'RESOLVED', label: 'Resolved' },
                ].map((f) => {
                  const count = f.id === 'ALL'
                    ? complaints.length
                    : complaints.filter(c => c.status === f.id).length;
                  return (
                    <button
                      key={f.id}
                      onClick={() => setComplaintFilter(f.id)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1.5 ${
                        complaintFilter === f.id
                          ? 'bg-[var(--color-primary)] text-white shadow-xs'
                          : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] hover:bg-[var(--color-muted)]/30'
                      }`}
                    >
                      <span>{f.label}</span>
                      <span className={`px-1.5 py-0.2 rounded-full text-[10px] ${complaintFilter === f.id ? 'bg-white/20 text-white' : 'bg-[var(--color-muted)] text-[var(--color-foreground)]'}`}>
                        {count}
                      </span>
                    </button>
                  );
                })}
              </div>

              {/* Complaints List */}
              <Card>
                <CardHeader
                  title={`Grievance Records (${complaints.filter(c => complaintFilter === 'ALL' || c.status === complaintFilter).length})`}
                  subtitle="Complaints routed to you as Section Mentor and your departmental reports"
                  action={
                    <Button size="sm" variant="ghost" onClick={fetchComplaints}>
                      Refresh
                    </Button>
                  }
                />
                <CardBody className="p-0">
                  {complaints.filter(c => complaintFilter === 'ALL' || c.status === complaintFilter).length === 0 ? (
                    <div className="p-10 text-center text-xs text-[var(--color-muted-foreground)]">
                      <CheckCircle2 className="w-8 h-8 mx-auto mb-2 text-emerald-500 opacity-80" />
                      <p className="font-semibold text-[var(--color-foreground)]">No Complaints Found</p>
                      <p className="mt-1">There are no tickets matching the "{complaintFilter}" filter.</p>
                    </div>
                  ) : (
                    <div className="divide-y divide-[var(--color-border)]">
                      {complaints
                        .filter(c => complaintFilter === 'ALL' || c.status === complaintFilter)
                        .map((c) => (
                          <div key={c.id} className="p-5 flex flex-col gap-3">
                            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                              <div className="flex flex-wrap items-center gap-2">
                                <span className="font-mono text-xs font-bold text-[var(--color-primary)]">
                                  {c.ticketNumber}
                                </span>
                                <Badge
                                  variant={
                                    c.status === 'RESOLVED' ? 'success' :
                                    c.status === 'IN_PROGRESS' ? 'info' : 'warning'
                                  }
                                  size="sm"
                                >
                                  {c.status}
                                </Badge>
                                <Badge variant="neutral" size="sm">
                                  {c.category}
                                </Badge>
                                <Badge variant={c.submitterRole === 'STUDENT' ? 'primary' : 'outline'} size="sm">
                                  Submitter: {c.submitterName || 'Student'} ({c.submitterRole || 'STUDENT'})
                                </Badge>
                                <Badge variant="neutral" size="sm">
                                  Routed To: {c.routedTo || 'FACULTY_MENTOR'}
                                </Badge>
                              </div>
                              <span className="text-[11px] text-[var(--color-muted-foreground)] font-mono">
                                {c.createdAt ? c.createdAt.substring(0, 10) : 'Recent'}
                              </span>
                            </div>

                            <p className="text-xs text-[var(--color-foreground)] font-medium">
                              {c.description}
                            </p>

                            <div className="flex flex-wrap items-center justify-between gap-3 text-[11px] text-[var(--color-muted-foreground)] pt-1">
                              <div className="flex items-center gap-3">
                                <span>Location: <strong className="text-[var(--color-foreground)]">{c.location}</strong></span>
                                <span>•</span>
                                <span>Urgency: <strong className="text-[var(--color-foreground)]">{c.urgency}</strong></span>
                                {c.department && (
                                  <>
                                    <span>•</span>
                                    <span>Dept: <strong className="text-[var(--color-foreground)]">{c.department}</strong></span>
                                  </>
                                )}
                              </div>

                              {/* Status Action Buttons for Mentor */}
                              {c.routedTo === 'FACULTY_MENTOR' && c.status !== 'RESOLVED' && (
                                <div className="flex items-center gap-2">
                                  {c.status === 'OPEN' && (
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => handleUpdateComplaintStatus(c.id, 'IN_PROGRESS')}
                                    >
                                      Mark In Progress
                                    </Button>
                                  )}
                                  <Button
                                    size="sm"
                                    onClick={() => {
                                      setResolvingId(resolvingId === c.id ? null : c.id);
                                      setResolutionText(c.resolutionNotes || '');
                                    }}
                                  >
                                    Resolve Ticket
                                  </Button>
                                </div>
                              )}
                            </div>

                            {/* Resolution Notes Display */}
                            {c.resolutionNotes && (
                              <div className="p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-xs text-emerald-800 dark:text-emerald-300">
                                <span className="font-bold">Resolution Notes: </span>
                                {c.resolutionNotes}
                              </div>
                            )}

                            {/* Inline Resolution Notes Box */}
                            {resolvingId === c.id && (
                              <div className="p-4 rounded-xl border border-[var(--color-primary)]/40 bg-[var(--color-background)] space-y-3 mt-2">
                                <p className="text-xs font-bold text-[var(--color-foreground)]">
                                  Resolve Grievance #{c.ticketNumber}
                                </p>
                                <textarea
                                  rows={2}
                                  value={resolutionText}
                                  onChange={(e) => setResolutionText(e.target.value)}
                                  placeholder="Provide resolution details (e.g. Technician replaced projector cable in CS-204; verified working)..."
                                  className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2 text-[var(--color-foreground)] focus:ring-1 focus:ring-[var(--color-primary)] outline-none"
                                />
                                <div className="flex justify-end gap-2">
                                  <Button size="sm" variant="ghost" onClick={() => setResolvingId(null)}>
                                    Cancel
                                  </Button>
                                  <Button
                                    size="sm"
                                    loading={savingResolution}
                                    onClick={() => handleUpdateComplaintStatus(c.id, 'RESOLVED', resolutionText)}
                                  >
                                    Confirm Resolution
                                  </Button>
                                </div>
                              </div>
                            )}
                          </div>
                        ))}
                    </div>
                  )}
                </CardBody>
              </Card>
            </div>
          ) : activeTab === 'myTimetable' ? (
            /* ================= MY TEACHING SCHEDULE VIEW ================= */
            <div className="space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                  <h2 className="text-lg font-bold text-[var(--color-foreground)]">
                    My Personal Teaching Schedule
                  </h2>
                  <p className="text-xs text-[var(--color-muted-foreground)]">
                    Comprehensive schedule of lectures and lab sessions you are assigned to teach across all sections
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Button size="sm" variant="outline" onClick={fetchMyTimetable} loading={loadingMyTimetable}>
                    Refresh
                  </Button>
                </div>
              </div>

              {/* Day Filter Pills */}
              <div className="flex items-center gap-1.5 border-b border-[var(--color-border)] pb-2 overflow-x-auto no-scrollbar">
                {['ALL', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'].map((day) => {
                  const count = day === 'ALL'
                    ? myTimetable.length
                    : myTimetable.filter(t => t.dayOfWeek === day).length;
                  return (
                    <button
                      key={day}
                      onClick={() => setMyDayFilter(day)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1.5 ${
                        myDayFilter === day
                          ? 'bg-[var(--color-primary)] text-white shadow-xs'
                          : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] hover:bg-[var(--color-muted)]/30'
                      }`}
                    >
                      <span>{day === 'ALL' ? 'All Days' : day}</span>
                      <span className={`px-1.5 py-0.2 rounded-full text-[10px] ${myDayFilter === day ? 'bg-white/20 text-white' : 'bg-[var(--color-muted)] text-[var(--color-foreground)]'}`}>
                        {count}
                      </span>
                    </button>
                  );
                })}
              </div>

              {/* Teaching Classes List */}
              <Card>
                <CardHeader
                  title={`Teaching Periods (${myTimetable.filter(t => myDayFilter === 'ALL' || t.dayOfWeek === myDayFilter).length})`}
                  subtitle="Classes derived directly from the Master Timetable Registry"
                />
                <CardBody className="p-0">
                  {myTimetable.filter(t => myDayFilter === 'ALL' || t.dayOfWeek === myDayFilter).length === 0 ? (
                    <div className="p-12 text-center text-xs text-[var(--color-muted-foreground)]">
                      <Clock className="w-8 h-8 mx-auto mb-2 text-stone-400 opacity-60" />
                      <p className="font-semibold text-[var(--color-foreground)]">No Lectures Scheduled</p>
                      <p className="mt-1">You have no teaching periods scheduled for {myDayFilter === 'ALL' ? 'the week' : myDayFilter}.</p>
                    </div>
                  ) : (
                    <div className="divide-y divide-[var(--color-border)]">
                      {myTimetable
                        .filter(t => myDayFilter === 'ALL' || t.dayOfWeek === myDayFilter)
                        .map((t) => (
                          <div key={t.id} className="p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 hover:bg-[var(--color-background)]/50">
                            <div className="flex items-start gap-3">
                              <div className="p-2.5 rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)] font-mono text-xs font-semibold shrink-0">
                                {t.startTime} - {t.endTime}
                              </div>
                              <div>
                                <div className="flex flex-wrap items-center gap-2">
                                  <span className="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded bg-[var(--color-muted)] text-[var(--color-foreground)]">
                                    {t.subjectCode}
                                  </span>
                                  <span className="text-xs font-bold text-[var(--color-foreground)]">
                                    {t.subjectName}
                                  </span>
                                  <Badge variant="primary" size="sm">
                                    {t.department} • Sec {t.section}
                                  </Badge>
                                </div>
                                <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1 flex items-center gap-3">
                                  <span>Day: <strong className="text-[var(--color-primary)]">{t.dayOfWeek}</strong></span>
                                  <span>•</span>
                                  <span>Room: <strong className="text-[var(--color-foreground)]">{t.classroom}</strong></span>
                                  <span>•</span>
                                  <span>Faculty: <strong>{t.facultyName}</strong></span>
                                </p>
                              </div>
                            </div>
                            <div className="text-right shrink-0">
                              <Badge variant="neutral" size="sm">
                                Semester {t.semester || 5}
                              </Badge>
                            </div>
                          </div>
                        ))}
                    </div>
                  )}
                </CardBody>
              </Card>
            </div>
          ) : activeTab === 'sectionTimetable' ? (
            /* ================= MENTOR SECTION TIMETABLE EDITOR VIEW ================= */
            <div className="space-y-6">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                  <h2 className="text-lg font-bold text-[var(--color-foreground)]">
                    Mentor Section Timetable Editor
                  </h2>
                  <p className="text-xs text-[var(--color-muted-foreground)]">
                    Authorized mentors can configure class periods, assign instructors, rooms, and detect schedule overlaps with Conflict Engine
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  {mentorSections.length > 1 && (
                    <select
                      value={selectedSectionId || ''}
                      onChange={(e) => {
                        const id = Number(e.target.value);
                        setSelectedSectionId(id);
                        fetchSectionTimetable(id);
                      }}
                      className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs py-1.5 px-2.5 text-[var(--color-foreground)]"
                    >
                      {mentorSections.map((s) => (
                        <option key={s.id} value={s.id}>
                          {s.department} - Sec {s.section} (Sem {s.semester})
                        </option>
                      ))}
                    </select>
                  )}
                  <Button
                    size="sm"
                    icon={Plus}
                    onClick={() => {
                      setEditingPeriodId(null);
                      setPeriodForm({
                        subjectCode: '',
                        subjectName: '',
                        facultyName: user ? `${user.firstName} ${user.lastName}` : '',
                        dayOfWeek: 'Monday',
                        startTime: '09:00 AM',
                        endTime: '10:00 AM',
                        classroom: 'CS-204',
                      });
                      setShowAddSectionPeriod(true);
                    }}
                  >
                    Add Period
                  </Button>
                </div>
              </div>

              {/* Conflict Warnings Banner */}
              {conflictWarnings.length > 0 && (
                <div className="p-4 rounded-xl border border-amber-500/30 bg-amber-500/10 text-xs space-y-2">
                  <div className="flex items-center gap-2 font-bold text-amber-700 dark:text-amber-400">
                    <AlertTriangle className="w-4 h-4" />
                    <span>Autonomous Conflict Engine Detected Overlaps:</span>
                  </div>
                  <div className="space-y-1.5 pl-6">
                    {conflictWarnings.map((cw, i) => (
                      <div key={i} className="text-[11px] text-amber-800 dark:text-amber-300">
                        <p className="font-semibold">• [{cw.type}] {cw.message}</p>
                        <p className="text-stone-500 dark:text-stone-400 mt-0.5"><em>Recommendation:</em> {cw.recommendation}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Add / Edit Period Form Modal / Drawer */}
              {showAddSectionPeriod && (
                <Card>
                  <CardHeader
                    title={editingPeriodId ? `Edit Timetable Period` : `Add New Lecture Period`}
                    subtitle={`Section ${mentorSections.find(s => s.id === selectedSectionId)?.section || assignedSec || 'A'} Schedule`}
                  />
                  <CardBody className="p-5">
                    <form onSubmit={handleSaveSectionPeriod} className="space-y-4">
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <Input
                          label="Subject Code"
                          placeholder="e.g. CS301"
                          value={periodForm.subjectCode}
                          onChange={(e) => setPeriodForm({ ...periodForm, subjectCode: e.target.value })}
                          required
                        />
                        <Input
                          label="Subject Name"
                          placeholder="e.g. Database Management Systems"
                          value={periodForm.subjectName}
                          onChange={(e) => setPeriodForm({ ...periodForm, subjectName: e.target.value })}
                          required
                        />
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div>
                          <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">Day of Week</label>
                          <select
                            value={periodForm.dayOfWeek}
                            onChange={(e) => setPeriodForm({ ...periodForm, dayOfWeek: e.target.value })}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2 text-[var(--color-foreground)]"
                          >
                            {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                          </select>
                        </div>
                        <Input
                          label="Start Time"
                          placeholder="e.g. 09:00 AM"
                          value={periodForm.startTime}
                          onChange={(e) => setPeriodForm({ ...periodForm, startTime: e.target.value })}
                          required
                        />
                        <Input
                          label="End Time"
                          placeholder="e.g. 10:00 AM"
                          value={periodForm.endTime}
                          onChange={(e) => setPeriodForm({ ...periodForm, endTime: e.target.value })}
                          required
                        />
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <Input
                          label="Assigned Instructor / Faculty"
                          placeholder="e.g. Dr. K. Ramesh"
                          value={periodForm.facultyName}
                          onChange={(e) => setPeriodForm({ ...periodForm, facultyName: e.target.value })}
                          required
                        />
                        <Input
                          label="Classroom / Lab Location"
                          placeholder="e.g. CS-204 or Systems Lab 2"
                          value={periodForm.classroom}
                          onChange={(e) => setPeriodForm({ ...periodForm, classroom: e.target.value })}
                          required
                        />
                      </div>

                      <div className="flex justify-end gap-2 pt-2">
                        <Button size="sm" variant="ghost" onClick={() => { setShowAddSectionPeriod(false); setEditingPeriodId(null); }}>
                          Cancel
                        </Button>
                        <Button size="sm" type="submit" loading={submittingPeriod}>
                          {editingPeriodId ? 'Save Changes' : 'Create Period'}
                        </Button>
                      </div>
                    </form>
                  </CardBody>
                </Card>
              )}

              {/* Section Periods Table */}
              <Card>
                <CardHeader
                  title={`Section Timetable Periods (${sectionTimetable.length})`}
                  subtitle={`Weekly timetable for ${mentorSections.find(s => s.id === selectedSectionId)?.department || assignedDept || 'Department'} - Section ${mentorSections.find(s => s.id === selectedSectionId)?.section || assignedSec || 'A'}`}
                  action={
                    <Button size="sm" variant="ghost" onClick={() => fetchSectionTimetable(selectedSectionId)}>
                      Refresh
                    </Button>
                  }
                />
                <CardBody className="p-0">
                  {sectionTimetable.length === 0 ? (
                    <div className="p-12 text-center text-xs text-[var(--color-muted-foreground)]">
                      <BookOpen className="w-8 h-8 mx-auto mb-2 text-stone-400 opacity-60" />
                      <p className="font-semibold text-[var(--color-foreground)]">No Lectures Configured</p>
                      <p className="mt-1">Click "Add Period" to configure lectures for this section.</p>
                    </div>
                  ) : (
                    <div className="divide-y divide-[var(--color-border)]">
                      {sectionTimetable.map((item) => (
                        <div key={item.id} className="p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 hover:bg-[var(--color-background)]/50">
                          <div className="flex items-start gap-3">
                            <div className="p-2.5 rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)] font-mono text-xs font-semibold shrink-0">
                              {item.startTime} - {item.endTime}
                            </div>
                            <div>
                              <div className="flex flex-wrap items-center gap-2">
                                <span className="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded bg-[var(--color-muted)] text-[var(--color-foreground)]">
                                  {item.subjectCode}
                                </span>
                                <span className="text-xs font-bold text-[var(--color-foreground)]">
                                  {item.subjectName}
                                </span>
                                <Badge variant="neutral" size="sm">
                                  {item.dayOfWeek}
                                </Badge>
                              </div>
                              <p className="text-[11px] text-[var(--color-muted-foreground)] mt-1 flex items-center gap-3">
                                <span>Room: <strong className="text-[var(--color-foreground)]">{item.classroom}</strong></span>
                                <span>•</span>
                                <span>Faculty: <strong className="text-[var(--color-primary)]">{item.facultyName}</strong></span>
                              </p>
                            </div>
                          </div>

                          <div className="flex items-center gap-2 shrink-0">
                            <Button
                              size="sm"
                              variant="ghost"
                              icon={Edit2}
                              onClick={() => handleOpenEditPeriod(item)}
                            >
                              Edit
                            </Button>
                            <Button
                              size="sm"
                              variant="ghost"
                              icon={Trash2}
                              onClick={() => handleDeleteSectionPeriod(item.id)}
                              className="text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/30"
                            />
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardBody>
              </Card>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2 space-y-6">

                {/* Section Timetable Management */}
                <Card>
                  <CardHeader
                    title={`Timetable Management: ${assignedDept || 'General'} (Section ${assignedSec || 'A'})`}
                    subtitle="Configure lecture and laboratory schedules for your assigned section"
                    action={
                      assignedDept && assignedSec && (
                        <Button
                          size="sm"
                          icon={Plus}
                          onClick={() => setShowAddTimetable(!showAddTimetable)}
                        >
                          Add Lecture
                        </Button>
                      )
                    }
                  />
                  <CardBody className="p-5">
                    {showAddTimetable && (
                      <form onSubmit={handleCreateTimetable} className="mb-6 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] space-y-3">
                        <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-primary)]">
                          New Timetable Slot for Section {assignedSec}
                        </h4>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          <Input
                            label="Subject Code"
                            placeholder="e.g. CS301"
                            value={timetableForm.subjectCode}
                            onChange={(e) => setTimetableForm({ ...timetableForm, subjectCode: e.target.value })}
                            required
                          />
                          <Input
                            label="Subject Name"
                            placeholder="e.g. Database Management Systems"
                            value={timetableForm.subjectName}
                            onChange={(e) => setTimetableForm({ ...timetableForm, subjectName: e.target.value })}
                            required
                          />
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                          <div>
                            <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                              Day of Week
                            </label>
                            <select
                              value={timetableForm.dayOfWeek}
                              onChange={(e) => setTimetableForm({ ...timetableForm, dayOfWeek: e.target.value })}
                              className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none"
                            >
                              {DAYS.map((d) => (
                                <option key={d} value={d}>{d}</option>
                              ))}
                            </select>
                          </div>
                          <Input
                            label="Start Time"
                            placeholder="e.g. 10:00 AM"
                            value={timetableForm.startTime}
                            onChange={(e) => setTimetableForm({ ...timetableForm, startTime: e.target.value })}
                            required
                          />
                          <Input
                            label="End Time"
                            placeholder="e.g. 11:30 AM"
                            value={timetableForm.endTime}
                            onChange={(e) => setTimetableForm({ ...timetableForm, endTime: e.target.value })}
                            required
                          />
                        </div>

                        <Input
                          label="Classroom / Lab"
                          placeholder="e.g. CS-204 or Systems Lab 1"
                          value={timetableForm.classroom}
                          onChange={(e) => setTimetableForm({ ...timetableForm, classroom: e.target.value })}
                          required
                        />

                        <div className="flex justify-end gap-2 pt-2">
                          <Button size="sm" variant="ghost" onClick={() => setShowAddTimetable(false)}>
                            Cancel
                          </Button>
                          <Button size="sm" type="submit" loading={submittingTimetable}>
                            Save Timetable Entry
                          </Button>
                        </div>
                      </form>
                    )}

                    {timetables.length === 0 ? (
                      <div className="p-8 text-center text-xs text-[var(--color-muted-foreground)]">
                        <BookOpen className="w-8 h-8 mx-auto mb-2 text-stone-400 opacity-60" />
                        <p className="font-semibold text-[var(--color-foreground)]">No Lectures Scheduled</p>
                        <p className="mt-1">Click "Add Lecture" to set up classes for Section {assignedSec}.</p>
                      </div>
                    ) : (
                      <div className="divide-y divide-[var(--color-border)]">
                        {timetables.map((item) => (
                          <div key={item.id} className="py-3 flex items-center justify-between">
                            <div className="flex items-start gap-3">
                              <div className="p-2 rounded bg-[var(--color-primary)]/10 text-[var(--color-primary)] font-mono text-xs font-semibold">
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
                                  {item.classroom} • <span className="font-medium text-[var(--color-primary)]">{item.dayOfWeek}</span> • Faculty: {item.facultyName}
                                </p>
                              </div>
                            </div>
                            <Button
                              size="sm"
                              variant="ghost"
                              icon={Trash2}
                              onClick={() => handleDeleteTimetable(item.id)}
                              className="text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/30"
                            />
                          </div>
                        ))}
                      </div>
                    )}
                  </CardBody>
                </Card>

                {/* Section Mentees Roster */}
                <Card>
                  <CardHeader
                    title={`Assigned Mentees Roster (${mentees.length})`}
                    subtitle={`Students enrolled in ${assignedDept || 'Department'} - Section ${assignedSec || 'A'}`}
                    action={
                      <Link to="/faculty/class">
                        <Button size="sm" variant="outline" icon={ArrowRight}>
                          Class Roster & Tasks
                        </Button>
                      </Link>
                    }
                  />
                  <CardBody className="p-0">
                    {mentees.length === 0 ? (
                      <div className="p-8 text-center text-xs text-[var(--color-muted-foreground)]">
                        <Users className="w-8 h-8 mx-auto mb-2 text-stone-400 opacity-60" />
                        <p className="font-semibold text-[var(--color-foreground)]">No Students Registered Yet</p>
                        <p className="mt-1">When students register with Department "{assignedDept}" and Section "{assignedSec}", they will automatically appear here.</p>
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-xs text-left">
                          <thead className="bg-[var(--color-background)] border-b border-[var(--color-border)] text-[var(--color-muted-foreground)] font-semibold">
                            <tr>
                              <th className="p-3">Roll No</th>
                              <th className="p-3">Student Name</th>
                              <th className="p-3">Email</th>
                              <th className="p-3">Attendance</th>
                              <th className="p-3">Status</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-[var(--color-border)]">
                            {mentees.map((m) => (
                              <tr key={m.id} className="hover:bg-[var(--color-background)]/50">
                                <td className="p-3 font-mono font-bold text-[var(--color-primary)]">{m.rollNumber}</td>
                                <td className="p-3 font-medium text-[var(--color-foreground)]">{m.name}</td>
                                <td className="p-3 text-[var(--color-muted-foreground)]">{m.email}</td>
                                <td className="p-3">
                                  <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                                    {m.attendance}%
                                  </span>
                                </td>
                                <td className="p-3">
                                  <Badge variant="success" size="sm">Active</Badge>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </CardBody>
                </Card>

              </div>

              <div>
                <AgentXAssistant onActionCompleted={() => fetchFacultyData()} />
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default FacultyPortal;
