import React from 'react';

export const PageHeader = ({ title, subtitle, actions, breadcrumbs }) => (
  <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
    <div>
      {breadcrumbs && <div className="text-xs text-[var(--color-muted-foreground)] mb-1.5">{breadcrumbs}</div>}
      <h1 className="text-2xl font-bold text-[var(--color-foreground)] tracking-tight">{title}</h1>
      {subtitle && <p className="text-sm text-[var(--color-muted-foreground)] mt-0.5">{subtitle}</p>}
    </div>
    {actions && <div className="flex items-center gap-2.5">{actions}</div>}
  </div>
);

export default PageHeader;
