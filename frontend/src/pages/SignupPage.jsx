import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api from '../services/api';
import Input from '../components/ui/Input';
import PasswordInput from '../components/ui/PasswordInput';
import Button from '../components/ui/Button';
import Card, { CardBody } from '../components/ui/Card';
import { UserPlus, Check, X, GraduationCap, Building2, Briefcase, ShieldAlert, CheckCircle2 } from 'lucide-react';

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
  'Head of Department (HOD)',
  'Lab Instructor',
  'Visiting Faculty'
];

const SECTIONS = ['A', 'B', 'C', 'D'];

export const SignupPage = () => {
  const [mode, setMode] = useState('STUDENT'); // 'STUDENT' | 'STAFF'

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    department: 'Computer Science & Engineering',
    section: 'A',
    rollNumber: '',
  });

  const [staffData, setStaffData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    department: 'Computer Science & Engineering',
    designation: 'Assistant Professor',
    notes: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [staffSubmitted, setStaffSubmitted] = useState(false);

  const { signup } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleStaffChange = (e) => {
    setStaffData({ ...staffData, [e.target.name]: e.target.value });
    setError('');
  };

  const isPasswordValid = formData.password.length >= 6;
  const isMatch = formData.password && formData.password === formData.confirmPassword;

  const handleSignup = async (e) => {
    e.preventDefault();
    if (!formData.rollNumber.trim()) {
      setError('Student Roll Number is required.');
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

    setLoading(true);
    setError('');

    try {
      await signup({
        ...formData,
        rollNumber: formData.rollNumber.trim().toUpperCase(),
        section: formData.section.trim().toUpperCase(),
      });
      addToast('Student account registered successfully! Please sign in.', 'success');
      navigate('/login');
    } catch (err) {
      const msg = err.response?.data?.message || 'Unable to register student account. Please verify your details.';
      setError(msg);
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleStaffRequest = async (e) => {
    e.preventDefault();
    if (!staffData.firstName.trim() || !staffData.lastName.trim() || !staffData.email.trim()) {
      setError('Please provide your name and institutional email.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await api.post('/auth/staff-request', staffData);
      setStaffSubmitted(true);
      addToast('Faculty access request submitted for admin review.', 'success');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to submit faculty access request. Please try again.';
      setError(msg);
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <Link to="/" className="inline-flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-[var(--color-primary)] flex items-center justify-center text-white font-bold text-base shadow-sm">
            AX
          </div>
          <span className="font-bold text-xl text-[var(--color-foreground)] tracking-tight">
            AgentX <span className="text-[var(--color-primary)]">Campus</span>
          </span>
        </Link>
        <h2 className="mt-6 text-2xl font-bold tracking-tight text-[var(--color-foreground)]">
          {mode === 'STUDENT' ? 'Student Registration' : 'Faculty Access Request'}
        </h2>
        <p className="mt-1.5 text-xs text-[var(--color-muted-foreground)]">
          {mode === 'STUDENT'
            ? 'Register with your academic department, section, and roll number'
            : 'Submit a faculty access request. Account activation requires administrative approval.'}
        </p>

        {/* Mode Selector Tabs */}
        <div className="mt-5 inline-flex p-1 rounded-xl bg-[var(--color-muted)]/50 border border-[var(--color-border)] shadow-xs">
          <button
            type="button"
            onClick={() => { setMode('STUDENT'); setError(''); }}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              mode === 'STUDENT'
                ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
            }`}
          >
            Student Registration
          </button>
          <button
            type="button"
            onClick={() => { setMode('STAFF'); setError(''); }}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              mode === 'STAFF'
                ? 'bg-[var(--color-card)] text-[var(--color-foreground)] shadow-xs'
                : 'text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]'
            }`}
          >
            Faculty Access Request
          </button>
        </div>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-xl px-4 sm:px-0">
        <Card className="shadow-lg border-[var(--color-border)]">
          <CardBody className="p-6 sm:p-8 space-y-5">
            {error && (
              <div className="p-3.5 rounded-lg border border-red-200 dark:border-red-900 bg-red-50/80 dark:bg-red-950/40 text-red-700 dark:text-red-300 text-xs">
                {error}
              </div>
            )}

            {mode === 'STUDENT' ? (
              <form onSubmit={handleSignup} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    label="First Name"
                    name="firstName"
                    placeholder="Vignesh"
                    value={formData.firstName}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  <Input
                    label="Last Name"
                    name="lastName"
                    placeholder="R"
                    value={formData.lastName}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    label="Username"
                    name="username"
                    placeholder="vignesh24"
                    value={formData.username}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                  <Input
                    label="Institutional Email"
                    name="email"
                    type="email"
                    placeholder="vignesh@campus.edu"
                    value={formData.email}
                    onChange={handleChange}
                    disabled={loading}
                    required
                  />
                </div>

                {/* Academic Hierarchy Information */}
                <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-muted)]/20 space-y-3">
                  <div className="flex items-center gap-2 text-xs font-semibold text-[var(--color-foreground)]">
                    <GraduationCap className="w-4 h-4 text-[var(--color-primary)]" />
                    <span>Academic Details</span>
                  </div>

                  <div className="space-y-3">
                    <div>
                      <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                        Department
                      </label>
                      <select
                        name="department"
                        value={formData.department}
                        onChange={handleChange}
                        className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                      >
                        {DEPARTMENTS.map((dept) => (
                          <option key={dept} value={dept}>
                            {dept}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                          Section
                        </label>
                        <select
                          name="section"
                          value={formData.section}
                          onChange={handleChange}
                          className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                        >
                          {SECTIONS.map((sec) => (
                            <option key={sec} value={sec}>
                              Section {sec}
                            </option>
                          ))}
                        </select>
                      </div>

                      <Input
                        label="Roll Number"
                        name="rollNumber"
                        placeholder="e.g. 24CSE001"
                        value={formData.rollNumber}
                        onChange={handleChange}
                        disabled={loading}
                        required
                        helperText="Unique university roll number"
                      />
                    </div>
                  </div>
                </div>

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

                {/* Password checks */}
                <div className="p-3 rounded-lg bg-[var(--color-background)] border border-[var(--color-border)] text-xs space-y-1">
                  <div className="flex items-center gap-1.5">
                    {isPasswordValid ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <X className="w-3.5 h-3.5 text-[var(--color-muted-foreground)]" />}
                    <span className={isPasswordValid ? 'text-emerald-700 dark:text-emerald-400' : 'text-[var(--color-muted-foreground)]'}>
                      At least 6 characters
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    {isMatch ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <X className="w-3.5 h-3.5 text-[var(--color-muted-foreground)]" />}
                    <span className={isMatch ? 'text-emerald-700 dark:text-emerald-400' : 'text-[var(--color-muted-foreground)]'}>
                      Passwords match
                    </span>
                  </div>
                </div>

                <Button
                  type="submit"
                  size="md"
                  loading={loading}
                  className="w-full"
                  icon={UserPlus}
                >
                  {loading ? 'Creating student profile...' : 'Register as Student'}
                </Button>
              </form>
            ) : staffSubmitted ? (
              <div className="text-center py-6 space-y-4">
                <div className="w-14 h-14 bg-emerald-100 dark:bg-emerald-950/50 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
                  <CheckCircle2 className="w-8 h-8" />
                </div>
                <h3 className="text-lg font-bold text-[var(--color-foreground)]">
                  Request Submitted Successfully
                </h3>
                <p className="text-xs text-[var(--color-muted-foreground)] max-w-md mx-auto leading-relaxed">
                  Your request for faculty access has been forwarded to the Academic Administration.
                  Status: <span className="font-semibold text-amber-600 dark:text-amber-400">PENDING APPROVAL</span>.
                </p>
                <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-muted)]/20 text-left text-xs space-y-1.5">
                  <p><span className="font-semibold text-[var(--color-foreground)]">Name:</span> {staffData.firstName} {staffData.lastName}</p>
                  <p><span className="font-semibold text-[var(--color-foreground)]">Email:</span> {staffData.email}</p>
                  <p><span className="font-semibold text-[var(--color-foreground)]">Department:</span> {staffData.department}</p>
                  <p><span className="font-semibold text-[var(--color-foreground)]">Designation:</span> {staffData.designation}</p>
                </div>
                <p className="text-xs text-[var(--color-muted-foreground)]">
                  Once an administrator approves your request, you will receive an invitation link to set your password and activate your account.
                </p>
                <div className="pt-2">
                  <Button variant="outline" onClick={() => navigate('/login')}>
                    Return to Sign In
                  </Button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleStaffRequest} className="space-y-4">
                <div className="p-3.5 rounded-lg border border-amber-200 dark:border-amber-900/60 bg-amber-50/70 dark:bg-amber-950/30 text-amber-800 dark:text-amber-300 text-xs flex items-start gap-2.5">
                  <ShieldAlert className="w-4 h-4 shrink-0 mt-0.5" />
                  <div>
                    <span className="font-semibold">Faculty Governance Notice:</span> In accordance with institutional security policy, faculty accounts cannot self-register directly. Prospective faculty members submit an access request which is reviewed by the Campus Administrator before credentials are created.
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    label="First Name"
                    name="firstName"
                    placeholder="Prof. Rajesh"
                    value={staffData.firstName}
                    onChange={handleStaffChange}
                    disabled={loading}
                    required
                  />
                  <Input
                    label="Last Name"
                    name="lastName"
                    placeholder="Sharma"
                    value={staffData.lastName}
                    onChange={handleStaffChange}
                    disabled={loading}
                    required
                  />
                </div>

                <Input
                  label="Institutional Email"
                  name="email"
                  type="email"
                  placeholder="rajesh.sharma@campus.edu"
                  value={staffData.email}
                  onChange={handleStaffChange}
                  disabled={loading}
                  required
                  helperText="Your official campus or university email address"
                />

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                      Academic Department
                    </label>
                    <select
                      name="department"
                      value={staffData.department}
                      onChange={handleStaffChange}
                      className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                    >
                      {DEPARTMENTS.map((dept) => (
                        <option key={dept} value={dept}>
                          {dept}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                      Designation / Role
                    </label>
                    <select
                      name="designation"
                      value={staffData.designation}
                      onChange={handleStaffChange}
                      className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                    >
                      {DESIGNATIONS.map((desig) => (
                        <option key={desig} value={desig}>
                          {desig}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-[var(--color-foreground)] mb-1">
                    Notes / Remarks (Optional)
                  </label>
                  <textarea
                    name="notes"
                    rows={2}
                    placeholder="e.g. Appointed as Assistant Professor for Data Structures, joining Fall 2026."
                    value={staffData.notes}
                    onChange={handleStaffChange}
                    disabled={loading}
                    className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-card)] text-[var(--color-foreground)] text-xs py-2 px-3 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 focus:border-[var(--color-primary)]"
                  />
                </div>

                <Button
                  type="submit"
                  size="md"
                  loading={loading}
                  className="w-full"
                  icon={Briefcase}
                >
                  {loading ? 'Submitting request...' : 'Submit Faculty Request'}
                </Button>
              </form>
            )}

            <p className="text-center text-xs text-[var(--color-muted-foreground)] pt-2 border-t border-[var(--color-border)]">
              Already have an account?{' '}
              <Link to="/login" className="font-semibold text-[var(--color-primary)] hover:underline">
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

