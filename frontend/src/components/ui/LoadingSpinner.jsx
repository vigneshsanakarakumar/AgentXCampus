import React from 'react';
import { Loader2 } from 'lucide-react';

export const LoadingSpinner = ({ size = 'md', message = '', className = '' }) => {
  const sizes = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-10 h-10',
  };

  return (
    <div className={`flex flex-col items-center justify-center gap-3 p-4 ${className}`}>
      <Loader2 className={`${sizes[size]} animate-spin text-[var(--color-primary)]`} />
      {message && <p className="text-xs text-[var(--color-muted-foreground)] animate-pulse">{message}</p>}
    </div>
  );
};

export default LoadingSpinner;
