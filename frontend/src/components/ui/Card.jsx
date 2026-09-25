import React from 'react';

export const Card = ({ children, className = '', hover = false, ...props }) => (
  <div
    className={`bg-[var(--color-card)] border border-[var(--color-border)] rounded-xl shadow-sm overflow-hidden ${
      hover ? 'hover:border-[var(--color-primary)]/40 hover:shadow-md transition-all duration-200' : ''
    } ${className}`}
    {...props}
  >
    {children}
  </div>
);

export const CardHeader = ({ title, subtitle, action, className = '' }) => (
  <div className={`p-5 border-b border-[var(--color-border)] flex items-center justify-between ${className}`}>
    <div>
      <h3 className="font-semibold text-base text-[var(--color-foreground)]">{title}</h3>
      {subtitle && <p className="text-xs text-[var(--color-muted-foreground)] mt-0.5">{subtitle}</p>}
    </div>
    {action && <div>{action}</div>}
  </div>
);

export const CardBody = ({ children, className = '' }) => (
  <div className={`p-5 ${className}`}>{children}</div>
);

export const CardFooter = ({ children, className = '' }) => (
  <div className={`p-4 border-t border-[var(--color-border)] bg-[var(--color-background)]/50 ${className}`}>
    {children}
  </div>
);

export default Card;
