import React from 'react';

export const Avatar = ({ name = 'Campus User', src, size = 'md', role, className = '' }) => {
  const getInitials = (str) => {
    if (!str) return 'U';
    const parts = str.trim().split(' ');
    if (parts.length >= 2) return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    return str.substring(0, 2).toUpperCase();
  };

  const sizes = {
    sm: 'w-7 h-7 text-xs',
    md: 'w-9 h-9 text-sm',
    lg: 'w-12 h-12 text-base font-semibold',
  };

  return (
    <div
      className={`relative inline-flex items-center justify-center rounded-full bg-[var(--color-primary)]/15 text-[var(--color-primary)] font-medium border border-[var(--color-border)] select-none shrink-0 ${sizes[size]} ${className}`}
    >
      {src ? (
        <img src={src} alt={name} className="w-full h-full object-cover rounded-full" />
      ) : (
        <span>{getInitials(name)}</span>
      )}
    </div>
  );
};

export default Avatar;
