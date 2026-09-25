import React from 'react';
import Card, { CardBody } from './Card';

export const StatCard = ({ title, value, subtitle, icon: Icon, badge, className = '' }) => (
  <Card hover className={`relative overflow-hidden ${className}`}>
    <CardBody className="p-5">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium text-[var(--color-muted-foreground)] tracking-wide uppercase">{title}</p>
          <h4 className="text-2xl font-bold text-[var(--color-foreground)] mt-1.5">{value}</h4>
          {subtitle && <p className="text-xs text-[var(--color-muted-foreground)] mt-1">{subtitle}</p>}
        </div>
        {Icon && (
          <div className="p-2.5 rounded-lg bg-[var(--color-primary)]/10 text-[var(--color-primary)]">
            <Icon className="w-5 h-5" />
          </div>
        )}
      </div>
      {badge && <div className="mt-3">{badge}</div>}
    </CardBody>
  </Card>
);

export default StatCard;
