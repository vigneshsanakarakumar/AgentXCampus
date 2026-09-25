import React from 'react';
import { AlertTriangle } from 'lucide-react';
import Button from './Button';

export const ErrorState = ({
  title = 'Unable to load data',
  message = 'A problem occurred while communicating with the campus service. Please try again.',
  onRetry,
}) => (
  <div className="flex flex-col items-center justify-center p-8 text-center rounded-xl border border-red-200 dark:border-red-900 bg-red-50/50 dark:bg-red-950/20 my-4">
    <div className="p-3 rounded-full bg-red-100 dark:bg-red-900/50 text-red-600 dark:text-red-400 mb-3">
      <AlertTriangle className="w-6 h-6" />
    </div>
    <h4 className="text-sm font-semibold text-[var(--color-foreground)]">{title}</h4>
    <p className="text-xs text-[var(--color-muted-foreground)] max-w-sm mt-1 mb-4">{message}</p>
    {onRetry && (
      <Button size="sm" variant="primary" onClick={onRetry}>
        Retry Request
      </Button>
    )}
  </div>
);

export default ErrorState;
