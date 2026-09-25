import React from 'react';

export const Badge = ({ children, variant = 'neutral', size = 'sm', className = '' }) => {
  const variants = {
    neutral: 'bg-[var(--color-border)]/40 text-[var(--color-foreground)] border-[var(--color-border)]',
    primary: 'bg-[var(--color-primary)]/10 text-[var(--color-primary)] border-[var(--color-primary)]/20',
    success: 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/60 dark:text-emerald-400 dark:border-emerald-800',
    warning: 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/60 dark:text-amber-400 dark:border-amber-800',
    danger: 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/60 dark:text-red-400 dark:border-red-800',
    info: 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/60 dark:text-blue-400 dark:border-blue-800',
  };

  const sizes = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-sm px-2.5 py-1',
  };

  return (
    <span className={`inline-flex items-center font-medium rounded-full border ${variants[variant] || variants.neutral} ${sizes[size]} ${className}`}>
      {children}
    </span>
  );
};

export default Badge;
