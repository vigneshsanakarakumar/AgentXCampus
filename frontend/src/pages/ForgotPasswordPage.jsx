import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import Card, { CardBody } from '../components/ui/Card';
import { Mail, ArrowLeft, KeyRound } from 'lucide-react';

export const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [devResetToken, setDevResetToken] = useState(null);

  const { forgotPassword } = useAuth();
  const { addToast } = useToast();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim()) return;

    setLoading(true);
    try {
      const res = await forgotPassword(email.trim());
      setSubmitted(true);
      if (res.devResetToken) {
        setDevResetToken(res.devResetToken);
      }
      addToast('Reset instructions sent if account exists.', 'info');
    } catch (err) {
      setSubmitted(true);
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
          Forgot your password?
        </h2>
        <p className="mt-1.5 text-xs text-[var(--color-muted-foreground)]">
          Enter your registered email address and we'll send instructions to reset your password.
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <Card className="shadow-lg border-[var(--color-border)]">
          <CardBody className="p-6 sm:p-8 space-y-6">
            {!submitted ? (
              <form onSubmit={handleSubmit} className="space-y-4">
                <Input
                  label="Registered Email"
                  id="email"
                  type="email"
                  placeholder="vignesh@campus.edu"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={loading}
                  required
                />

                <Button
                  type="submit"
                  size="md"
                  loading={loading}
                  className="w-full"
                  icon={Mail}
                >
                  {loading ? 'Sending reset link...' : 'Send Reset Link'}
                </Button>
              </form>
            ) : (
              <div className="text-center space-y-4">
                <div className="p-4 rounded-xl border border-emerald-200 dark:border-emerald-800 bg-emerald-50 dark:bg-emerald-950/40 text-emerald-800 dark:text-emerald-200 text-xs">
                  If an account exists for this email, password reset instructions have been sent.
                </div>

                {devResetToken && (
                  <div className="p-3.5 rounded-lg border border-[var(--color-primary)]/40 bg-[var(--color-primary)]/10 text-left text-xs space-y-2">
                    <p className="font-semibold text-[var(--color-primary)] flex items-center gap-1.5">
                      <KeyRound className="w-3.5 h-3.5" />
                      <span>Development Mode Reset Link:</span>
                    </p>
                    <Link
                      to={`/reset-password?token=${devResetToken}`}
                      className="font-mono text-[11px] underline break-all text-[var(--color-foreground)]"
                    >
                      /reset-password?token={devResetToken}
                    </Link>
                  </div>
                )}
              </div>
            )}

            <div className="text-center pt-2 border-t border-[var(--color-border)]">
              <Link
                to="/login"
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-[var(--color-primary)] hover:underline"
              >
                <ArrowLeft className="w-3.5 h-3.5" />
                <span>Back to Sign In</span>
              </Link>
            </div>
          </CardBody>
        </Card>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
