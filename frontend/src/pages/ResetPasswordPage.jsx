import React, { useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import PasswordInput from '../components/ui/PasswordInput';
import Button from '../components/ui/Button';
import Card, { CardBody } from '../components/ui/Card';
import { Check, X, KeyRound, CheckCircle2 } from 'lucide-react';

export const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const { resetPassword } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const isLength = newPassword.length >= 8;
  const isMatch = newPassword && newPassword === confirmPassword;

  const handleReset = async (e) => {
    e.preventDefault();
    if (!token) {
      setError('Invalid or missing password reset token.');
      return;
    }
    if (!isLength) {
      setError('Password must be at least 8 characters long.');
      return;
    }
    if (!isMatch) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await resetPassword(token, newPassword, confirmPassword);
      setSuccess(true);
      addToast('Password updated successfully.', 'success');
    } catch (err) {
      const msg = err.response?.data?.message || 'Unable to reset password. Link may have expired.';
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
          Create a new password
        </h2>
        <p className="mt-1.5 text-xs text-[var(--color-muted-foreground)]">
          Please enter your secure new credentials
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <Card className="shadow-lg border-[var(--color-border)]">
          <CardBody className="p-6 sm:p-8 space-y-6">
            {!success ? (
              <form onSubmit={handleReset} className="space-y-4">
                {error && (
                  <div className="p-3.5 rounded-lg border border-red-200 dark:border-red-900 bg-red-50/80 dark:bg-red-950/40 text-red-700 dark:text-red-300 text-xs">
                    {error}
                  </div>
                )}

                <PasswordInput
                  label="New Password"
                  placeholder="Enter new password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  disabled={loading}
                  required
                />

                <PasswordInput
                  label="Confirm Password"
                  placeholder="Confirm new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  disabled={loading}
                  required
                />

                <div className="p-3 rounded-lg bg-[var(--color-background)] border border-[var(--color-border)] text-xs space-y-1">
                  <div className="flex items-center gap-1.5">
                    {isLength ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <X className="w-3.5 h-3.5 text-[var(--color-muted-foreground)]" />}
                    <span className={isLength ? 'text-emerald-700 dark:text-emerald-400' : 'text-[var(--color-muted-foreground)]'}>
                      At least 8 characters
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
                  icon={KeyRound}
                >
                  {loading ? 'Resetting password...' : 'Reset Password'}
                </Button>
              </form>
            ) : (
              <div className="text-center space-y-4">
                <div className="w-12 h-12 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h3 className="text-base font-semibold text-[var(--color-foreground)]">
                  Password updated successfully.
                </h3>
                <p className="text-xs text-[var(--color-muted-foreground)]">
                  Your credentials have been securely refreshed.
                </p>
                <Link to="/login" className="block pt-2">
                  <Button size="md" className="w-full">
                    Continue to Sign In
                  </Button>
                </Link>
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
};

export default ResetPasswordPage;
