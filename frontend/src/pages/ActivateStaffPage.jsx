import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { useToast } from '../context/ToastContext';
import Input from '../components/ui/Input';
import PasswordInput from '../components/ui/PasswordInput';
import Button from '../components/ui/Button';
import Card, { CardHeader, CardBody } from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorState from '../components/ui/ErrorState';
import api from '../services/api';
import { CheckCircle2, ShieldCheck, UserCheck, GraduationCap, Building2, KeyRound } from 'lucide-react';

export const ActivateStaffPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [inviteData, setInviteData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [activated, setActivated] = useState(false);

  useEffect(() => {
    if (!token) {
      setError('Invitation token is missing. Please use the complete link provided in your invitation.');
      setLoading(false);
      return;
    }

    const verifyToken = async () => {
      try {
        const res = await api.get('/auth/verify-staff-invite?token=' + encodeURIComponent(token));
        setInviteData(res.data);
        if (res.data.email) {
          const suggestedUsername = res.data.email.split('@')[0].toLowerCase().replace(/[^a-z0-9]/g, '');
          setUsername(suggestedUsername);
        }
      } catch (err) {
        const msg = err.response?.data?.message || 'This invitation link is invalid or has expired.';
        setError(msg);
      } finally {
        setLoading(false);
      }
    };

    verifyToken();
  }, [token]);

  const handleActivate = async (e) => {
    e.preventDefault();
    if (!username.trim()) {
      addToast('Please choose a username.', 'error');
      return;
    }
    if (password.length < 6) {
      addToast('Password must be at least 6 characters long.', 'error');
      return;
    }
    if (password !== confirmPassword) {
      addToast('Passwords do not match.', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const res = await api.post('/auth/activate-staff', {
        token,
        username: username.trim(),
        password,
      });
      addToast('Faculty account activated successfully! You can now sign in.', 'success');
      setActivated(true);
      setTimeout(() => navigate('/login'), 2500);
    } catch (err) {
      const msg = err.response?.data?.message || 'Unable to activate account. Please check your details.';
      addToast(msg, 'error');
    } finally {
      setSubmitting(false);
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
        <h2 className="mt-4 text-xl font-bold text-[var(--color-foreground)]">
          Faculty Account Activation
        </h2>
        <p className="mt-1 text-xs text-[var(--color-muted-foreground)]">
          Activate your verified institutional faculty credentials
        </p>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-md">
        <Card>
          <CardBody className="p-6">
            {loading ? (
              <div className="py-8 flex justify-center items-center">
                <LoadingSpinner size="md" message="Validating invitation token..." />
              </div>
            ) : error ? (
              <div className="space-y-4">
                <ErrorState
                  title="Invalid Invitation Link"
                  message={error}
                />
                <div className="text-center">
                  <Link to="/login" className="text-xs text-[var(--color-primary)] hover:underline font-semibold">
                    Return to Login
                  </Link>
                </div>
              </div>
            ) : activated ? (
              <div className="text-center py-6 space-y-3">
                <div className="w-12 h-12 rounded-full bg-emerald-100 dark:bg-emerald-900/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center mx-auto">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h3 className="text-sm font-bold text-[var(--color-foreground)]">
                  Account Activated!
                </h3>
                <p className="text-xs text-[var(--color-muted-foreground)]">
                  Your faculty account is now active. Redirecting you to the sign-in screen...
                </p>
                <Link to="/login" className="inline-block text-xs text-[var(--color-primary)] font-semibold hover:underline pt-2">
                  Click here if not redirected automatically
                </Link>
              </div>
            ) : (
              <div className="space-y-5">
                {/* Invitee Details Badge */}
                <div className="p-3.5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/60 space-y-1.5 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-[var(--color-foreground)]">
                      {inviteData?.firstName} {inviteData?.lastName}
                    </span>
                    <Badge variant="primary" size="sm">FACULTY</Badge>
                  </div>
                  <p className="text-[11px] text-[var(--color-muted-foreground)] flex items-center gap-1.5">
                    <GraduationCap className="w-3.5 h-3.5 text-[var(--color-primary)]" />
                    <span>{inviteData?.designation} • {inviteData?.department}</span>
                  </p>
                  <p className="text-[11px] text-[var(--color-muted-foreground)] font-mono">
                    {inviteData?.email}
                  </p>
                </div>

                {/* Activation Form */}
                <form onSubmit={handleActivate} className="space-y-3.5">
                  <Input
                    label="Desired Username"
                    placeholder="e.g. sharma.v"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                  />

                  <PasswordInput
                    label="New Password"
                    placeholder="At least 6 characters"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />

                  <PasswordInput
                    label="Confirm Password"
                    placeholder="Re-enter password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                  />

                  <Button
                    type="submit"
                    className="w-full mt-2"
                    loading={submitting}
                  >
                    Set Password & Activate Faculty Account
                  </Button>
                </form>
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
};

export default ActivateStaffPage;
