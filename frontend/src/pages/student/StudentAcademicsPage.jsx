import React, { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import Card, { CardHeader, CardBody } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import StatCard from '../../components/ui/StatCard';
import {
  TrendingUp, BookOpen, Award, CheckCircle2, AlertTriangle,
  GraduationCap, Calendar, Clock, Filter
} from 'lucide-react';

export const StudentAcademicsPage = () => {
  const { data } = useOutletContext();
  const [assignmentFilter, setAssignmentFilter] = useState('ALL');

  const attendanceRecords = data?.attendanceRecords || [];
  const assignments = data?.assignments || [];

  const filteredAssignments = assignments.filter((a) => {
    if (assignmentFilter === 'HIGH_PRIORITY') return a.priority === 'HIGH';
    if (assignmentFilter === 'PENDING') return a.status !== 'SUBMITTED';
    if (assignmentFilter === 'SUBMITTED') return a.status === 'SUBMITTED';
    return true;
  });

  const overallAttendance = data?.attendance != null ? data.attendance : 85;
  const cgpa = data?.cgpa != null ? data.cgpa : 8.64;

  const lowAttendanceCourses = attendanceRecords.filter((r) => r.percentage < 75);

  return (
    <div className="space-y-6">
      {/* Top Academic Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Cumulative GPA (CGPA)"
          value={'' + cgpa}
          subtitle="Out of 10.0 scale"
          icon={Award}
          badge={<Badge variant="success">First Class Distinction</Badge>}
        />
        <StatCard
          title="Overall Attendance"
          value={overallAttendance + '%'}
          subtitle="Minimum 75% required"
          icon={TrendingUp}
          badge={<Badge variant={overallAttendance >= 75 ? 'success' : 'danger'}>
            {overallAttendance >= 75 ? 'Eligible for Exams' : 'Condonation Required'}
          </Badge>}
        />
        <StatCard
          title="Total Registered Courses"
          value={'' + attendanceRecords.length}
          subtitle="Semester 5 B.E. CSE"
          icon={BookOpen}
          badge={<Badge variant="primary">24 Credits</Badge>}
        />
        <StatCard
          title="Coursework Deliverables"
          value={'' + assignments.length}
          subtitle={`${assignments.filter(a => a.status === 'SUBMITTED').length} completed`}
          icon={GraduationCap}
          badge={<Badge variant="info">Active</Badge>}
        />
      </div>

      {/* Low Attendance Warning Alert if any */}
      {lowAttendanceCourses.length > 0 && (
        <div className="p-4 rounded-xl border border-amber-300 dark:border-amber-800 bg-amber-50/80 dark:bg-amber-950/30 flex items-start gap-3.5">
          <div className="p-2 rounded-lg bg-amber-100 dark:bg-amber-900/50 text-amber-600 dark:text-amber-400 shrink-0">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div className="flex-1 text-xs">
            <h4 className="font-bold text-amber-900 dark:text-amber-200">Attendance Warning & Condonation Alert</h4>
            <p className="text-amber-800 dark:text-amber-300 mt-0.5">
              You have {lowAttendanceCourses.length} course(s) with attendance below the mandatory 75% threshold ({lowAttendanceCourses.map(c => `${c.courseCode} (${c.percentage}%)`).join(', ')}).
              Per Autonomous Academic Regulations 2026, students with 65%-74% attendance must submit medical certificates to the Dean of Academics.
            </p>
          </div>
        </div>
      )}

      {/* Subject-Wise Attendance Breakdown */}
      <Card>
        <CardHeader
          title="Subject-Wise Attendance Registry"
          subtitle="Verified attendance records synchronized with the institutional academic registry"
        />
        <CardBody className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {attendanceRecords.map((rec) => {
              const isEligible = rec.percentage >= 75;
              return (
                <div
                  key={rec.id}
                  className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-3"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="font-mono text-xs font-bold text-[var(--color-primary)] px-2 py-0.5 rounded bg-[var(--color-primary)]/10">
                        {rec.courseCode}
                      </span>
                      <h4 className="text-xs font-bold text-[var(--color-foreground)] mt-1.5">{rec.courseName}</h4>
                    </div>
                    <div className="text-right">
                      <span className={`text-base font-extrabold font-mono ${isEligible ? 'text-emerald-600 dark:text-emerald-400' : 'text-amber-500'}`}>
                        {rec.percentage}%
                      </span>
                      <Badge variant={isEligible ? 'success' : 'warning'} size="sm" className="block mt-0.5">
                        {isEligible ? 'Eligible' : 'Warning'}
                      </Badge>
                    </div>
                  </div>

                  {/* Progress Bar */}
                  <div>
                    <div className="w-full bg-[var(--color-muted)] rounded-full h-2 overflow-hidden">
                      <div
                        className={`h-2 rounded-full transition-all duration-500 ${isEligible ? 'bg-emerald-500' : 'bg-amber-500'}`}
                        style={{ width: `${Math.min(rec.percentage, 100)}%` }}
                      />
                    </div>
                    <div className="flex justify-between items-center text-[10px] text-[var(--color-muted-foreground)] mt-1 font-mono">
                      <span>Attended: {rec.attendedClasses} / {rec.totalClasses} classes</span>
                      <span>Target: 75%</span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </CardBody>
      </Card>

      {/* Coursework & Assignments */}
      <Card>
        <CardHeader
          title="Coursework, Laboratory Deliverables & Assignments"
          subtitle="Track assignment deadlines, priority levels, and submission records"
          action={
            <div className="flex items-center gap-1.5 p-1 rounded-xl bg-[var(--color-muted)]/40 border border-[var(--color-border)]">
              <button
                onClick={() => setAssignmentFilter('ALL')}
                className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-all ${
                  assignmentFilter === 'ALL'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                All ({assignments.length})
              </button>
              <button
                onClick={() => setAssignmentFilter('HIGH_PRIORITY')}
                className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-all ${
                  assignmentFilter === 'HIGH_PRIORITY'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                High Priority
              </button>
              <button
                onClick={() => setAssignmentFilter('PENDING')}
                className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-all ${
                  assignmentFilter === 'PENDING'
                    ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                    : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
                }`}
              >
                Pending
              </button>
            </div>
          }
        />
        <CardBody className="p-4">
          {filteredAssignments.length === 0 ? (
            <p className="text-xs text-center text-[var(--color-muted-foreground)] py-6">No assignments match the selected filter.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filteredAssignments.map((asg) => (
                <div
                  key={asg.id}
                  className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50 space-y-2.5"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-bold text-[var(--color-primary)]">{asg.subjectCode}</span>
                    <div className="flex items-center gap-1.5">
                      <Badge variant={asg.priority === 'HIGH' ? 'danger' : 'neutral'} size="sm">
                        {asg.priority}
                      </Badge>
                      <Badge variant={asg.status === 'SUBMITTED' ? 'success' : 'warning'} size="sm">
                        {asg.status || 'PENDING'}
                      </Badge>
                    </div>
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-[var(--color-foreground)]">{asg.title}</h4>
                    <p className="text-[11px] text-[var(--color-muted-foreground)] mt-0.5">{asg.description}</p>
                  </div>
                  <div className="pt-2 border-t border-[var(--color-border)]/60 flex items-center justify-between text-[11px]">
                    <span className="text-[var(--color-muted-foreground)]">
                      Due: <strong className="text-[var(--color-foreground)]">{asg.dueDate}</strong>
                    </span>
                    <span className="font-semibold text-emerald-600 dark:text-emerald-400 font-mono">
                      Max: {asg.maxMarks} Marks
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
};

export default StudentAcademicsPage;
