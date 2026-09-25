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
  Workflow, AlertCircle, ShieldAlert, Bot, CheckCircle2, XCircle,
  Users, GraduationCap, BookOpen, Plus, Trash2, Calendar, Filter,
  Building, ShieldCheck, AlertTriangle, Sparkles, FileText, Bell,
  Search, Cpu, ArrowUpRight, CheckSquare, Layers, Copy, ExternalLink,
  UserCheck, Clock, Check, Wrench
} from 'lucide-react';

const DEPARTMENTS = [
  'Computer Science & Engineering',
  'Information Technology',
  'Electronics & Communication Engineering',
  'Electrical & Electronics Engineering',
  'Mechanical Engineering',
  'Civil Engineering',
  'Data Science & AI'
];

const SECTIONS = ['A', 'B', 'C', 'D'];
const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

export const AdminPortal = () => {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [activeTab, setActiveTab] = useState('overview');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState(null);

  // Sub-tabs in Admin Portal
  const [adminView, setAdminView] = useState('overview'); // 'overview' | 'faculty' | 'students' | 'notices' | 'events' | 'documents' | 'timetables' | 'resources'

  // Faculty State
  const [facultyList, setFacultyList] = useState([]);
  const [showCreateFaculty, setShowCreateFaculty] = useState(false);
  const [facultyForm, setFacultyForm] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    employeeId: '',
    department: 'Computer Science & Engineering',
    designation: 'Assistant Professor',
    assignedDepartment: 'Computer Science & Engineering',
    assignedSection: 'A',
  });
  const [creatingFaculty, setCreatingFaculty] = useState(false);

  // Mentor Assignment Modal
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedFacultyForAssign, setSelectedFacultyForAssign] = useState(null);
  const [assignDept, setAssignDept] = useState('Computer Science & Engineering');
  const [assignSec, setAssignSec] = useState('A');
  const [assignSem, setAssignSem] = useState(3);
  const [assignYear, setAssignYear] = useState('2024-2025');
  const [savingAssign, setSavingAssign] = useState(false);

  // Staff Requests State
  const [staffRequests, setStaffRequests] = useState([]);
  const [staffReqFilter, setStaffReqFilter] = useState('ALL'); // 'ALL' | 'PENDING' | 'APPROVED' | 'ACTIVATED' | 'REJECTED'
  const [processingStaffReqId, setProcessingStaffReqId] = useState(null);
  const [inviteModalData, setInviteModalData] = useState(null); // { message, inviteToken, inviteLink, expiresAt, request }
  const [copiedLink, setCopiedLink] = useState(false);

  // Students State
  const [studentList, setStudentList] = useState([]);
  const [filterDept, setFilterDept] = useState('');
  const [filterSec, setFilterSec] = useState('');
  const [studentSearch, setStudentSearch] = useState('');

  // Timetables State
  const [timetables, setTimetables] = useState([]);
  const [ttFilterDept, setTtFilterDept] = useState('Computer Science & Engineering');
  const [ttFilterSec, setTtFilterSec] = useState('A');
  const [showAddTt, setShowAddTt] = useState(false);
  const [ttForm, setTtForm] = useState({
    subjectCode: '',
    subjectName: '',
    facultyName: '',
    dayOfWeek: 'Monday',
    startTime: '09:00 AM',
    endTime: '10:30 AM',
    classroom: 'CS-101',
  });
  const [addingTt, setAddingTt] = useState(false);
  const [scanningConflicts, setScanningConflicts] = useState(false);

  // Notices State
  const [noticesList, setNoticesList] = useState([]);
  const [showAddNotice, setShowAddNotice] = useState(false);
  const [noticeForm, setNoticeForm] = useState({
    title: '',
    content: '',
    category: 'GENERAL',
    priority: 'NORMAL',
    targetAudience: 'ALL'
  });
  const [addingNotice, setAddingNotice] = useState(false);

  // Events State
  const [eventsList, setEventsList] = useState([]);
  const [showAddEvent, setShowAddEvent] = useState(false);
  const [eventForm, setEventForm] = useState({
    title: '',
    category: 'Hackathon',
    description: '',
    location: 'Vikram Sarabhai Seminar Hall',
    eventDate: '',
    eventTime: '09:00 AM',
    organizer: 'Department of CSE'
  });
  const [addingEvent, setAddingEvent] = useState(false);

  // Documents / RAG Knowledge Base State
  const [documentsList, setDocumentsList] = useState([]);
  const [showAddDoc, setShowAddDoc] = useState(false);
  const [ragStatus, setRagStatus] = useState(null);
  const [selectedDocFile, setSelectedDocFile] = useState(null);
  const [ragTestQuery, setRagTestQuery] = useState('');
  const [ragTestResults, setRagTestResults] = useState([]);
  const [testingRag, setTestingRag] = useState(false);
  const [docForm, setDocForm] = useState({
    title: '',
    category: 'REGULATION',
    description: '',
    content: '',
    department: 'All Departments',
    documentType: 'PDF',
    version: '1.0'
  });
  const [addingDoc, setAddingDoc] = useState(false);

  // Complaints & Grievance Routing State
  const [complaintsList, setComplaintsList] = useState([]);
  const [complaintsFilerFilter, setComplaintsFilerFilter] = useState('ALL'); // 'ALL' | 'FACULTY' | 'STUDENT'
  const [complaintsStatusFilter, setComplaintsStatusFilter] = useState('ALL'); // 'ALL' | 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
  const [complaintsLoading, setComplaintsLoading] = useState(false);
  const [resolvingComplaintId, setResolvingComplaintId] = useState(null);
  const [adminResolutionNotes, setAdminResolutionNotes] = useState('');
  const [updatingComplaint, setUpdatingComplaint] = useState(false);

  const fetchAdminData = async () => {
    try {
      const res = await api.get('/admin/dashboard');
      setData(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchFaculty = async () => {
    try {
      const res = await api.get('/admin/faculty');
      setFacultyList(res.data);
    } catch (err) {}
  };

  const fetchStudents = async () => {
    try {
      const params = {};
      if (filterDept) params.department = filterDept;
      if (filterSec) params.section = filterSec;
      const res = await api.get('/admin/students', { params });
      setStudentList(res.data);
    } catch (err) {}
  };

  const fetchTimetables = async () => {
    try {
      const params = {};
      if (ttFilterDept) params.department = ttFilterDept;
      if (ttFilterSec) params.section = ttFilterSec;
      const res = await api.get('/admin/timetables', { params });
      setTimetables(res.data);
    } catch (err) {}
  };

  const fetchNotices = async () => {
    try {
      const res = await api.get('/admin/notices');
      setNoticesList(res.data);
    } catch (err) {}
  };

  const fetchEvents = async () => {
    try {
      const res = await api.get('/admin/events');
      setEventsList(res.data);
    } catch (err) {}
  };

  const fetchDocuments = async () => {
    try {
      const res = await api.get('/admin/documents');
      setDocumentsList(res.data);
    } catch (err) {}
    try {
      const statusRes = await api.get('/admin/rag/status');
      setRagStatus(statusRes.data);
    } catch (err) {}
  };

  const fetchStaffRequests = async () => {
    try {
      const res = await api.get('/admin/staff-requests');
      setStaffRequests(res.data);
    } catch (err) {
      console.error('Failed to load staff requests', err);
    }
  };

  const fetchComplaints = async (filerRole = complaintsFilerFilter, status = complaintsStatusFilter) => {
    setComplaintsLoading(true);
    try {
      const params = {};
      if (filerRole && filerRole !== 'ALL') params.filerRole = filerRole;
      if (status && status !== 'ALL') params.status = status;
      const res = await api.get('/admin/complaints', { params });
      setComplaintsList(res.data || []);
    } catch (err) {
      console.error('Failed to load complaints', err);
    } finally {
      setComplaintsLoading(false);
    }
  };

  const handleUpdateComplaintStatus = async (id, newStatus, notes) => {
    setUpdatingComplaint(true);
    try {
      await api.patch(`/admin/complaints/${id}/status`, {
        status: newStatus,
        resolutionNotes: notes || undefined,
      });
      addToast(`Ticket status updated to ${newStatus}.`, 'success');
      setResolvingComplaintId(null);
      setAdminResolutionNotes('');
      fetchComplaints(complaintsFilerFilter, complaintsStatusFilter);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update complaint status.';
      addToast(msg, 'error');
    } finally {
      setUpdatingComplaint(false);
    }
  };

  useEffect(() => {
    fetchAdminData();
    fetchFaculty();
    fetchStudents();
    fetchTimetables();
    fetchNotices();
    fetchEvents();
    fetchDocuments();
    fetchStaffRequests();
    fetchComplaints();
  }, []);

  const handleDecision = async (id, decision) => {
    setProcessingId(id);
    try {
      await api.post('/admin/approvals/' + id + '/decision', { decision });
      addToast(
        decision === 'APPROVED' ? 'Approved & executed: timetable updated, students notified!' : 'Recommendation rejected.',
        decision === 'APPROVED' ? 'success' : 'info'
      );
      fetchAdminData();
      fetchTimetables();
    } catch (err) {
      addToast('Unable to record administrative decision.', 'error');
    } finally {
      setProcessingId(null);
    }
  };

  const handleScanConflicts = async () => {
    setScanningConflicts(true);
    try {
      const res = await api.post('/admin/conflicts/scan');
      addToast(`Conflict scan complete: ${res.data.length} approval recommendations queued.`, 'info');
      fetchAdminData();
    } catch (err) {
      addToast('Error scanning timetable conflicts.', 'error');
    } finally {
      setScanningConflicts(false);
    }
  };

  const handleCreateFaculty = async (e) => {
    e.preventDefault();
    setCreatingFaculty(true);
    try {
      await api.post('/admin/faculty', facultyForm);
      addToast('Faculty member created successfully!', 'success');
      setShowCreateFaculty(false);
      setFacultyForm({
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        password: '',
        employeeId: '',
        department: 'Computer Science & Engineering',
        designation: 'Assistant Professor',
        assignedDepartment: 'Computer Science & Engineering',
        assignedSection: 'A',
      });
      fetchFaculty();
      fetchAdminData();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create faculty member.';
      addToast(msg, 'error');
    } finally {
      setCreatingFaculty(false);
    }
  };

  const handleApproveStaff = async (id) => {
    setProcessingStaffReqId(id);
    try {
      const res = await api.post(`/admin/staff-requests/${id}/approve`);
      addToast('Staff request approved! Invitation generated.', 'success');
      setInviteModalData(res.data);
      setCopiedLink(false);
      fetchStaffRequests();
      fetchFaculty();
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to approve staff request.', 'error');
    } finally {
      setProcessingStaffReqId(null);
    }
  };

  const handleRejectStaff = async (id) => {
    const reason = window.prompt('Enter rejection reason (optional):', 'Administrative review declined');
    if (reason === null) return;

    setProcessingStaffReqId(id);
    try {
      await api.post(`/admin/staff-requests/${id}/reject`, { reason });
      addToast('Staff request rejected.', 'info');
      fetchStaffRequests();
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to reject staff request.', 'error');
    } finally {
      setProcessingStaffReqId(null);
    }
  };

  const handleAssignMentor = async (e) => {
    e.preventDefault();
    if (!selectedFacultyForAssign) return;
    setSavingAssign(true);
    try {
      await api.post(`/admin/faculty/${selectedFacultyForAssign.id}/mentor-sections`, {
        department: assignDept,
        section: assignSec,
        semester: Number(assignSem) || 1,
        academicYear: assignYear || '2024-2025',
      });
      addToast(`Assigned ${selectedFacultyForAssign.name} as mentor for ${assignDept} Sec ${assignSec} (Sem ${assignSem})`, 'success');
      setShowAssignModal(false);
      setSelectedFacultyForAssign(null);
      fetchFaculty();
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to assign mentor section.', 'error');
    } finally {
      setSavingAssign(false);
    }
  };

  const handleRemoveMentorSection = async (facultyId, mappingId) => {
    if (!window.confirm('Remove this mentor section assignment?')) return;
    try {
      await api.delete(`/admin/faculty/${facultyId}/mentor-sections/${mappingId}`);
      addToast('Mentor section assignment removed.', 'info');
      fetchFaculty();
    } catch (err) {
      addToast('Failed to remove mentor section.', 'error');
    }
  };

  const handleCreateNotice = async (e) => {
    e.preventDefault();
    setAddingNotice(true);
    try {
      await api.post('/admin/notices', noticeForm);
      addToast('Notice published successfully!', 'success');
      setShowAddNotice(false);
      setNoticeForm({ title: '', content: '', category: 'GENERAL', priority: 'NORMAL', targetAudience: 'ALL' });
      fetchNotices();
      fetchAdminData();
    } catch (err) {
      addToast('Failed to create notice.', 'error');
    } finally {
      setAddingNotice(false);
    }
  };

  const handleDeleteNotice = async (id) => {
    if (!window.confirm('Delete this notice?')) return;
    try {
      await api.delete(`/admin/notices/${id}`);
      addToast('Notice deleted.', 'info');
      fetchNotices();
      fetchAdminData();
    } catch (err) {
      addToast('Failed to delete notice.', 'error');
    }
  };

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    setAddingEvent(true);
    try {
      await api.post('/admin/events', eventForm);
      addToast('Event created successfully!', 'success');
      setShowAddEvent(false);
      setEventForm({
        title: '',
        category: 'Hackathon',
        description: '',
        location: 'Vikram Sarabhai Seminar Hall',
        eventDate: '',
        eventTime: '09:00 AM',
        organizer: 'Department of CSE'
      });
      fetchEvents();
      fetchAdminData();
    } catch (err) {
      addToast('Failed to create event.', 'error');
    } finally {
      setAddingEvent(false);
    }
  };

  const handleDeleteEvent = async (id) => {
    if (!window.confirm('Delete this event?')) return;
    try {
      await api.delete(`/admin/events/${id}`);
      addToast('Event deleted.', 'info');
      fetchEvents();
      fetchAdminData();
    } catch (err) {
      addToast('Failed to delete event.', 'error');
    }
  };

  const handleCreateDocument = async (e) => {
    e.preventDefault();
    setAddingDoc(true);
    try {
      if (selectedDocFile) {
        const formData = new FormData();
        formData.append('file', selectedDocFile);
        formData.append('title', docForm.title || selectedDocFile.name);
        formData.append('category', docForm.category);
        formData.append('department', docForm.department);
        formData.append('version', docForm.version);
        await api.post('/admin/rag/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      } else {
        await api.post('/admin/documents', docForm);
      }
      addToast('Document successfully uploaded & indexed in RAG knowledge base!', 'success');
      setShowAddDoc(false);
      setSelectedDocFile(null);
      setDocForm({
        title: '',
        category: 'REGULATION',
        description: '',
        content: '',
        department: 'All Departments',
        documentType: 'PDF',
        version: '1.0'
      });
      fetchDocuments();
      fetchAdminData();
    } catch (err) {
      addToast('Failed to upload document: ' + (err.response?.data?.error || err.message), 'error');
    } finally {
      setAddingDoc(false);
    }
  };

  const handleTestRagQuery = async (e) => {
    e.preventDefault();
    if (!ragTestQuery.trim()) return;
    setTestingRag(true);
    try {
      const res = await api.post('/admin/rag/query', { query: ragTestQuery, topK: 3 });
      setRagTestResults(res.data || []);
      addToast(`RAG Retrieval returned ${res.data?.length || 0} chunk(s)`, 'info');
    } catch (err) {
      addToast('RAG query test failed', 'error');
    } finally {
      setTestingRag(false);
    }
  };

  const handleDeleteDocument = async (id) => {
    if (!window.confirm('Delete this document from RAG knowledge base?')) return;
    try {
      await api.delete(`/admin/documents/${id}`);
      addToast('Document removed from knowledge base.', 'info');
      fetchDocuments();
      fetchAdminData();
    } catch (err) {
      addToast('Failed to delete document.', 'error');
    }
  };

  const handleCreateTimetable = async (e) => {
    e.preventDefault();
    setAddingTt(true);
    try {
      await api.post('/admin/timetables', {
        department: ttFilterDept,
        section: ttFilterSec,
        ...ttForm,
      });
      addToast('Timetable entry created!', 'success');
      setShowAddTt(false);
      setTtForm({
        subjectCode: '',
        subjectName: '',
        facultyName: '',
        dayOfWeek: 'Monday',
        startTime: '09:00 AM',
        endTime: '10:30 AM',
        classroom: 'CS-101',
      });
      fetchTimetables();
      fetchAdminData();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create timetable.';
      addToast(msg, 'error');
    } finally {
      setAddingTt(false);
    }
  };

  const handleDeleteTimetable = async (id) => {
    if (!window.confirm('Delete this timetable entry?')) return;
    try {
      await api.delete(`/admin/timetables/${id}`);
      addToast('Timetable entry deleted.', 'info');
      fetchTimetables();
      fetchAdminData();
    } catch (err) {
      addToast('Failed to delete timetable.', 'error');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[var(--color-background)]">
        <Navbar />
        <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
          <LoadingSpinner size="lg" message="Loading campus administration cockpit..." />
        </div>
      </div>
    );
  }

  const pendingApprovalsCount = data?.approvals ? data.approvals.filter(a => a.status === 'PENDING').length : 0;
  const conflicts = data?.conflicts || [];
  const agentLogs = data?.agentLogs || [];

  const filteredStudents = studentList.filter(s => {
    if (!studentSearch.trim()) return true;
    const q = studentSearch.toLowerCase();
    return (s.name && s.name.toLowerCase().includes(q)) ||
           (s.rollNumber && s.rollNumber.toLowerCase().includes(q)) ||
           (s.email && s.email.toLowerCase().includes(q));
  });

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
                  Campus Operations Cockpit
                </h1>
                <Badge variant="danger" size="sm">System Admin</Badge>
              </div>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-1">
                Context Graph • Proactive Conflict Engine • Multi-Agent Governance
              </p>
            </div>

            {/* Admin View Switcher */}
            <div className="flex items-center gap-1.5 p-1 rounded-xl bg-[var(--color-muted)]/40 border border-[var(--color-border)] overflow-x-auto no-scrollbar">
              <button
                onClick={() => setAdminView('overview')}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'overview'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Overview
              </button>
              <button
                onClick={() => { setAdminView('students'); fetchStudents(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'students'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Students ({studentList.length})
              </button>
              <button
                onClick={() => { setAdminView('faculty'); fetchFaculty(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'faculty'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Faculty ({facultyList.length})
              </button>
              <button
                onClick={() => { setAdminView('staffRequests'); fetchStaffRequests(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1.5 ${
                  adminView === 'staffRequests'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                <span>Staff Requests</span>
                {staffRequests.filter(r => r.status === 'PENDING').length > 0 && (
                  <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-amber-500 text-white font-bold">
                    {staffRequests.filter(r => r.status === 'PENDING').length}
                  </span>
                )}
              </button>
              <button
                onClick={() => { setAdminView('notices'); fetchNotices(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'notices'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Notices ({noticesList.length})
              </button>
              <button
                onClick={() => { setAdminView('events'); fetchEvents(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'events'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Events ({eventsList.length})
              </button>
              <button
                onClick={() => { setAdminView('documents'); fetchDocuments(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'documents'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                RAG Knowledge Base ({documentsList.length})
              </button>
              <button
                onClick={() => { setAdminView('timetables'); fetchTimetables(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'timetables'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Timetables
              </button>
              <button
                onClick={() => setAdminView('resources')}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  adminView === 'resources'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Digital Campus
              </button>
              <button
                onClick={() => { setAdminView('complaints'); setActiveTab('overview'); fetchComplaints(); }}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1.5 ${
                  adminView === 'complaints' || activeTab === 'complaints'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                <span>Complaints Hub</span>
                {complaintsList.filter(c => c.status === 'OPEN').length > 0 && (
                  <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-red-500 text-white font-bold">
                    {complaintsList.filter(c => c.status === 'OPEN').length}
                  </span>
                )}
              </button>
            </div>
          </div>

          {/* Real Database KPI Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              title="Registered Students"
              value={'' + (data?.studentCount || studentList.length)}
              subtitle="Enrolled with unique Roll No"
              icon={Users}
              badge={<Badge variant="primary">Database</Badge>}
            />
            <StatCard
              title="Active Faculty"
              value={'' + (data?.facultyCount || facultyList.length)}
              subtitle="Mentors & Course Professors"
              icon={GraduationCap}
              badge={<Badge variant="info">Faculty Roster</Badge>}
            />
            <StatCard
              title="Knowledge Base (RAG)"
              value={'' + (data?.documentsCount || documentsList.length)}
              subtitle="Indexed Institutional Policies"
              icon={FileText}
              badge={<Badge variant="success">Vector Chunks</Badge>}
            />
            <StatCard
              title="AI Requests Handled"
              value={'' + (data?.agentTasksToday || 0)}
              subtitle="Multi-agent orchestrations"
              icon={Cpu}
              badge={<Badge variant="warning">Live Engine</Badge>}
            />
          </div>

          {/* Sub-View Routing */}
          {activeTab === 'agent' ? (
            <div className="max-w-3xl">
              <AgentXAssistant onActionCompleted={() => { fetchAdminData(); fetchFaculty(); fetchStudents(); }} />
            </div>
          ) : adminView === 'resources' ? (
            <DigitalCampusView />
          ) : adminView === 'students' ? (
            /* ================= STUDENTS MANAGEMENT VIEW ================= */
            <Card>
              <CardHeader
                title="Registered Students Roster"
                subtitle="Filter, search, and inspect real student records by academic Department and Section"
                action={
                  <div className="flex flex-wrap items-center gap-2">
                    <div className="relative">
                      <Search className="w-3.5 h-3.5 absolute left-2.5 top-2.5 text-[var(--color-muted-foreground)]" />
                      <input
                        type="text"
                        placeholder="Search student or roll no..."
                        value={studentSearch}
                        onChange={(e) => setStudentSearch(e.target.value)}
                        className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs pl-8 pr-3 py-1.5 text-[var(--color-foreground)] focus:outline-none"
                      />
                    </div>
                    <select
                      value={filterDept}
                      onChange={(e) => setFilterDept(e.target.value)}
                      className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs py-1.5 px-2.5 text-[var(--color-foreground)]"
                    >
                      <option value="">All Departments</option>
                      {DEPARTMENTS.map((d) => (
                        <option key={d} value={d}>{d}</option>
                      ))}
                    </select>
                    <select
                      value={filterSec}
                      onChange={(e) => setFilterSec(e.target.value)}
                      className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs py-1.5 px-2.5 text-[var(--color-foreground)]"
                    >
                      <option value="">All Sections</option>
                      {SECTIONS.map((s) => (
                        <option key={s} value={s}>Section {s}</option>
                      ))}
                    </select>
                    <Button size="sm" variant="outline" icon={Filter} onClick={fetchStudents}>
                      Apply
                    </Button>
                  </div>
                }
              />
              <CardBody className="p-0">
                {filteredStudents.length === 0 ? (
                  <div className="p-8 text-center text-xs text-[var(--color-muted-foreground)]">
                    <Users className="w-8 h-8 mx-auto mb-2 text-[var(--color-muted-foreground)] opacity-60" />
                    <p className="font-semibold text-[var(--color-foreground)]">No Students Found</p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-xs text-left">
                      <thead className="bg-[var(--color-background)] border-b border-[var(--color-border)] text-[var(--color-muted-foreground)] font-semibold">
                        <tr>
                          <th className="p-3">Roll Number</th>
                          <th className="p-3">Student Name</th>
                          <th className="p-3">Email</th>
                          <th className="p-3">Department</th>
                          <th className="p-3">Section</th>
                          <th className="p-3">Attendance</th>
                          <th className="p-3">CGPA</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--color-border)]">
                        {filteredStudents.map((s) => (
                          <tr key={s.id} className="hover:bg-[var(--color-background)]/50">
                            <td className="p-3 font-mono font-bold text-[var(--color-primary)]">{s.rollNumber}</td>
                            <td className="p-3 font-medium text-[var(--color-foreground)]">{s.name}</td>
                            <td className="p-3 text-[var(--color-muted-foreground)]">{s.email}</td>
                            <td className="p-3">{s.department}</td>
                            <td className="p-3">
                              <Badge variant="neutral" size="sm">Sec {s.section}</Badge>
                            </td>
                            <td className="p-3 font-semibold text-emerald-600 dark:text-emerald-400">
                              {s.attendance}%
                            </td>
                            <td className="p-3 font-mono">{s.cgpa || 0.0}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </CardBody>
            </Card>
          ) : adminView === 'faculty' ? (
            /* ================= FACULTY MANAGEMENT VIEW ================= */
            <div className="space-y-6">
              <Card>
                <CardHeader
                  title="Faculty & Mentor Roster"
                  subtitle="Create faculty accounts and assign them to specific Department Sections as Mentors"
                  action={
                    <Button size="sm" icon={Plus} onClick={() => setShowCreateFaculty(!showCreateFaculty)}>
                      Create Faculty
                    </Button>
                  }
                />
                <CardBody className="p-5">
                  {showCreateFaculty && (
                    <form onSubmit={handleCreateFaculty} className="mb-6 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] space-y-4">
                      <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-primary)]">
                        Add New Faculty Member
                      </h4>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <Input
                          label="First Name"
                          placeholder="Arun"
                          value={facultyForm.firstName}
                          onChange={(e) => setFacultyForm({ ...facultyForm, firstName: e.target.value })}
                          required
                        />
                        <Input
                          label="Last Name"
                          placeholder="Kumar"
                          value={facultyForm.lastName}
                          onChange={(e) => setFacultyForm({ ...facultyForm, lastName: e.target.value })}
                          required
                        />
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <Input
                          label="Username"
                          placeholder="arun.kumar"
                          value={facultyForm.username}
                          onChange={(e) => setFacultyForm({ ...facultyForm, username: e.target.value })}
                          required
                        />
                        <Input
                          label="Email"
                          type="email"
                          placeholder="arun.kumar@campus.edu"
                          value={facultyForm.email}
                          onChange={(e) => setFacultyForm({ ...facultyForm, email: e.target.value })}
                          required
                        />
                        <Input
                          label="Employee ID"
                          placeholder="EMP-CSE-105"
                          value={facultyForm.employeeId}
                          onChange={(e) => setFacultyForm({ ...facultyForm, employeeId: e.target.value })}
                        />
                      </div>
                      <div className="flex justify-end gap-2 pt-2">
                        <Button size="sm" variant="ghost" onClick={() => setShowCreateFaculty(false)}>Cancel</Button>
                        <Button size="sm" type="submit" loading={creatingFaculty}>Create</Button>
                      </div>
                    </form>
                  )}

                  <div className="overflow-x-auto">
                    <table className="w-full text-xs text-left">
                      <thead className="bg-[var(--color-background)] border-b border-[var(--color-border)] text-[var(--color-muted-foreground)] font-semibold">
                        <tr>
                          <th className="p-3">Employee ID</th>
                          <th className="p-3">Name</th>
                          <th className="p-3">Designation</th>
                          <th className="p-3">Department</th>
                          <th className="p-3">Assigned Mentorship</th>
                          <th className="p-3 text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--color-border)]">
                        {facultyList.map((f) => (
                          <tr key={f.id} className="hover:bg-[var(--color-background)]/50">
                            <td className="p-3 font-mono font-bold text-[var(--color-primary)]">{f.employeeId}</td>
                            <td className="p-3 font-medium text-[var(--color-foreground)]">{f.name}</td>
                            <td className="p-3">{f.designation}</td>
                            <td className="p-3">{f.department}</td>
                            <td className="p-3">
                              <div className="flex flex-wrap items-center gap-1.5">
                                {f.mentorSections && f.mentorSections.length > 0 ? (
                                  f.mentorSections.map((ms) => (
                                    <span
                                      key={ms.id}
                                      className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-[var(--color-primary)]/10 text-[var(--color-primary)] border border-[var(--color-primary)]/30 text-[11px] font-medium"
                                    >
                                      <span>{ms.department ? ms.department.split(' ')[0] : ''} Sec {ms.section} (Sem {ms.semester})</span>
                                      <button
                                        type="button"
                                        onClick={() => handleRemoveMentorSection(f.id, ms.id)}
                                        className="hover:text-red-500 font-bold ml-0.5 text-xs text-[var(--color-muted-foreground)] hover:scale-110 transition-all"
                                        title="Remove mentor section mapping"
                                      >
                                        ×
                                      </button>
                                    </span>
                                  ))
                                ) : f.assignedSection ? (
                                  <Badge variant="primary" size="sm">
                                    Section {f.assignedSection}
                                  </Badge>
                                ) : (
                                  <span className="text-[var(--color-muted-foreground)]">Not assigned</span>
                                )}
                              </div>
                            </td>
                            <td className="p-3 text-right">
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => {
                                  setSelectedFacultyForAssign(f);
                                  setShowAssignModal(true);
                                }}
                              >
                                + Add Section
                              </Button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </CardBody>
              </Card>

              {showAssignModal && selectedFacultyForAssign && (
                <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
                  <div className="bg-[var(--color-card)] rounded-2xl border border-[var(--color-border)] max-w-md w-full p-6 shadow-xl space-y-4">
                    <h3 className="text-sm font-bold text-[var(--color-foreground)]">
                      Assign Mentor Section Mapping
                    </h3>
                    <p className="text-xs text-[var(--color-muted-foreground)]">
                      Assign {selectedFacultyForAssign.name} as Class Advisor/Mentor to an academic section cohort.
                    </p>
                    <form onSubmit={handleAssignMentor} className="space-y-3">
                      <div>
                        <label className="block text-xs font-medium mb-1">Department</label>
                        <select
                          value={assignDept}
                          onChange={(e) => setAssignDept(e.target.value)}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] text-xs p-2.5"
                        >
                          {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
                        </select>
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <label className="block text-xs font-medium mb-1">Section</label>
                          <select
                            value={assignSec}
                            onChange={(e) => setAssignSec(e.target.value)}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] text-xs p-2.5"
                          >
                            {SECTIONS.map(s => <option key={s} value={s}>Section {s}</option>)}
                          </select>
                        </div>
                        <div>
                          <label className="block text-xs font-medium mb-1">Semester</label>
                          <select
                            value={assignSem}
                            onChange={(e) => setAssignSem(e.target.value)}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] text-xs p-2.5"
                          >
                            {[1, 2, 3, 4, 5, 6, 7, 8].map(sem => <option key={sem} value={sem}>Semester {sem}</option>)}
                          </select>
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs font-medium mb-1">Academic Year</label>
                        <input
                          type="text"
                          value={assignYear}
                          onChange={(e) => setAssignYear(e.target.value)}
                          placeholder="2024-2025"
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] text-xs p-2.5"
                        />
                      </div>
                      <div className="flex justify-end gap-2 pt-3">
                        <Button size="sm" variant="ghost" onClick={() => setShowAssignModal(false)}>Cancel</Button>
                        <Button size="sm" type="submit" loading={savingAssign}>Confirm Assignment</Button>
                      </div>
                    </form>
                  </div>
                </div>
              )}
            </div>
          ) : adminView === 'staffRequests' ? (
            /* ================= STAFF LOGIN REQUESTS / APPROVAL CENTER ================= */
            <div className="space-y-6">
              <Card>
                <CardHeader
                  title="Staff & Faculty Access Approval Center"
                  subtitle="Review prospective staff registration requests, approve accounts, and generate secure activation invitation links"
                  action={
                    <div className="flex items-center gap-1.5 p-1 rounded-lg bg-[var(--color-muted)]/50 border border-[var(--color-border)]">
                      {['ALL', 'PENDING', 'APPROVED', 'ACTIVATED', 'REJECTED'].map((st) => (
                        <button
                          key={st}
                          type="button"
                          onClick={() => setStaffReqFilter(st)}
                          className={`px-2.5 py-1 rounded-md text-[11px] font-semibold transition-all ${
                            staffReqFilter === st
                              ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                              : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                          }`}
                        >
                          {st}
                        </button>
                      ))}
                    </div>
                  }
                />
                <CardBody className="p-5">
                  {staffRequests.filter(r => staffReqFilter === 'ALL' || r.status === staffReqFilter).length === 0 ? (
                    <div className="py-12 text-center text-xs text-[var(--color-muted-foreground)]">
                      No staff requests found for status "{staffReqFilter}".
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-xs text-left">
                        <thead className="bg-[var(--color-background)] border-b border-[var(--color-border)] text-[var(--color-muted-foreground)] font-semibold">
                          <tr>
                            <th className="p-3">Applicant Name</th>
                            <th className="p-3">Email Address</th>
                            <th className="p-3">Department & Designation</th>
                            <th className="p-3">Status</th>
                            <th className="p-3">Requested At</th>
                            <th className="p-3 text-right">Actions</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-[var(--color-border)]">
                          {staffRequests
                            .filter(r => staffReqFilter === 'ALL' || r.status === staffReqFilter)
                            .map((r) => (
                              <tr key={r.id} className="hover:bg-[var(--color-background)]/50">
                                <td className="p-3 font-medium text-[var(--color-foreground)]">
                                  {r.firstName} {r.lastName}
                                  {r.notes && (
                                    <p className="text-[10px] text-[var(--color-muted-foreground)] font-normal italic mt-0.5">
                                      "{r.notes}"
                                    </p>
                                  )}
                                </td>
                                <td className="p-3 font-mono text-[var(--color-primary)]">{r.email}</td>
                                <td className="p-3">
                                  <p className="font-medium text-[var(--color-foreground)]">{r.designation}</p>
                                  <p className="text-[10px] text-[var(--color-muted-foreground)]">{r.department}</p>
                                </td>
                                <td className="p-3">
                                  <Badge
                                    variant={
                                      r.status === 'PENDING' ? 'warning' :
                                      r.status === 'APPROVED' ? 'info' :
                                      r.status === 'ACTIVATED' ? 'success' : 'danger'
                                    }
                                    size="sm"
                                  >
                                    {r.status}
                                  </Badge>
                                </td>
                                <td className="p-3 text-[11px] text-[var(--color-muted-foreground)]">
                                  {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : 'Recent'}
                                </td>
                                <td className="p-3 text-right">
                                  {r.status === 'PENDING' ? (
                                    <div className="flex items-center justify-end gap-2">
                                      <Button
                                        size="sm"
                                        variant="outline"
                                        className="text-red-600 hover:text-red-700"
                                        disabled={processingStaffReqId === r.id}
                                        onClick={() => handleRejectStaff(r.id)}
                                      >
                                        Reject
                                      </Button>
                                      <Button
                                        size="sm"
                                        icon={UserCheck}
                                        loading={processingStaffReqId === r.id}
                                        onClick={() => handleApproveStaff(r.id)}
                                      >
                                        Approve & Invite
                                      </Button>
                                    </div>
                                  ) : r.status === 'APPROVED' ? (
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      icon={ExternalLink}
                                      onClick={() => {
                                        setInviteModalData({
                                          request: r,
                                          inviteToken: r.inviteToken,
                                          inviteLink: `${window.location.origin}/activate-staff?token=${r.inviteToken}`,
                                          expiresAt: r.inviteExpiresAt,
                                        });
                                        setCopiedLink(false);
                                      }}
                                    >
                                      View Invite Link
                                    </Button>
                                  ) : (
                                    <span className="text-[11px] text-[var(--color-muted-foreground)] font-mono">
                                      {r.status === 'ACTIVATED' ? 'Account Active' : 'Declined'}
                                    </span>
                                  )}
                                </td>
                              </tr>
                            ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </CardBody>
              </Card>

              {/* Invitation Link Modal */}
              {inviteModalData && (
                <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
                  <div className="bg-[var(--color-card)] rounded-2xl border border-[var(--color-border)] max-w-lg w-full p-6 shadow-2xl space-y-4">
                    <div className="flex items-center gap-2 text-emerald-600 dark:text-emerald-400 font-bold text-sm">
                      <CheckCircle2 className="w-5 h-5" />
                      <span>Faculty Account Approved & Invitation Issued</span>
                    </div>

                    <p className="text-xs text-[var(--color-muted-foreground)] leading-relaxed">
                      An invitation has been generated for{' '}
                      <strong className="text-[var(--color-foreground)]">
                        {inviteModalData.request?.firstName} {inviteModalData.request?.lastName}
                      </strong>{' '}
                      ({inviteModalData.request?.email || inviteModalData.email}). The staff member can use this secure link to set their password and activate their account.
                    </p>

                    <div className="space-y-1.5">
                      <label className="block text-[11px] font-semibold text-[var(--color-foreground)]">
                        Activation Link:
                      </label>
                      <div className="flex items-center gap-2">
                        <input
                          type="text"
                          readOnly
                          value={inviteModalData.inviteLink}
                          className="flex-1 rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] text-xs font-mono p-2.5 text-[var(--color-foreground)] selection:bg-[var(--color-primary)] selection:text-white"
                        />
                        <Button
                          size="sm"
                          icon={copiedLink ? Check : Copy}
                          variant={copiedLink ? 'success' : 'outline'}
                          onClick={() => {
                            navigator.clipboard.writeText(inviteModalData.inviteLink);
                            setCopiedLink(true);
                            addToast('Invitation link copied to clipboard!', 'success');
                          }}
                        >
                          {copiedLink ? 'Copied' : 'Copy'}
                        </Button>
                      </div>
                    </div>

                    <div className="p-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-muted)]/20 text-[11px] text-[var(--color-muted-foreground)] space-y-1 font-mono">
                      <div>Token: <span className="text-[var(--color-primary)]">{inviteModalData.inviteToken}</span></div>
                      <div>Expires: {inviteModalData.expiresAt ? new Date(inviteModalData.expiresAt).toLocaleString() : 'In 7 days'}</div>
                    </div>

                    <div className="flex items-center justify-between pt-2">
                      <a
                        href={inviteModalData.inviteLink}
                        target="_blank"
                        rel="noreferrer"
                        className="text-xs text-[var(--color-primary)] hover:underline inline-flex items-center gap-1 font-medium"
                      >
                        <span>Open Activation Page</span>
                        <ExternalLink className="w-3.5 h-3.5" />
                      </a>

                      <Button
                        size="sm"
                        onClick={() => setInviteModalData(null)}
                      >
                        Done
                      </Button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ) : adminView === 'notices' ? (
            /* ================= NOTICES MANAGEMENT VIEW ================= */
            <Card>
              <CardHeader
                title="Institutional Notices & Circulars"
                subtitle="Create, publish, and manage verified college announcements"
                action={
                  <Button size="sm" icon={Plus} onClick={() => setShowAddNotice(!showAddNotice)}>
                    New Notice
                  </Button>
                }
              />
              <CardBody className="p-5">
                {showAddNotice && (
                  <form onSubmit={handleCreateNotice} className="mb-6 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] space-y-3">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-primary)]">Publish Notice</h4>
                    <Input
                      label="Notice Title"
                      placeholder="e.g. Schedule for Continuous Internal Assessment..."
                      value={noticeForm.title}
                      onChange={(e) => setNoticeForm({ ...noticeForm, title: e.target.value })}
                      required
                    />
                    <div>
                      <label className="block text-xs font-medium mb-1">Notice Content</label>
                      <textarea
                        rows={3}
                        placeholder="Detailed notice body..."
                        value={noticeForm.content}
                        onChange={(e) => setNoticeForm({ ...noticeForm, content: e.target.value })}
                        className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-3 text-[var(--color-foreground)]"
                        required
                      />
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-medium mb-1">Priority</label>
                        <select
                          value={noticeForm.priority}
                          onChange={(e) => setNoticeForm({ ...noticeForm, priority: e.target.value })}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2"
                        >
                          <option value="NORMAL">Normal</option>
                          <option value="HIGH">High</option>
                          <option value="URGENT">Urgent</option>
                        </select>
                      </div>
                      <div>
                        <label className="block text-xs font-medium mb-1">Target Audience</label>
                        <select
                          value={noticeForm.targetAudience}
                          onChange={(e) => setNoticeForm({ ...noticeForm, targetAudience: e.target.value })}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2"
                        >
                          <option value="ALL">All Campus</option>
                          <option value="STUDENTS">Students Only</option>
                          <option value="FACULTY">Faculty Only</option>
                        </select>
                      </div>
                    </div>
                    <div className="flex justify-end gap-2 pt-2">
                      <Button size="sm" variant="ghost" onClick={() => setShowAddNotice(false)}>Cancel</Button>
                      <Button size="sm" type="submit" loading={addingNotice}>Publish</Button>
                    </div>
                  </form>
                )}

                <div className="space-y-3">
                  {noticesList.map((n) => (
                    <div key={n.id} className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                      <div>
                        <div className="flex items-center gap-2">
                          <Badge variant={n.priority === 'URGENT' ? 'danger' : 'neutral'} size="sm">{n.priority}</Badge>
                          <span className="text-[10px] uppercase font-bold text-[var(--color-muted-foreground)]">Audience: {n.targetAudience}</span>
                        </div>
                        <h4 className="text-xs font-bold text-[var(--color-foreground)] mt-1.5">{n.title}</h4>
                        <p className="text-[11px] text-[var(--color-muted-foreground)] mt-0.5">{n.content}</p>
                      </div>
                      <Button size="sm" variant="ghost" onClick={() => handleDeleteNotice(n.id)}>
                        <Trash2 className="w-3.5 h-3.5 text-red-500" />
                      </Button>
                    </div>
                  ))}
                </div>
              </CardBody>
            </Card>
          ) : adminView === 'events' ? (
            /* ================= EVENTS MANAGEMENT VIEW ================= */
            <Card>
              <CardHeader
                title="Campus Events & Symposia"
                subtitle="Configure hackathons, guest lectures, and institutional conferences"
                action={
                  <Button size="sm" icon={Plus} onClick={() => setShowAddEvent(!showAddEvent)}>
                    New Event
                  </Button>
                }
              />
              <CardBody className="p-5">
                {showAddEvent && (
                  <form onSubmit={handleCreateEvent} className="mb-6 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] space-y-3">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-primary)]">Add Campus Event</h4>
                    <Input
                      label="Event Name"
                      placeholder="e.g. InnovateX Hackathon..."
                      value={eventForm.title}
                      onChange={(e) => setEventForm({ ...eventForm, title: e.target.value })}
                      required
                    />
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <Input
                        label="Category"
                        placeholder="Hackathon, Workshop, Symposium..."
                        value={eventForm.category}
                        onChange={(e) => setEventForm({ ...eventForm, category: e.target.value })}
                        required
                      />
                      <Input
                        label="Location / Venue"
                        placeholder="Vikram Sarabhai Seminar Hall..."
                        value={eventForm.location}
                        onChange={(e) => setEventForm({ ...eventForm, location: e.target.value })}
                        required
                      />
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <Input
                        label="Event Date"
                        type="date"
                        value={eventForm.eventDate}
                        onChange={(e) => setEventForm({ ...eventForm, eventDate: e.target.value })}
                        required
                      />
                      <Input
                        label="Organizer"
                        placeholder="Department of CSE..."
                        value={eventForm.organizer}
                        onChange={(e) => setEventForm({ ...eventForm, organizer: e.target.value })}
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-medium mb-1">Description</label>
                      <textarea
                        rows={2}
                        value={eventForm.description}
                        onChange={(e) => setEventForm({ ...eventForm, description: e.target.value })}
                        className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-3 text-[var(--color-foreground)]"
                      />
                    </div>
                    <div className="flex justify-end gap-2 pt-2">
                      <Button size="sm" variant="ghost" onClick={() => setShowAddEvent(false)}>Cancel</Button>
                      <Button size="sm" type="submit" loading={addingEvent}>Create Event</Button>
                    </div>
                  </form>
                )}

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {eventsList.map((e) => (
                    <div key={e.id} className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-2">
                      <div className="flex items-center justify-between">
                        <Badge variant="primary" size="sm">{e.category}</Badge>
                        <Button size="sm" variant="ghost" onClick={() => handleDeleteEvent(e.id)}>
                          <Trash2 className="w-3.5 h-3.5 text-red-500" />
                        </Button>
                      </div>
                      <h4 className="text-xs font-bold text-[var(--color-foreground)]">{e.title}</h4>
                      <p className="text-[11px] text-[var(--color-muted-foreground)]">{e.description}</p>
                      <div className="pt-2 border-t border-[var(--color-border)]/60 text-[10px] text-[var(--color-muted-foreground)] flex items-center justify-between">
                        <span>📅 {e.eventDate} ({e.eventTime})</span>
                        <span>📍 {e.location}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </CardBody>
            </Card>
          ) : adminView === 'documents' ? (
            /* ================= RAG KNOWLEDGE BASE MANAGEMENT VIEW ================= */
            <div className="space-y-6">
              {/* RAG Cluster Status Banner */}
              <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <div className={`p-2.5 rounded-xl ${ragStatus?.ragflowHealthy ? 'bg-emerald-500/10 text-emerald-600' : 'bg-sky-500/10 text-sky-600'}`}>
                    <Cpu className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h4 className="text-sm font-bold text-[var(--color-foreground)]">Enterprise RAG Engine</h4>
                      <Badge variant={ragStatus?.ragflowHealthy ? 'success' : 'primary'} size="sm">
                        {ragStatus?.ragflowHealthy ? 'RAGFlow v0.16.0 Connected' : 'Embedded Semantic Vector Active'}
                      </Badge>
                    </div>
                    <p className="text-xs text-[var(--color-muted-foreground)]">
                      {ragStatus?.statusMessage || 'Hybrid institutional knowledge retrieval with dense subword vector similarity & citations'}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-4 text-xs font-mono text-[var(--color-muted-foreground)]">
                  <div>
                    <span className="text-[10px] uppercase text-[var(--color-muted-foreground)] block">Documents</span>
                    <strong className="text-sm text-[var(--color-foreground)]">{ragStatus?.totalDocuments || documentsList.length}</strong>
                  </div>
                  <div className="border-l border-[var(--color-border)] pl-4">
                    <span className="text-[10px] uppercase text-[var(--color-muted-foreground)] block">Indexed Chunks</span>
                    <strong className="text-sm text-[var(--color-foreground)]">{ragStatus?.totalChunks || (documentsList.length * 4)}</strong>
                  </div>
                  <div className="border-l border-[var(--color-border)] pl-4">
                    <span className="text-[10px] uppercase text-[var(--color-muted-foreground)] block">Active Provider</span>
                    <strong className="text-xs text-[var(--color-primary)]">{ragStatus?.activeProvider || 'EMBEDDED'}</strong>
                  </div>
                </div>
              </div>

              {/* RAG Query Tester Card */}
              <Card>
                <CardHeader
                  title="Test Knowledge Retrieval & Citations"
                  subtitle="Verify retrieval scores, section chunking, and citations against the active RAG engine"
                />
                <CardBody className="p-4 space-y-3">
                  <form onSubmit={handleTestRagQuery} className="flex gap-2">
                    <Input
                      placeholder="e.g. What is the condonation fee if attendance is 68%? or hostel visitor rules..."
                      value={ragTestQuery}
                      onChange={(e) => setRagTestQuery(e.target.value)}
                      className="flex-1"
                    />
                    <Button type="submit" size="sm" loading={testingRag} icon={Search}>
                      Test Retrieval
                    </Button>
                  </form>

                  {ragTestResults.length > 0 && (
                    <div className="space-y-2 pt-2 border-t border-[var(--color-border)]">
                      <span className="text-xs font-semibold text-[var(--color-primary)]">
                        Top {ragTestResults.length} Retrieved Chunk(s):
                      </span>
                      {ragTestResults.map((chunk, cIdx) => (
                        <div key={cIdx} className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] space-y-1.5 text-xs">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <Badge variant="primary" size="sm">Score: {chunk.score ? Math.round(chunk.score * 100) / 100 : 'N/A'}</Badge>
                              <strong className="text-[var(--color-foreground)]">{chunk.documentTitle}</strong>
                            </div>
                            <span className="text-[10px] font-mono text-[var(--color-muted-foreground)]">
                              Page {chunk.pageNumber} • Para {chunk.paragraphNumber} • Provider: {chunk.provider || 'RAG'}
                            </span>
                          </div>
                          <p className="text-[11px] text-[var(--color-muted-foreground)] bg-[var(--color-card)] p-2 rounded border border-[var(--color-border)]/60 font-mono">
                            "{chunk.content}"
                          </p>
                          <div className="text-[10px] text-sky-600 dark:text-sky-400 font-mono">
                            {chunk.citation || `📌 Source: ${chunk.documentTitle} (Page ${chunk.pageNumber})`}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardBody>
              </Card>

              {/* Document Management Card */}
              <Card>
                <CardHeader
                  title="Statutory Documents & Institutional Knowledge Base"
                  subtitle="Upload official regulations, handbooks, and policy circulars"
                  action={
                    <Button size="sm" icon={Plus} onClick={() => setShowAddDoc(!showAddDoc)}>
                      Upload Document
                    </Button>
                  }
                />
                <CardBody className="p-5">
                  {showAddDoc && (
                    <form onSubmit={handleCreateDocument} className="mb-6 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)] space-y-3">
                      <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-primary)]">Upload Institutional Document</h4>
                      
                      {/* File Upload Option */}
                      <div className="p-3 border-2 border-dashed border-[var(--color-border)] rounded-xl bg-[var(--color-card)] text-center space-y-1">
                        <input
                          type="file"
                          id="ragDocFile"
                          accept=".pdf,.docx,.txt"
                          className="hidden"
                          onChange={(e) => {
                            const f = e.target.files?.[0];
                            if (f) {
                              setSelectedDocFile(f);
                              if (!docForm.title) setDocForm({ ...docForm, title: f.name.replace(/\.[^/.]+$/, "") });
                            }
                          }}
                        />
                        <label htmlFor="ragDocFile" className="cursor-pointer block">
                          <FileText className="w-6 h-6 mx-auto text-[var(--color-primary)] mb-1" />
                          <span className="text-xs font-medium text-[var(--color-foreground)]">
                            {selectedDocFile ? selectedDocFile.name : 'Click to select PDF, DOCX, or TXT file'}
                          </span>
                          <span className="text-[10px] text-[var(--color-muted-foreground)] block">
                            {selectedDocFile ? `${(selectedDocFile.size / 1024).toFixed(1)} KB selected` : 'Or paste document content manually below'}
                          </span>
                        </label>
                        {selectedDocFile && (
                          <Button size="sm" variant="ghost" onClick={() => setSelectedDocFile(null)} className="text-xs text-red-500 py-0.5">
                            Remove File
                          </Button>
                        )}
                      </div>

                      <Input
                        label="Document Title"
                        placeholder="e.g. Autonomous Academic Regulations 2026..."
                        value={docForm.title}
                        onChange={(e) => setDocForm({ ...docForm, title: e.target.value })}
                        required
                      />
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div>
                          <label className="block text-xs font-medium mb-1">Category</label>
                          <select
                            value={docForm.category}
                            onChange={(e) => setDocForm({ ...docForm, category: e.target.value })}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2"
                          >
                            <option value="REGULATION">Academic Regulations</option>
                            <option value="EXAM_RULES">Examination & Assessment Rules</option>
                            <option value="CAMPUS_GUIDE">Campus & Hostel Guide</option>
                            <option value="POLICY">Placement & Training Policy</option>
                            <option value="SYLLABUS">Course Syllabus</option>
                          </select>
                        </div>
                        <Input
                          label="Department"
                          value={docForm.department}
                          onChange={(e) => setDocForm({ ...docForm, department: e.target.value })}
                        />
                        <Input
                          label="Version"
                          value={docForm.version}
                          onChange={(e) => setDocForm({ ...docForm, version: e.target.value })}
                        />
                      </div>
                      
                      {!selectedDocFile && (
                        <div>
                          <label className="block text-xs font-medium mb-1">Full Document Text / Content for RAG Indexing</label>
                          <textarea
                            rows={6}
                            placeholder="Paste document text or extracted paragraphs here for chunking & retrieval..."
                            value={docForm.content}
                            onChange={(e) => setDocForm({ ...docForm, content: e.target.value })}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-3 text-[var(--color-foreground)] font-mono"
                            required={!selectedDocFile}
                          />
                        </div>
                      )}
                      
                      <div className="flex justify-end gap-2 pt-2">
                        <Button size="sm" variant="ghost" onClick={() => { setShowAddDoc(false); setSelectedDocFile(null); }}>Cancel</Button>
                        <Button size="sm" type="submit" loading={addingDoc}>Save & Index Document</Button>
                      </div>
                    </form>
                  )}

                  <div className="space-y-3">
                    {documentsList.map((doc) => (
                      <div key={doc.id} className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-2">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Badge variant="primary" size="sm">{doc.category}</Badge>
                            <span className="text-[10px] font-mono text-[var(--color-muted-foreground)]">Ver: {doc.version} • {doc.department}</span>
                          </div>
                          <Button size="sm" variant="ghost" onClick={() => handleDeleteDocument(doc.id)}>
                            <Trash2 className="w-3.5 h-3.5 text-red-500" />
                          </Button>
                        </div>
                        <h4 className="text-xs font-bold text-[var(--color-foreground)]">{doc.title}</h4>
                        <p className="text-[11px] text-[var(--color-muted-foreground)] line-clamp-3">{doc.content}</p>
                        <p className="text-[10px] text-emerald-600 dark:text-emerald-400 font-medium">✓ Active in Document/RAG Agent chunk index</p>
                      </div>
                    ))}
                  </div>
                </CardBody>
              </Card>
            </div>
          ) : adminView === 'timetables' ? (
            /* ================= TIMETABLE MASTER VIEW ================= */
            <div className="space-y-6">
              <Card>
                <CardHeader
                  title="Master Timetable Schedule"
                  subtitle="Configure timetable allocations across departments and sections"
                  action={
                    <div className="flex items-center gap-2">
                      <select
                        value={ttFilterDept}
                        onChange={(e) => setTtFilterDept(e.target.value)}
                        className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs py-1.5 px-2.5 text-[var(--color-foreground)]"
                      >
                        {DEPARTMENTS.map((d) => (
                          <option key={d} value={d}>{d}</option>
                        ))}
                      </select>
                      <select
                        value={ttFilterSec}
                        onChange={(e) => setTtFilterSec(e.target.value)}
                        className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs py-1.5 px-2.5 text-[var(--color-foreground)]"
                      >
                        {SECTIONS.map((s) => (
                          <option key={s} value={s}>Section {s}</option>
                        ))}
                      </select>
                      <Button size="sm" icon={Plus} onClick={() => setShowAddTt(!showAddTt)}>
                        Add Entry
                      </Button>
                    </div>
                  }
                />
                <CardBody className="p-0">
                  {showAddTt && (
                    <form onSubmit={handleCreateTimetable} className="p-5 border-b border-[var(--color-border)] bg-[var(--color-background)] space-y-3">
                      <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--color-primary)]">Add Class Period</h4>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <Input
                          label="Subject Code"
                          placeholder="CS301"
                          value={ttForm.subjectCode}
                          onChange={(e) => setTtForm({ ...ttForm, subjectCode: e.target.value })}
                          required
                        />
                        <Input
                          label="Subject Name"
                          placeholder="Database Management Systems"
                          value={ttForm.subjectName}
                          onChange={(e) => setTtForm({ ...ttForm, subjectName: e.target.value })}
                          required
                        />
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <Input
                          label="Faculty Name"
                          placeholder="Dr. M. Priya"
                          value={ttForm.facultyName}
                          onChange={(e) => setTtForm({ ...ttForm, facultyName: e.target.value })}
                          required
                        />
                        <Input
                          label="Classroom"
                          placeholder="CS-204"
                          value={ttForm.classroom}
                          onChange={(e) => setTtForm({ ...ttForm, classroom: e.target.value })}
                          required
                        />
                        <div>
                          <label className="block text-xs font-medium mb-1">Day of Week</label>
                          <select
                            value={ttForm.dayOfWeek}
                            onChange={(e) => setTtForm({ ...ttForm, dayOfWeek: e.target.value })}
                            className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2"
                          >
                            {DAYS.map(day => <option key={day} value={day}>{day}</option>)}
                          </select>
                        </div>
                      </div>
                      <div className="flex justify-end gap-2 pt-2">
                        <Button size="sm" variant="ghost" onClick={() => setShowAddTt(false)}>Cancel</Button>
                        <Button size="sm" type="submit" loading={addingTt}>Save Entry</Button>
                      </div>
                    </form>
                  )}

                  <div className="divide-y divide-[var(--color-border)]">
                    {timetables.map((item) => (
                      <div key={item.id} className="p-3.5 flex items-center justify-between hover:bg-[var(--color-background)]/50">
                        <div className="flex items-center gap-3">
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
                              {item.facultyName} • {item.classroom} • <span className="font-semibold text-[var(--color-primary)]">{item.dayOfWeek}</span>
                            </p>
                          </div>
                        </div>
                        <Button size="sm" variant="ghost" onClick={() => handleDeleteTimetable(item.id)}>
                          <Trash2 className="w-3.5 h-3.5 text-red-500" />
                        </Button>
                      </div>
                    ))}
                  </div>
                </CardBody>
              </Card>
            </div>
          ) : (adminView === 'complaints' || activeTab === 'complaints') ? (
            /* ================= CAMPUS COMPLAINTS & GRIEVANCE ROUTING HUB ================= */
            <div className="space-y-6">
              <Card>
                <CardHeader
                  title="Campus Grievance & Facility Maintenance Operations Hub"
                  subtitle="Unified routing engine: Faculty reports routed to Admin & full oversight of student-to-mentor complaints"
                  action={
                    <Button
                      size="sm"
                      variant="ghost"
                      loading={complaintsLoading}
                      onClick={() => fetchComplaints(complaintsFilerFilter, complaintsStatusFilter)}
                    >
                      Refresh Tickets
                    </Button>
                  }
                />
                <CardBody className="p-5 space-y-4">
                  {/* Filter Toolbar */}
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-3 border-b border-[var(--color-border)]">
                    {/* Filer Role Tabs */}
                    <div className="flex items-center gap-1.5 p-1 rounded-xl bg-[var(--color-muted)]/40 border border-[var(--color-border)] overflow-x-auto no-scrollbar">
                      {[
                        { id: 'ALL', label: 'All Tickets' },
                        { id: 'FACULTY', label: 'Faculty Reports (Routed to Admin)' },
                        { id: 'STUDENT', label: 'Student Grievances (Mentor Assigned)' },
                      ].map((tab) => (
                        <button
                          key={tab.id}
                          onClick={() => {
                            setComplaintsFilerFilter(tab.id);
                            fetchComplaints(tab.id, complaintsStatusFilter);
                          }}
                          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                            complaintsFilerFilter === tab.id
                              ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                              : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                          }`}
                        >
                          {tab.label}
                        </button>
                      ))}
                    </div>

                    {/* Status Filter */}
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-[var(--color-muted-foreground)] font-medium">Status:</span>
                      <select
                        value={complaintsStatusFilter}
                        onChange={(e) => {
                          const newStatus = e.target.value;
                          setComplaintsStatusFilter(newStatus);
                          fetchComplaints(complaintsFilerFilter, newStatus);
                        }}
                        className="rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs py-1.5 px-2.5 text-[var(--color-foreground)]"
                      >
                        <option value="ALL">All Statuses</option>
                        <option value="OPEN">Open</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="RESOLVED">Resolved</option>
                      </select>
                    </div>
                  </div>

                  {/* Grievances List */}
                  {complaintsList.length === 0 ? (
                    <div className="p-12 text-center text-xs text-[var(--color-muted-foreground)]">
                      <CheckCircle2 className="w-8 h-8 mx-auto mb-2 text-emerald-500 opacity-80" />
                      <p className="font-semibold text-[var(--color-foreground)]">No Grievances Found</p>
                      <p className="mt-1">No complaint tickets match the selected filters.</p>
                    </div>
                  ) : (
                    <div className="divide-y divide-[var(--color-border)]">
                      {complaintsList.map((c) => (
                        <div key={c.id} className="py-4 space-y-3">
                          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-2">
                            <div className="space-y-1.5 flex-1">
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
                                <Badge
                                  variant={c.submitterRole === 'FACULTY' ? 'info' : 'outline'}
                                  size="sm"
                                >
                                  Submitter: {c.submitterName || 'User'} ({c.submitterRole || 'USER'})
                                </Badge>
                                <Badge
                                  variant={c.routedTo === 'ADMIN' ? 'danger' : 'primary'}
                                  size="sm"
                                >
                                  Routed To: {c.assignedTo || (c.routedTo === 'ADMIN' ? 'ADMIN (Operations)' : 'Class Mentor')}
                                </Badge>
                              </div>

                              <p className="text-xs text-[var(--color-foreground)] font-medium pt-0.5">
                                {c.description}
                              </p>

                              <p className="text-[10px] text-[var(--color-muted-foreground)] flex flex-wrap items-center gap-2 pt-0.5">
                                <span>Location: <strong className="text-[var(--color-foreground)]">{c.location}</strong></span>
                                <span>•</span>
                                <span>Urgency: <strong className="text-[var(--color-foreground)]">{c.urgency}</strong></span>
                                {c.department && (
                                  <>
                                    <span>•</span>
                                    <span>Dept: <strong className="text-[var(--color-foreground)]">{c.department}</strong></span>
                                  </>
                                )}
                                <span>•</span>
                                <span>Contact: <strong>{c.submitterEmail}</strong></span>
                              </p>
                            </div>

                            <div className="flex flex-col sm:items-end gap-2 shrink-0">
                              <span className="text-[11px] font-mono text-[var(--color-muted-foreground)]">
                                {c.createdAt ? c.createdAt.substring(0, 10) : 'Recent'}
                              </span>

                              {c.status !== 'RESOLVED' && (
                                <div className="flex items-center gap-1.5">
                                  {c.status === 'OPEN' && (
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => handleUpdateComplaintStatus(c.id, 'IN_PROGRESS')}
                                    >
                                      In Progress
                                    </Button>
                                  )}
                                  <Button
                                    size="sm"
                                    onClick={() => {
                                      setResolvingComplaintId(resolvingComplaintId === c.id ? null : c.id);
                                      setAdminResolutionNotes(c.resolutionNotes || '');
                                    }}
                                  >
                                    Resolve
                                  </Button>
                                </div>
                              )}
                            </div>
                          </div>

                          {/* Resolution Notes Display */}
                          {c.resolutionNotes && (
                            <div className="p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-xs text-emerald-800 dark:text-emerald-300">
                              <span className="font-bold">Official Resolution Note: </span>
                              {c.resolutionNotes}
                            </div>
                          )}

                          {/* Inline Resolution Box */}
                          {resolvingComplaintId === c.id && (
                            <div className="p-4 rounded-xl border border-[var(--color-primary)]/40 bg-[var(--color-background)] space-y-3">
                              <p className="text-xs font-bold text-[var(--color-foreground)]">
                                Resolve Grievance Ticket #{c.ticketNumber}
                              </p>
                              <textarea
                                rows={2}
                                value={adminResolutionNotes}
                                onChange={(e) => setAdminResolutionNotes(e.target.value)}
                                placeholder="Enter operational resolution details (e.g. Facilities dispatched technician; classroom AC serviced and verified operational)..."
                                className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-xs p-2.5 text-[var(--color-foreground)] focus:ring-1 focus:ring-[var(--color-primary)] outline-none"
                              />
                              <div className="flex justify-end gap-2">
                                <Button size="sm" variant="ghost" onClick={() => setResolvingComplaintId(null)}>
                                  Cancel
                                </Button>
                                <Button
                                  size="sm"
                                  loading={updatingComplaint}
                                  onClick={() => handleUpdateComplaintStatus(c.id, 'RESOLVED', adminResolutionNotes)}
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
          ) : (
            /* ================= DEFAULT OVERVIEW DASHBOARD ================= */
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Left Column (2 Cols) */}
              <div className="lg:col-span-2 space-y-6">
                {/* Proactive Conflict Engine */}
                <Card>
                  <CardHeader
                    title="Autonomous Timetable Conflict Engine"
                    subtitle="Proactively evaluates multi-faculty room and schedule overlaps"
                    action={
                      <Button size="sm" variant="outline" loading={scanningConflicts} onClick={handleScanConflicts}>
                        Scan Engine
                      </Button>
                    }
                  />
                  <CardBody className="p-4">
                    {conflicts.length === 0 ? (
                      <div className="p-4 text-center text-xs text-emerald-600 dark:text-emerald-400 font-medium">
                        ✓ Optimal Schedule: No overlapping room allocations or faculty collisions detected.
                      </div>
                    ) : (
                      <div className="space-y-3">
                        {conflicts.map((c, idx) => (
                          <div key={idx} className="p-3 rounded-lg border border-amber-500/30 bg-amber-500/10 text-xs flex items-center justify-between">
                            <div>
                              <p className="font-bold text-[var(--color-foreground)]">{c.issue}</p>
                              <p className="text-[11px] text-[var(--color-muted-foreground)] mt-0.5">{c.recommendation}</p>
                            </div>
                            <Badge variant="warning" size="sm">Conflict</Badge>
                          </div>
                        ))}
                      </div>
                    )}
                  </CardBody>
                </Card>

                {/* HITL Approvals */}
                <Card>
                  <CardHeader
                    title="Human-in-the-Loop (HITL) Agent Mutations"
                    subtitle="Sign-off required for autonomous timetable schedule updates"
                  />
                  <CardBody className="p-0">
                    {(!data?.approvals || data.approvals.length === 0) ? (
                      <div className="p-6 text-center text-xs text-[var(--color-muted-foreground)]">
                        No pending administrative approvals.
                      </div>
                    ) : (
                      <div className="divide-y divide-[var(--color-border)]">
                        {data.approvals.slice(0, 4).map((a) => (
                          <div key={a.id} className="p-3.5 flex items-center justify-between hover:bg-[var(--color-background)]/50">
                            <div>
                              <div className="flex items-center gap-2">
                                <Badge variant={a.status === 'PENDING' ? 'warning' : a.status === 'APPROVED' ? 'success' : 'danger'} size="sm">
                                  {a.status}
                                </Badge>
                                <span className="font-mono text-xs font-bold text-[var(--color-foreground)]">{a.actionType}</span>
                              </div>
                              <p className="text-xs text-[var(--color-foreground)] font-medium mt-1">{a.reason}</p>
                            </div>
                            {a.status === 'PENDING' && (
                              <div className="flex items-center gap-2">
                                <Button size="sm" variant="ghost" onClick={() => handleDecision(a.id, 'REJECTED')} disabled={processingId === a.id}>
                                  Reject
                                </Button>
                                <Button size="sm" onClick={() => handleDecision(a.id, 'APPROVED')} loading={processingId === a.id}>
                                  Approve
                                </Button>
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </CardBody>
                </Card>

                {/* Agent Execution Logs */}
                <Card>
                  <CardHeader
                    title="Multi-Agent Live Execution Audit Logs"
                    subtitle="Actual runtime delegation across Orchestrator, Academic, RAG, and Schedule Agents"
                  />
                  <CardBody className="p-0">
                    <div className="divide-y divide-[var(--color-border)]">
                      {agentLogs.slice(0, 5).map((log) => (
                        <div key={log.id} className="p-3 hover:bg-[var(--color-background)]/50 transition-colors">
                          <div className="flex items-center justify-between mb-1">
                            <span className="font-mono text-[10px] font-bold text-[var(--color-primary)]">
                              {log.agentType || 'Orchestrator Agent'}
                            </span>
                            <span className="text-[10px] text-[var(--color-muted-foreground)] font-mono">
                              ⚡ {log.latencyMs}ms
                            </span>
                          </div>
                          <p className="text-xs font-medium text-[var(--color-foreground)]">"{log.userQuery}"</p>
                          {log.toolsUsed && (
                            <p className="text-[10px] text-[var(--color-muted-foreground)] mt-0.5">
                              Tools: <span className="font-mono text-[var(--color-primary)]">{log.toolsUsed}</span>
                            </p>
                          )}
                        </div>
                      ))}
                    </div>
                  </CardBody>
                </Card>
              </div>

              {/* Right Column (1 Col) */}
              <div className="space-y-6">
                <AgentXAssistant onActionCompleted={() => { fetchAdminData(); fetchFaculty(); fetchStudents(); }} />
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default AdminPortal;
