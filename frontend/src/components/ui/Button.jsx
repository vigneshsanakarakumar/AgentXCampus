import React from 'react';
import { Loader2 } from 'lucide-react';

export const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  className = '',
  type = 'button',
  icon: Icon,
  ...props
}) => {
  const baseClasses = 'inline-flex items-center justify-center font-medium rounded-lg transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none active:scale-[0.98]';

  const variants = {
    primary: 'bg-[var(--color-primary)] text-white hover:opacity-90 focus:ring-[var(--color-primary)] shadow-sm',
    secondary: 'bg-[var(--color-card)] text-[var(--color-foreground)] border border-[var(--color-border)] hover:bg-[var(--color-border)]/50',
    outline: 'border border-[var(--color-border)] text-[var(--color-foreground)] hover:bg-[var(--color-primary)]/10 hover:border-[var(--color-primary)]/40',
    ghost: 'text-[var(--color-foreground)] hover:bg-[var(--color-border)]/50',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
  };

  const sizes = {
    sm: 'text-xs px-2.5 py-1.5 gap-1.5',
    md: 'text-sm px-4 py-2.5 gap-2',
    lg: 'text-base px-5 py-3 gap-2.5',
  };

  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`${baseClasses} ${variants[variant] || variants.primary} ${sizes[size] || sizes.md} ${className}`}
      {...props}
    >
      {loading ? (
        <Loader2 className="w-4 h-4 animate-spin shrink-0" />
      ) : Icon ? (
        <Icon className="w-4 h-4 shrink-0" />
      ) : null}
      <span>{children}</span>
    </button>
  );
};

export default Button;
