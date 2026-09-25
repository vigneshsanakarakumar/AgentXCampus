import React, { forwardRef } from 'react';

export const Input = forwardRef(({
  label,
  error,
  helperText,
  icon: Icon,
  className = '',
  id,
  ...props
}, ref) => {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full space-y-1.5">
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-[var(--color-foreground)]">
          {label}
        </label>
      )}
      <div className="relative rounded-lg shadow-sm">
        {Icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[var(--color-muted-foreground)]">
            <Icon className="w-4 h-4" />
          </div>
        )}
        <input
          ref={ref}
          id={inputId}
          className={`w-full rounded-lg border bg-[var(--color-card)] text-[var(--color-foreground)] placeholder-[var(--color-muted-foreground)] text-sm transition-colors py-2.5 ${
            Icon ? 'pl-9' : 'pl-3.5'
          } pr-3.5 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 ${
            error
              ? 'border-red-500 focus:border-red-500'
              : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'
          } ${className}`}
          {...props}
        />
      </div>
      {error && (
        <p className="text-xs text-red-600 dark:text-red-400 mt-1">{error}</p>
      )}
      {helperText && !error && (
        <p className="text-xs text-[var(--color-muted-foreground)] mt-1">{helperText}</p>
      )}
    </div>
  );
});

Input.displayName = 'Input';
export default Input;
