import React, { useState, forwardRef } from 'react';
import { Eye, EyeOff, Lock } from 'lucide-react';

export const PasswordInput = forwardRef(({
  label,
  error,
  helperText,
  className = '',
  id,
  ...props
}, ref) => {
  const [showPassword, setShowPassword] = useState(false);
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full space-y-1.5">
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-[var(--color-foreground)]">
          {label}
        </label>
      )}
      <div className="relative rounded-lg shadow-sm">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[var(--color-muted-foreground)]">
          <Lock className="w-4 h-4" />
        </div>
        <input
          ref={ref}
          id={inputId}
          type={showPassword ? 'text' : 'password'}
          className={`w-full rounded-lg border bg-[var(--color-card)] text-[var(--color-foreground)] placeholder-[var(--color-muted-foreground)] text-sm transition-colors py-2.5 pl-9 pr-10 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/40 ${
            error
              ? 'border-red-500 focus:border-red-500'
              : 'border-[var(--color-border)] focus:border-[var(--color-primary)]'
          } ${className}`}
          {...props}
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute inset-y-0 right-0 pr-3 flex items-center text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] transition-colors"
          tabIndex={-1}
        >
          {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
        </button>
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

PasswordInput.displayName = 'PasswordInput';
export default PasswordInput;
