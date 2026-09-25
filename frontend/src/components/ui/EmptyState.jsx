import React from 'react';
import { Inbox } from 'lucide-react';
import Button from './Button';

export const EmptyState = ({
  icon: Icon = Inbox,
  title = 'No records found',
  description = 'There are no active records in this view right now.',
  actionLabel,
  onAction,
}) => (
  <div className="flex flex-col items-center justify-center p-8 text-center rounded-xl border border-dashed border-[var(--color-border)] bg-[var(--color-background)]/40 my-4">
    <div className="p-3 rounded-full bg-[var(--color-card)] text-[var(--color-muted-foreground)] mb-3 border border-[var(--color-border)]">
      <Icon className="w-6 h-6" />
    </div>
    <h4 className="text-sm font-semibold text-[var(--color-foreground)]">{title}</h4>
    <p className="text-xs text-[var(--color-muted-foreground)] max-w-sm mt-1 mb-4">{description}</p>
    {actionLabel && onAction && (
      <Button size="sm" variant="secondary" onClick={onAction}>
        {actionLabel}
      </Button>
    )}
  </div>
);

export default EmptyState;
