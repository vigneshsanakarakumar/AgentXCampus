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
import Modal from '../components/ui/Modal';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import {
  Users,
  BookOpen,
  Calendar,
  AlertTriangle,
  CheckCircle,
  Plus,
  Search,
  Filter,
  GraduationCap,
  FileText,
  ChevronDown,
  ChevronUp,
  Clock,
  Layers,
  ArrowRight
} from 'lucide-react';

export const FacultyClassRosterPage = () => {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [sections, setSections] = useState([]);
  const [selectedSectionId, setSelectedSectionId] = useState(null);
  const [loadingSections, setLoadingSections] = useState(true);

  const [summary, setSummary] = useState(null);
  const [roster, setRoster] = useState([]);
  const [assignments, setAssignments] = useState([]);
  const [loadingData, setLoadingData] = useState(false);

  const [activeTab, setActiveTab] = useState('roster');
  const [searchQuery, setSearchQuery] = useState('');
  const [filterDeficitOnly, setFilterDeficitOnly] = useState(false);
  const [expandedStudentId, setExpandedStudentId] = useState(null);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submittingAssignment, setSubmittingAssignment] = useState(false);
  const [assignmentForm, setAssignmentForm] = useState({
    title: '',
    subjectCode: '',
    subjectName: '',
    description: '',
    dueDate: '',
    priority: 'MEDIUM',
    maxMarks: 100
  });

  const fetchMentorSections = async () => {
    setLoadingSections(true);
    try {
      const res = await api.get('/faculty/mentor-sections');
      const list = res.data || [];
      setSections(list);
      if (list.length > 0) {
        setSelectedSectionId(list[0].id);
      }
    } catch (err) {
      console.error(err);
      addToast('Could not load assigned mentor sections', 'error');
    } finally {
      setLoadingSections(false);
    }
  };

  useEffect(() => {
    fetchMentorSections();
  }, []);

  const fetchSectionDetails = async (sectionId) => {
    if (!sectionId) return;
    setLoadingData(true);
    try {
      const [sumRes, rosRes, assignRes] = await Promise.all([
        api.get('/faculty/mentor-sections/' + sectionId + '/attendance-summary'),
        api.get('/faculty/mentor-sections/' + sectionId + '/roster'),
        api.get('/faculty/mentor-sections/' + sectionId + '/assignments')
      ]);

      setSummary(sumRes.data);
      setRoster(rosRes.data || []);
      setAssignments(assignRes.data || []);
    } catch (err) {
      console.error('Error loading section details', err);
      if (err.response && err.response.status === 403) {
        addToast('Forbidden: You are not authorized as the mentor of this section.', 'error');
      } else {
        addToast('Failed to load class section data.', 'error');
      }
    } finally {
      setLoadingData(false);
    }
  };

  useEffect(() => {
    if (selectedSectionId) {
      fetchSectionDetails(selectedSectionId);
    }
  }, [selectedSectionId]);

  const handleCreateAssignment = async (e) => {
    e.preventDefault();
    if (!selectedSectionId) {
      addToast('Please select a valid section first.', 'error');
      return;
    }
    if (!assignmentForm.title || !assignmentForm.subjectCode || !assignmentForm.dueDate) {
      addToast('Please fill out the Title, Subject Code, and Due Date.', 'error');
      return;
    }

    setSubmittingAssignment(true);
    try {
      await api.post('/faculty/mentor-sections/' + selectedSectionId + '/assignments', {
        ...assignmentForm,
        maxMarks: Number(assignmentForm.maxMarks) || 100
      });
      addToast('Coursework successfully assigned to all students in this section!', 'success');
      setIsModalOpen(false);
      setAssignmentForm({
        title: '',
        subjectCode: '',
        subjectName: '',
        description: '',
        dueDate: '',
        priority: 'MEDIUM',
        maxMarks: 100
      });
      fetchSectionDetails(selectedSectionId);
    } catch (err) {
      const msg = err.response && err.response.data && err.response.data.message ? err.response.data.message : 'Failed to publish coursework.';
      addToast(msg, 'error');
    } finally {
      setSubmittingAssignment(false);
    }
  };

  const filteredRoster = roster.filter((s) => {
    const q = searchQuery.toLowerCase();
    const matchesSearch =
      (s.name && s.name.toLowerCase().includes(q)) ||
      (s.rollNumber && s.rollNumber.toLowerCase().includes(q)) ||
      (s.email && s.email.toLowerCase().includes(q));
    const matchesDeficit = filterDeficitOnly ? s.standing === 'ATTENDANCE_DEFICIT' || s.attendanceRate < 75 : true;
    return matchesSearch && matchesDeficit;
  });

  const selectedSection = sections.find((s) => s.id === selectedSectionId);

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col">
      <Navbar />

      <div className="flex-1 flex flex-col md:flex-row">
        <Sidebar />

        <main className="flex-1 p-6 md:p-8 overflow-y-auto max-w-7xl">
          {/* Header Banner */}
          <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4 pb-4 border-b border-[var(--color-border)]">
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-bold text-[var(--color-foreground)] tracking-tight">
                  Class Roster & Section Management
                </h1>
                <Badge variant="primary" size="sm">Faculty Mentor</Badge>
              </div>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-1">
                Monitor student attendance health, academic progress, and issue coursework tasks to your assigned section cohort.
              </p>
            </div>

            {/* Section Selector / Actions */}
            <div className="flex items-center gap-3 flex-wrap">
              {sections.length > 1 && (
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-[var(--color-muted-foreground)]">Section:</span>
                  <select
                    value={selectedSectionId || ''}
                    onChange={(e) => setSelectedSectionId(Number(e.target.value))}
                    className="text-xs bg-[var(--color-card)] border border-[var(--color-border)] rounded-lg px-3 py-2 text-[var(--color-foreground)] font-medium focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]"
                  >
                    {sections.map((sec) => (
                      <option key={sec.id} value={sec.id}>
                        {sec.department} - Sec {sec.sectionName} (Sem {sec.semester})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {selectedSection && (
                <Button
                  variant="primary"
                  size="sm"
                  icon={Plus}
                  onClick={() => setIsModalOpen(true)}
                >
                  Assign Work to Class
                </Button>
              )}
            </div>
          </div>

          {/* If No Assigned Sections */}
          {loadingSections ? (
            <div className="p-16 flex items-center justify-center">
              <LoadingSpinner size="lg" message="Loading your assigned mentor sections..." />
            </div>
          ) : sections.length === 0 ? (
            <Card className="border-amber-200 dark:border-amber-900/50 bg-amber-50/30 dark:bg-amber-950/10">
              <CardBody className="p-8 text-center">
                <GraduationCap className="w-12 h-12 mx-auto mb-3 text-amber-500 opacity-80" />
                <h3 className="text-base font-bold text-[var(--color-foreground)]">No Class Section Assigned</h3>
                <p className="text-xs text-[var(--color-muted-foreground)] max-w-md mx-auto mt-2 leading-relaxed">
                  Your faculty account is not currently assigned as a class mentor for any section. Please request your department head or administrator to assign a class section to your profile.
                </p>
                <div className="mt-4">
                  <Link to="/faculty/dashboard">
                    <Button variant="outline" size="sm">
                      Return to Faculty Overview
                    </Button>
                  </Link>
                </div>
              </CardBody>
            </Card>
          ) : (
            <>
              {/* Cohort Info Banner */}
              {selectedSection && (
                <div className="mb-6 p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 shadow-sm">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center">
                      <Layers className="w-5 h-5" />
                    </div>
                    <div>
                      <h2 className="text-sm font-bold text-[var(--color-foreground)]">
                        {selectedSection.department} — Section {selectedSection.sectionName}
                      </h2>
                      <p className="text-xs text-[var(--color-muted-foreground)] flex items-center gap-2 mt-0.5">
                        <span>Semester: <strong>{selectedSection.semester}</strong></span>
                        <span>•</span>
                        <span>Academic Year: <strong>{selectedSection.academicYear || 'Current'}</strong></span>
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-[11px] font-medium text-[var(--color-muted-foreground)]">
                      Faculty Mentor: <strong className="text-[var(--color-foreground)]">{user && user.firstName} {user && user.lastName}</strong>
                    </span>
                  </div>
                </div>
              )}

              {/* KPI Summary Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                <StatCard
                  title="Enrolled Students"
                  value={summary ? '' + summary.totalStudents : '0'}
                  subtitle={'Active in Section ' + (selectedSection ? selectedSection.sectionName : '')}
                  icon={Users}
                  badge={<Badge variant="primary">Enrolled</Badge>}
                />
                <StatCard
                  title="Average Attendance"
                  value={summary ? summary.averageAttendance + '%' : '0%'}
                  subtitle="Class aggregate attendance"
                  icon={CheckCircle}
                  badge={
                    <Badge variant={summary && summary.averageAttendance >= 75 ? 'success' : 'danger'}>
                      {summary && summary.averageAttendance >= 75 ? 'Optimal' : 'Low Average'}
                    </Badge>
                  }
                />
                <StatCard
                  title="At-Risk (<75%)"
                  value={summary ? '' + summary.atRiskCount : '0'}
                  subtitle="Students with deficit"
                  icon={AlertTriangle}
                  badge={
                    <Badge variant={summary && summary.atRiskCount > 0 ? 'danger' : 'success'}>
                      {summary && summary.atRiskCount > 0 ? 'Requires Action' : 'All Clear'}
                    </Badge>
                  }
                />
                <StatCard
                  title="Class Average CGPA"
                  value={summary ? '' + summary.averageCgpa : '0.0'}
                  subtitle="Overall academic benchmark"
                  icon={GraduationCap}
                  badge={<Badge variant="info">Scale 10.0</Badge>}
                />
              </div>

              {/* Tabs */}
              <div className="flex items-center justify-between border-b border-[var(--color-border)] mb-6">
                <div className="flex space-x-4">
                  <button
                    onClick={() => setActiveTab('roster')}
                    className={'pb-3 text-xs font-semibold flex items-center gap-2 border-b-2 transition-all ' + (
                      activeTab === 'roster'
                        ? 'border-[var(--color-primary)] text-[var(--color-primary)]'
                        : 'border-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                    )}
                  >
                    <Users className="w-4 h-4" />
                    <span>Class Student Roster ({roster.length})</span>
                  </button>

                  <button
                    onClick={() => setActiveTab('assignments')}
                    className={'pb-3 text-xs font-semibold flex items-center gap-2 border-b-2 transition-all ' + (
                      activeTab === 'assignments'
                        ? 'border-[var(--color-primary)] text-[var(--color-primary)]'
                        : 'border-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                    )}
                  >
                    <FileText className="w-4 h-4" />
                    <span>Assigned Coursework ({assignments.length})</span>
                  </button>
                </div>
              </div>

              {/* Tab 1: Class Student Roster */}
              {activeTab === 'roster' && (
                <Card>
                  <CardHeader
                    title="Student Roster & Performance Overview"
                    subtitle="Detailed attendance percentages and CGPA metrics retrieved from AcademicAgent records"
                    action={
                      <div className="flex items-center gap-2 flex-wrap">
                        <div className="relative">
                          <Search className="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
                          <input
                            type="text"
                            placeholder="Filter by name, roll no..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="text-xs bg-[var(--color-background)] border border-[var(--color-border)] rounded-lg pl-8 pr-3 py-1.5 text-[var(--color-foreground)] focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]"
                          />
                        </div>
                        <button
                          onClick={() => setFilterDeficitOnly(!filterDeficitOnly)}
                          className={'text-xs px-2.5 py-1.5 rounded-lg border font-medium flex items-center gap-1.5 transition-all ' + (
                            filterDeficitOnly
                              ? 'bg-rose-500/10 border-rose-500/30 text-rose-600 dark:text-rose-400'
                              : 'border-[var(--color-border)] text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                          )}
                        >
                          <Filter className="w-3 h-3" />
                          <span>Deficit Only</span>
                        </button>
                      </div>
                    }
                  />

                  <CardBody className="p-0">
                    {loadingData ? (
                      <div className="p-12 flex justify-center">
                        <LoadingSpinner size="md" message="Loading student roster..." />
                      </div>
                    ) : filteredRoster.length === 0 ? (
                      <div className="p-12 text-center text-xs text-[var(--color-muted-foreground)]">
                        <Users className="w-8 h-8 mx-auto mb-2 text-[var(--color-muted-foreground)] opacity-60" />
                        <p className="font-semibold text-[var(--color-foreground)]">No Students Found</p>
                        <p className="mt-1">
                          {searchQuery || filterDeficitOnly
                            ? 'No students matched the active search or filter criteria.'
                            : 'No students are enrolled in this section yet.'}
                        </p>
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-xs text-left">
                          <thead className="bg-[var(--color-background)] border-b border-[var(--color-border)] text-[var(--color-muted-foreground)] font-semibold">
                            <tr>
                              <th className="p-3">Student</th>
                              <th className="p-3">Contact</th>
                              <th className="p-3">Attendance</th>
                              <th className="p-3">CGPA / Grade</th>
                              <th className="p-3">Status</th>
                              <th className="p-3 text-right">Details</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-[var(--color-border)]">
                            {filteredRoster.map((student) => {
                              const isDeficit = student.standing === 'ATTENDANCE_DEFICIT' || student.attendanceRate < 75;
                              const isExpanded = expandedStudentId === student.studentId;

                              return (
                                <React.Fragment key={student.studentId || student.rollNumber}>
                                  <tr className="hover:bg-[var(--color-background)]/50 transition-colors">
                                    <td className="p-3">
                                      <div className="font-semibold text-[var(--color-foreground)]">
                                        {student.name}
                                      </div>
                                      <div className="text-[11px] font-mono font-bold text-[var(--color-primary)]">
                                        {student.rollNumber}
                                      </div>
                                    </td>

                                    <td className="p-3 text-[var(--color-muted-foreground)] font-mono text-[11px]">
                                      {student.email}
                                    </td>

                                    <td className="p-3">
                                      <div className="w-32">
                                        <div className="flex items-center justify-between text-[11px] mb-1">
                                          <span className={'font-bold ' + (isDeficit ? 'text-rose-600 dark:text-rose-400' : 'text-emerald-600 dark:text-emerald-400')}>
                                            {student.attendanceRate}%
                                          </span>
                                          {isDeficit && (
                                            <span className="text-[9px] uppercase font-bold text-rose-500">Deficit</span>
                                          )}
                                        </div>
                                        <div className="w-full bg-[var(--color-border)] rounded-full h-1.5 overflow-hidden">
                                          <div
                                            className={'h-1.5 rounded-full ' + (
                                              student.attendanceRate >= 85
                                                ? 'bg-emerald-500'
                                                : student.attendanceRate >= 75
                                                ? 'bg-amber-500'
                                                : 'bg-rose-500'
                                            )}
                                            style={{ width: `${Math.min(student.attendanceRate, 100)}%` }}
                                          />
                                        </div>
                                      </div>
                                    </td>

                                    <td className="p-3">
                                      <div className="flex items-center gap-2">
                                        <span className="font-bold text-[var(--color-foreground)] text-xs">
                                          {student.cgpa ? Number(student.cgpa).toFixed(2) : 'N/A'}
                                        </span>
                                        {student.letterGrade && (
                                          <Badge
                                            variant={
                                              ['O', 'A+', 'A'].includes(student.letterGrade)
                                                ? 'success'
                                                : ['B+', 'B'].includes(student.letterGrade)
                                                ? 'primary'
                                                : 'warning'
                                            }
                                            size="sm"
                                          >
                                            {student.letterGrade}
                                          </Badge>
                                        )}
                                      </div>
                                    </td>

                                    <td className="p-3">
                                      <Badge variant={isDeficit ? 'danger' : 'success'} size="sm">
                                        {isDeficit ? 'Attendance Risk' : 'Good Standing'}
                                      </Badge>
                                    </td>

                                    <td className="p-3 text-right">
                                      <button
                                        onClick={() => setExpandedStudentId(isExpanded ? null : student.studentId)}
                                        className="p-1 rounded hover:bg-[var(--color-border)] text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] transition-colors"
                                        title="View Subject Breakdown"
                                      >
                                        {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                                      </button>
                                    </td>
                                  </tr>

                                  {/* Subject Attendance Breakdown Accordion */}
                                  {isExpanded && (
                                    <tr className="bg-[var(--color-background)]/70">
                                      <td colSpan={6} className="p-4 border-t border-b border-[var(--color-border)]">
                                        <div className="text-xs">
                                          <div className="font-semibold text-[var(--color-foreground)] mb-2 flex items-center gap-2">
                                            <BookOpen className="w-3.5 h-3.5 text-[var(--color-primary)]" />
                                            <span>Subject-Wise Attendance Breakdown for {student.name}</span>
                                          </div>

                                          {(!student.subjectAttendance || student.subjectAttendance.length === 0) ? (
                                            <p className="text-[11px] text-[var(--color-muted-foreground)] italic">
                                              No individual subject attendance records found for this student.
                                            </p>
                                          ) : (
                                            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2 mt-2">
                                              {student.subjectAttendance.map((sub, idx) => (
                                                <div
                                                  key={idx}
                                                  className="p-2.5 rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[11px]"
                                                >
                                                  <div className="flex items-center justify-between font-semibold">
                                                    <span className="text-[var(--color-foreground)] truncate max-w-[130px]">{sub.subjectName}</span>
                                                    <span className="font-mono text-[var(--color-primary)]">{sub.subjectCode}</span>
                                                  </div>
                                                  <div className="flex items-center justify-between text-[var(--color-muted-foreground)] mt-1">
                                                    <span>{sub.attendedHours} / {sub.totalHours} hrs</span>
                                                    <span className={'font-bold ' + (sub.percentage < 75 ? 'text-rose-500' : 'text-emerald-600 dark:text-emerald-400')}>
                                                      {sub.percentage}%
                                                    </span>
                                                  </div>
                                                </div>
                                              ))}
                                            </div>
                                          )}
                                        </div>
                                      </td>
                                    </tr>
                                  )}
                                </React.Fragment>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </CardBody>
                </Card>
              )}

              {/* Tab 2: Assigned Coursework */}
              {activeTab === 'assignments' && (
                <Card>
                  <CardHeader
                    title={'Coursework Assigned to Section ' + (selectedSection ? selectedSection.sectionName : '')}
                    subtitle="All class tasks and homework distributed to enrolled students"
                    action={
                      <Button
                        variant="primary"
                        size="sm"
                        icon={Plus}
                        onClick={() => setIsModalOpen(true)}
                      >
                        Create New Assignment
                      </Button>
                    }
                  />

                  <CardBody className="p-0">
                    {loadingData ? (
                      <div className="p-12 flex justify-center">
                        <LoadingSpinner size="md" message="Loading section coursework..." />
                      </div>
                    ) : assignments.length === 0 ? (
                      <div className="p-12 text-center text-xs text-[var(--color-muted-foreground)]">
                        <FileText className="w-8 h-8 mx-auto mb-2 text-[var(--color-muted-foreground)] opacity-60" />
                        <p className="font-semibold text-[var(--color-foreground)]">No Coursework Assigned Yet</p>
                        <p className="mt-1">
                          You haven't assigned any coursework tasks to Section {selectedSection ? selectedSection.sectionName : ''} yet.
                        </p>
                        <div className="mt-4">
                          <Button
                            variant="outline"
                            size="sm"
                            icon={Plus}
                            onClick={() => setIsModalOpen(true)}
                          >
                            Assign First Task
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div className="divide-y divide-[var(--color-border)]">
                        {assignments.map((item) => (
                          <div key={item.id} className="p-4 hover:bg-[var(--color-background)]/50 transition-colors flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                            <div className="space-y-1">
                              <div className="flex items-center gap-2 flex-wrap">
                                <h4 className="text-xs font-bold text-[var(--color-foreground)]">
                                  {item.title}
                                </h4>
                                <Badge variant="primary" size="sm">
                                  {item.subjectCode || 'General'}
                                </Badge>
                                {item.priority && (
                                  <Badge
                                    variant={
                                      item.priority === 'URGENT'
                                        ? 'danger'
                                        : item.priority === 'HIGH'
                                        ? 'warning'
                                        : 'neutral'
                                    }
                                    size="sm"
                                  >
                                    {item.priority}
                                  </Badge>
                                )}
                              </div>
                              <p className="text-[11px] text-[var(--color-muted-foreground)]">
                                {item.subjectName} {item.description && ('— ' + item.description)}
                              </p>
                            </div>

                            <div className="flex items-center gap-4 text-xs shrink-0">
                              <div className="text-right">
                                <p className="text-[10px] uppercase font-bold text-[var(--color-muted-foreground)]">Due Date</p>
                                <p className="font-medium text-[var(--color-foreground)] flex items-center gap-1">
                                  <Clock className="w-3 h-3 text-[var(--color-primary)]" />
                                  <span>{item.dueDate ? new Date(item.dueDate).toLocaleDateString() : 'N/A'}</span>
                                </p>
                              </div>
                              <div className="text-right pl-3 border-l border-[var(--color-border)]">
                                <p className="text-[10px] uppercase font-bold text-[var(--color-muted-foreground)]">Max Marks</p>
                                <p className="font-bold text-[var(--color-primary)]">
                                  {(item.maxMarks || 100)} pts
                                </p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </CardBody>
                </Card>
              )}
            </>
          )}

          {/* Assign Coursework Modal */}
          <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            title={'Assign Coursework to ' + (selectedSection ? selectedSection.department + ' - Section ' + selectedSection.sectionName : '')}
            maxWidth="max-w-lg"
          >
            <form onSubmit={handleCreateAssignment} className="space-y-4 text-xs">
              <p className="text-[11px] text-[var(--color-muted-foreground)] leading-relaxed">
                This assignment will automatically create student tasks for every enrolled student in this section cohort and appear on their student dashboard.
              </p>

              <div>
                <label className="block font-semibold text-[var(--color-foreground)] mb-1">
                  Assignment Title *
                </label>
                <Input
                  type="text"
                  placeholder="e.g. Distributed Database Systems Lab 3"
                  value={assignmentForm.title}
                  onChange={(e) => setAssignmentForm({ ...assignmentForm, title: e.target.value })}
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-[var(--color-foreground)] mb-1">
                    Subject Code *
                  </label>
                  <Input
                    type="text"
                    placeholder="e.g. CS602"
                    value={assignmentForm.subjectCode}
                    onChange={(e) => setAssignmentForm({ ...assignmentForm, subjectCode: e.target.value })}
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-[var(--color-foreground)] mb-1">
                    Subject Name
                  </label>
                  <Input
                    type="text"
                    placeholder="e.g. Operating Systems"
                    value={assignmentForm.subjectName}
                    onChange={(e) => setAssignmentForm({ ...assignmentForm, subjectName: e.target.value })}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-[var(--color-foreground)] mb-1">
                    Due Date *
                  </label>
                  <Input
                    type="date"
                    value={assignmentForm.dueDate}
                    onChange={(e) => setAssignmentForm({ ...assignmentForm, dueDate: e.target.value })}
                    required
                  />
                </div>
                <div>
                  <label className="block font-semibold text-[var(--color-foreground)] mb-1">
                    Max Marks
                  </label>
                  <Input
                    type="number"
                    min="1"
                    max="1000"
                    placeholder="100"
                    value={assignmentForm.maxMarks}
                    onChange={(e) => setAssignmentForm({ ...assignmentForm, maxMarks: e.target.value })}
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-[var(--color-foreground)] mb-1">
                  Priority
                </label>
                <select
                  value={assignmentForm.priority}
                  onChange={(e) => setAssignmentForm({ ...assignmentForm, priority: e.target.value })}
                  className="w-full text-xs bg-[var(--color-background)] border border-[var(--color-border)] rounded-lg px-3 py-2 text-[var(--color-foreground)] focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </select>
              </div>

              <div>
                <label className="block font-semibold text-[var(--color-foreground)] mb-1">
                  Instructions / Description
                </label>
                <textarea
                  rows={3}
                  placeholder="Detailed guidelines, submission links, or task deliverables..."
                  value={assignmentForm.description}
                  onChange={(e) => setAssignmentForm({ ...assignmentForm, description: e.target.value })}
                  className="w-full text-xs bg-[var(--color-background)] border border-[var(--color-border)] rounded-lg p-2.5 text-[var(--color-foreground)] focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]"
                />
              </div>

              <div className="pt-2 flex items-center justify-end gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setIsModalOpen(false)}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                  loading={submittingAssignment}
                >
                  Distribute to Class
                </Button>
              </div>
            </form>
          </Modal>
        </main>
      </div>
    </div>
  );
};

export default FacultyClassRosterPage;
