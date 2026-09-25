import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Input from '../components/ui/Input';
import PasswordInput from '../components/ui/PasswordInput';
import Button from '../components/ui/Button';
import Card, { CardBody } from '../components/ui/Card';
import { LogIn, Clock, ShieldAlert } from 'lucide-react';

export const LoginPage = () => {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isPendingApproval, setIsPendingApproval] = useState(false);

  const { login } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!identifier.trim() || !password) {
      setError('Please provide your username or email and password.');
      return;
    }

    setLoading(true);
    setError('');
    setIsPendingApproval(false);

    try {
      const user = await login(identifier, password, rememberMe);
      addToast(`Welcome back, ${user.firstName}!`, 'success');

      // Post-login redirect strictly determined by database role
      if (user.role === 'STUDENT') navigate('/dashboard');
      else if (user.role === 'FACULTY') navigate('/faculty/dashboard');
      else if (user.role === 'STAFF') navigate('/staff/dashboard');
      else if (user.role === 'ADMIN') navigate('/admin/dashboard');
      else navigate('/');
    } catch (err) {
      const status = err.response?.status;
      const statusText = err.response?.data?.status;
      const msg = err.response?.data?.message || 'Incorrect username/email or password. Please try again.';

      if (status === 403 || statusText === 'PENDING_APPROVAL' || msg.toLowerCase().includes('pending approval')) {
        setIsPendingApproval(true);
      }
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
          Welcome back
        </h2>
        <p className="mt-1.5 text-xs text-[var(--color-muted-foreground)]">
          Sign in to your authorized campus workspace
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <Card className="shadow-lg border-[var(--color-border)]">
          <CardBody className="p-6 sm:p-8 space-y-6">
            {isPendingApproval ? (
              <div className="p-4 rounded-xl border border-amber-300 dark:border-amber-800 bg-amber-50 dark:bg-amber-950/40 text-amber-800 dark:text-amber-200 text-xs space-y-2">
                <div className="flex items-center gap-2 font-bold">
                  <Clock className="w-4 h-4 text-amber-600 dark:text-amber-400 shrink-0" />
                  <span>Account Pending Administrative Approval</span>
                </div>
                <p className="leading-relaxed">
                  Your faculty/staff access request has been received and is currently under review by Campus Administration.
                </p>
                <p className="text-[11px] text-amber-700 dark:text-amber-300">
                  Once approved, an activation link will be issued allowing you to set a password and activate your account.
                </p>
              </div>
            ) : error ? (
              <div className="p-3.5 rounded-lg border border-red-200 dark:border-red-900 bg-red-50/80 dark:bg-red-950/40 text-red-700 dark:text-red-300 text-xs">
                {error}
              </div>
            ) : null}

            <form onSubmit={handleLogin} className="space-y-4">
              <Input
                label="Username or Email"
                id="identifier"
                type="text"
                placeholder="Enter your username or campus email"
                value={identifier}
                onChange={(e) => {
                  setIdentifier(e.target.value);
                  setError('');
                }}
                disabled={loading}
                required
              />

              <PasswordInput
                label="Password"
                id="password"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setError('');
                }}
                disabled={loading}
                required
              />

              <div className="flex items-center justify-between text-xs">
                <label className="flex items-center gap-2 cursor-pointer text-[var(--color-muted-foreground)]">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="rounded border-[var(--color-border)] text-[var(--color-primary)] focus:ring-[var(--color-primary)]"
                  />
                  <span>Remember me</span>
                </label>

                <Link
                  to="/forgot-password"
                  className="font-medium text-[var(--color-primary)] hover:underline"
                >
                  Forgot password?
                </Link>
              </div>

              <Button
                type="submit"
                size="md"
                loading={loading}
                className="w-full"
                icon={LogIn}
              >
                {loading ? 'Signing in...' : 'Sign In'}
              </Button>
            </form>

            <p className="text-center text-xs text-[var(--color-muted-foreground)] pt-2 border-t border-[var(--color-border)]">
              Don't have an account?{' '}
              <Link to="/signup" className="font-semibold text-[var(--color-primary)] hover:underline">
                Register as Student
              </Link>
            </p>
          </CardBody>
        </Card>
      </div>
    </div>
  );
};

export default LoginPage;
