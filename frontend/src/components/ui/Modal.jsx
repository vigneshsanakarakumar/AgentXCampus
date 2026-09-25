import React from 'react';
import { X } from 'lucide-react';

export const Modal = ({ isOpen, onClose, title, children, maxWidth = 'max-w-md' }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-fade-in">
      <div className={`w-full ${maxWidth} bg-[var(--color-card)] border border-[var(--color-border)] rounded-xl shadow-xl overflow-hidden`}>
        <div className="p-4 border-b border-[var(--color-border)] flex items-center justify-between">
          <h3 className="font-semibold text-base text-[var(--color-foreground)]">{title}</h3>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] hover:bg-[var(--color-border)]/50 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
};

export default Modal;
