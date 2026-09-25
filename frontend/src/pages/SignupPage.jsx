import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api from '../services/api';
import Input from '../components/ui/Input';
import PasswordInput from '../components/ui/PasswordInput';
import Button from '../components/ui/Button';
import Card, { CardBody } from '../components/ui/Card';
import {
  UserPlus,
  Check,
  X,
  GraduationCap,
  Building2,
  Briefcase,
  CheckCircle2,
  Clock,
  BookOpen,
  ArrowRight,
  ShieldCheck,
  Info
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

const DESIGNATIONS = [
  'Assistant Professor',
  'Associate Professor',
  'Professor',
  'Assistant Professor (Subject Staff)',
  'Subject Staff / Lecturer',
  'Visiting Faculty',
  'Lab Instructor'
];

const SECTIONS = ['A', 'B', 'C', 'D'];
const YEARS = [
  { value: 1, label: '1st Year' },
  { value: 2, label: '2nd Year' },
  { value: 3, label: '3rd Year' },
  { value: 4, label: '4th Year' }
];

export const SignupPage = () => {
  // Role selection: 'STUDENT' or 'FACULTY'
  const [role, setRole] = useState('STUDENT');

  // Unified form data
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    department: 'Computer Science & Engineering',
    // Student specific
    rollNumber: '',
    section: 'A',
    year: 1,
    semester: 1,
    // Faculty specific
    designation: 'Assistant Professor',
    specialization: '',
    cabinNumber: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [submittedData, setSubmittedData] = useState(null);

  const { addToast } = useToast();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError('');
  };

  const isPasswordValid = formData.password.length >= 6;
  const isMatch = formData.password && formData.password === formData.confirmPassword;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Common validations
    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      setError('Please provide your full first and last name.');
      return;
    }
    if (!formData.username.trim() || !formData.email.trim()) {
      setError('Please provide a unique username and institutional email.');
      return;
    }
    if (!isPasswordValid) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (!isMatch) {
      setError('Passwords do not match.');
      return;
    }

    // Role-specific validations
    if (role === 'STUDENT') {
      if (!formData.rollNumber.trim()) {
        setError('University Roll Number is required for student registration.');
        return;
      }
    } else if (role === 'FACULTY') {
      if (!formData.designation) {
        setError('Please select your academic faculty designation.');
        return;
      }
    }

    setLoading(true);

    try {
      const payload = {
        role,
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        username: formData.username.trim().toLowerCase(),
        email: formData.email.trim().toLowerCase(),
        password: formData.password,
        confirmPassword: formData.confirmPassword,
        department: formData.department,
        ...(role === 'STUDENT'
          ? {
              rollNumber: formData.rollNumber.trim().toUpperCase(),
              section: formData.section.trim().toUpperCase(),
              year: Number(formData.year) || 1,
              semester: Number(formData.semester) || 1
            }
          : {
              designation: formData.designation,
              specialization: formData.specialization?.trim() || 'General Subject Faculty',
              cabinNumber: formData.cabinNumber?.trim() || 'Staff Room'
            })
      };

      const response = await api.post('/auth/register-request', payload);
      setSubmittedData({ ...payload, ...response.data });
      addToast(
        `Registration submitted! Awaiting ${formData.department} HOD verification and approval.`,
        'success'
      );
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        'Unable to submit registration request. Please verify your details.';
      setError(msg);
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setSubmittedData(null);
    setFormData({
      firstName: '',
      lastName: '',
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      department: 'Computer Science & Engineering',
      rollNumber: '',
      section: 'A',
      year: 1,
      semester: 1,
      designation: 'Assistant Professor',
      specialization: '',
      cabinNumber: ''
    });
    setError('');
  };

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-xl text-center">
        <Link to="/" className="inline-flex items-center gap-2.5">
          <div className="w-10 h-10 rounded-xl bg-[var(--color-primary)] flex items-center justify-center text-white font-bold text-lg shadow-sm">
            AX
          </div>
          <span className="font-bold text-2xl text-[var(--color-foreground)] tracking-tight">
            AgentX <span className="text-[var(--color-primary)]">Campus</span>
          </span>
        </Link>
        <h2 className="mt-5 text-2xl font-bold tracking-tight text-[var(--color-foreground)]">
          {submittedData
            ? 'Registration Submitted'
            : role === 'STUDENT'
            ? 'Student Account Registration'
            : 'Faculty Account Registration'}
        </h2>
        <p className="mt-1 text-xs text-[var(--color-muted-foreground)]">
          {submittedData
            ? 'Your application has been routed to your Department Head of Department (HOD) for review.'
            : 'All registrations are verified and approved by the respective Department HOD before activation.'}
        </p>

        {/* 2-Option Selector Toggle */}
        {!submittedData && (
          <div className="mt-5 inline-flex p-1 rounded-xl bg-[var(--color-muted)]/50 border border-[var(--color-border)] shadow-xs">
            <button
              type="button"
              onClick={() => {
                setRole('STUDENT');
                setError('');
              }}
              className={`flex items-center gap-2 px-5 py-2 rounded-lg text-xs font-semibold transition-all ${
                role === 'STUDENT'
                  ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs border border-[var(--color-border)]'
                  : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
              }`}
            >
              <GraduationCap className="w-4 h-4 text-[var(--color-primary)]" />
              Register as Student
            </button>
            <button
              type="button"
              onClick={() => {
                setRole('FACULTY');
                setError('');
              }}
              className={`flex items-center gap-2 px-5 py-2 rounded-lg text-xs font-semibold transition-all ${
                role === 'FACULTY'
                  ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs border border-[var(--color-border)]'
                  : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
              }`}
            >
              <Briefcase className="w-4 h-4 text-purple-600 dark:text-purple-400" />
              Register as Faculty
            </button>
          </div>
        )}
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-xl px-4 sm:px-0">
        <Card className="shadow-lg border-[var(--color-border)]">
          <CardBody className="p-6 sm:p-8 space-y-5">
            {error && (
              <div className="p-3.5 rounded-lg border border-red-200 dark:border-red-900 bg-red-50/80 dark:bg-red-950/40 text-red-700 dark:text-red-300 text-xs">
                {error}
              </div>
            )}

            {/* WAITING / CONFIRMATION SCREEN */}
            {submittedData ? (
              <div className="text-center py-4 space-y-5">
                <div className="w-16 h-16 bg-amber-100 dark:bg-amber-950/60 text-amber-600 dark:text-amber-400 rounded-full flex items-center justify-center mx-auto ring-8 ring-amber-50 dark:ring-amber-950/20">
                  <Clock className="w-9 h-9 animate-pulse" />
                </div>

                <div className="space-y-1">
                  <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-700 dark:text-amber-300 border border-amber-500/20">
                    <Clock className="w-3.5 h-3.5" />
                    STATUS: PENDING HOD APPROVAL
                  </div>
                  <h3 className="text-xl font-bold text-[var(--color-foreground)] mt-2">
                    Application Sent to Department HOD
                  </h3>
                  <p className="text-xs text-[var(--color-muted-foreground)] max-w-md mx-auto leading-relaxed">
                    Thank you, <span className="font-semibold text-[var(--color-foreground)]">{submittedData.firstName} {submittedData.lastName}</span>! Your registration request as a <span className="font-semibold text-[var(--color-foreground)]">{submittedData.role === 'STUDENT' ? 'Student' : 'Faculty Member'}</span> has been successfully sent to the Head of Department for review.
                  </p>
                </div>

                {/* Submitted Details Review Card */}
                <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-muted)]/20 text-left text-xs space-y-2.5">
                  <div className="flex items-center justify-between pb-2 border-b border-[var(--color-border)]/60">
                    <span className="font-bold text-[var(--color-foreground)] flex items-center gap-1.5">
                      <ShieldCheck className="w-4 h-4 text-emerald-500" />
                      Submitted Application Details
                    </span>
                    <span className="text-[11px] font-mono text-[var(--color-muted-foreground)]">
                      Ref #{submittedData.requestId || 'REQ-NEW'}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-2 text-[11px]">
                    <div>
                      <span className="text-[var(--color-muted-foreground)] block">Username:</span>
                      <span className="font-semibold text-[var(--color-foreground)]">@{submittedData.username}</span>
                    </div>
                    <div>
                      <span className="text-[var(--color-muted-foreground)] block">Official Email:</span>
                      <span className="font-semibold text-[var(--color-foreground)]">{submittedData.email}</span>
                    </div>
                    <div className="col-span-2">
                      <span className="text-[var(--color-muted-foreground)] block">Academic Department:</span>
                      <span className="font-semibold text-[var(--color-primary)]">{submittedData.department}</span>
                    </div>

                    {submittedData.role === 'STUDENT' ? (
                      <>
                        <div>
                          <span className="text-[var(--color-muted-foreground)] block">Roll Number:</span>
                          <span className="font-bold text-blue-600 dark:text-blue-400">{submittedData.rollNumber}</span>
                        </div>
                        <div>
                          <span className="text-[var(--color-muted-foreground)] block">Section & Year:</span>
                          <span className="font-semibold text-[var(--color-foreground)]">
                            Section {submittedData.section} (Year {submittedData.year}, Sem {submittedData.semester})
                          </span>
                        </div>
                      </>
                    ) : (
                      <>
                        <div>
                          <span className="text-[var(--color-muted-foreground)] block">Designation:</span>
                          <span className="font-bold text-purple-600 dark:text-purple-400">{submittedData.designation}</span>
                        </div>
                        <div>
                          <span className="text-[var(--color-muted-foreground)] block">Cabin / Office:</span>
                          <span className="font-semibold text-[var(--color-foreground)]">{submittedData.cabinNumber || 'Staff Room'}</span>
                        </div>
                        <div className="col-span-2">
                          <span className="text-[var(--color-muted-foreground)] block">Specialization / Subjects:</span>
                          <span className="font-semibold text-[var(--color-foreground)]">{submittedData.specialization || 'General Subject Staff'}</span>
                        </div>
                      </>
                    )}
                  </div>
                </div>

                {/* Institutional Workflow Instructions */}
                <div className="p-3.5 rounded-lg border border-blue-500/20 bg-blue-500/5 text-left text-xs space-y-1.5">
                  <div className="font-semibold text-blue-700 dark:text-blue-300 flex items-center gap-1.5">
                    <Info className="w-3.5 h-3.5" /> What happens next?
                  </div>
                  <p className="text-[11px] text-[var(--color-muted-foreground)] leading-relaxed">
                    Your Department Head of Department (HOD) will inspect your academic credentials and approve your account. While awaiting approval, your account remains safely locked. Once approved, you can immediately sign in with your credentials.
                  </p>
                </div>

                <div className="flex flex-col sm:flex-row gap-3 pt-2">
                  <Button
                    variant="outline"
                    className="flex-1"
                    onClick={handleReset}
                  >
                    Register Another Account
                  </Button>
                  <Button
                    variant="primary"
                    className="flex-1"
                    icon={ArrowRight}
                    onClick={() => navigate('/login')}
                  >
                    Proceed to Sign In
                  </Button>
                </div>
              </div>
            ) : (
              /* REGISTRATION FORM */
              <form onSubmit={handleSubmit} className="space-y-4">
                {/* Department Notification Banner */}
                <div className="p-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-muted)]/30 text-xs flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Building2 className="w-4 h-4 text-[var(--color-primary)]" />
                    <span className="text-[var(--color-muted-foreground)]">
                      Routing target:{' '}
                      <strong className="text-[var(--color-foreground)]">{formData.department} HOD</strong>
                    </span>
                  </div>
                  <span className="text-[10px] px-2 py-0.5 rounded font-semibold bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
                    Direct HOD Verification
                  </span>
                </div>

                {/* Name fields */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    label="First Name"
                    name="firstName"
                    placeholder={role === 'STUDENT' ? 'e.g. Vignesh' : 'e.g. Dr. Rajesh'}
                    value={formData.firstName}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  <Input
                    label="Last Name"
                    name="lastName"
                    placeholder={role === 'STUDENT' ? 'e.g. Kumar' : 'e.g. Sharma'}
                    value={formData.lastName}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                </div>

                {/* Username & Email */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    label="Username"
                    name="username"
                    placeholder={role === 'STUDENT' ? 'vignesh24' : 'rajesh.sharma'}
                    value={formData.username}
                    onChange={handleChange}
                    disabled={loading}
                    required
                    helperText="Unique username used to sign in"
                  />
                  <Input
                    label="Institutional Email"
                    name="email"
                    type="email"
                    placeholder={role === 'STUDENT' ? 'vignesh@student.campus.edu' : 'r.sharma@faculty.campus.edu'}
                    value={formData.email}
                    onChange={handleChange}
                    disabled={loading}
                    required
                    helperText="Campus-issued official email"
                  />
                </div>

                {/* Academic Department Selector */}
                <div>
                  <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                    Academic Department
                  </label>
                  <select
                    name="department"
                    value={formData.department}
                    onChange={handleChange}
                    disabled={loading}
                    className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                  >
                    {DEPARTMENTS.map((dept) => (
                      <option key={dept} value={dept}>
                        {dept}
                      </option>
                    ))}
                  </select>
                </div>

                {/* ROLE-SPECIFIC FIELDS */}
                {role === 'STUDENT' ? (
                  <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-muted)]/20 space-y-3">
                    <div className="flex items-center gap-2 text-xs font-semibold text-[var(--color-foreground)]">
                      <GraduationCap className="w-4 h-4 text-[var(--color-primary)]" />
                      <span>Student Academic Details (Verified by HOD)</span>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                      <div>
                        <Input
                          label="Roll Number"
                          name="rollNumber"
                          placeholder="e.g. 717824P388"
                          value={formData.rollNumber}
                          onChange={handleChange}
                          disabled={loading}
                          required
                          helperText="University Roll / Reg No"
                        />
                      </div>

                      <div>
                        <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                          Section
                        </label>
                        <select
                          name="section"
                          value={formData.section}
                          onChange={handleChange}
                          disabled={loading}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                        >
                          {SECTIONS.map((sec) => (
                            <option key={sec} value={sec}>
                              Section {sec}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                          Academic Year
                        </label>
                        <select
                          name="year"
                          value={formData.year}
                          onChange={handleChange}
                          disabled={loading}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                        >
                          {YEARS.map((y) => (
                            <option key={y.value} value={y.value}>
                              {y.label}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-muted)]/20 space-y-3">
                    <div className="flex items-center gap-2 text-xs font-semibold text-[var(--color-foreground)]">
                      <Briefcase className="w-4 h-4 text-purple-600 dark:text-purple-400" />
                      <span>Faculty Appointment Details (Verified by HOD)</span>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                          Designation / Role
                        </label>
                        <select
                          name="designation"
                          value={formData.designation}
                          onChange={handleChange}
                          disabled={loading}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                        >
                          {DESIGNATIONS.map((desig) => (
                            <option key={desig} value={desig}>
                              {desig}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <Input
                          label="Cabin / Office Room"
                          name="cabinNumber"
                          placeholder="e.g. Block A - Room 204"
                          value={formData.cabinNumber}
                          onChange={handleChange}
                          disabled={loading}
                          helperText="Faculty desk or room location"
                        />
                      </div>
                    </div>

                    <div>
                      <Input
                        label="Specialization / Subject Taught"
                        name="specialization"
                        placeholder="e.g. Cloud Computing, Distributed Systems, Discrete Mathematics"
                        value={formData.specialization}
                        onChange={handleChange}
                        disabled={loading}
                        helperText="Subject expertise (Subject staff who are not class tutors)"
                      />
                    </div>
                  </div>
                )}

                {/* Password & Confirmation */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <PasswordInput
                    label="Password"
                    name="password"
                    placeholder="At least 6 characters"
                    value={formData.password}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />

                  <PasswordInput
                    label="Confirm Password"
                    name="confirmPassword"
                    placeholder="Re-enter password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                </div>

                {/* Password validity indicators */}
                <div className="p-3 rounded-lg bg-[var(--color-background)] border border-[var(--color-border)] text-xs space-y-1">
                  <div className="flex items-center gap-1.5">
                    {isPasswordValid ? (
                      <Check className="w-3.5 h-3.5 text-emerald-500" />
                    ) : (
                      <X className="w-3.5 h-3.5 text-[var(--color-muted-foreground)]" />
                    )}
                    <span
                      className={
                        isPasswordValid
                          ? 'text-emerald-700 dark:text-emerald-400'
                          : 'text-[var(--color-muted-foreground)]'
                      }
                    >
                      At least 6 characters
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    {isMatch ? (
                      <Check className="w-3.5 h-3.5 text-emerald-500" />
                    ) : (
                      <X className="w-3.5 h-3.5 text-[var(--color-muted-foreground)]" />
                    )}
                    <span
                      className={
                        isMatch
                          ? 'text-emerald-700 dark:text-emerald-400'
                          : 'text-[var(--color-muted-foreground)]'
                      }
                    >
                      Passwords match
                    </span>
                  </div>
                </div>

                <Button
                  type="submit"
                  size="md"
                  loading={loading}
                  className="w-full"
                  icon={role === 'STUDENT' ? GraduationCap : Briefcase}
                >
                  {loading
                    ? 'Submitting to HOD...'
                    : role === 'STUDENT'
                    ? 'Submit Student Registration for HOD Approval'
                    : 'Submit Faculty Registration for HOD Approval'}
                </Button>
              </form>
            )}

            <p className="text-center text-xs text-[var(--color-muted-foreground)] pt-2 border-t border-[var(--color-border)]">
              Already have an approved account?{' '}
              <Link
                to="/login"
                className="font-semibold text-[var(--color-primary)] hover:underline"
              >
                Sign in
              </Link>
            </p>
          </CardBody>
        </Card>
      </div>
    </div>
  );
};

export default SignupPage;
