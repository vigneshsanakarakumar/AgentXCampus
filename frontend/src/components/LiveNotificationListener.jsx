import React, { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

/**
 * Global Real-time SSE Live Notification Listener.
 * Connects to `/api/v1/notifications/stream?token=...` and displays instant
 * toast notifications when requests (leave, OD, gate pass, etc.) are updated or approved.
 */
export const LiveNotificationListener = () => {
  const { user, token } = useAuth();
  const { addToast } = useToast();

  useEffect(() => {
    if (!user || !token) return;

    let eventSource = null;
    let isSubscribed = true;

    try {
      const streamUrl = `http://localhost:8080/api/v1/notifications/stream?token=${encodeURIComponent(token)}`;
      eventSource = new EventSource(streamUrl);

      eventSource.addEventListener('connected', () => {
        // Connected to real-time notification stream
      });

      eventSource.addEventListener('notification', (e) => {
        if (!isSubscribed) return;
        try {
          const data = JSON.parse(e.data);
          const title = data.title || 'Campus Update';
          const msg = data.message || '';
          addToast(`${title}: ${msg}`, 'success', 6000);
        } catch (err) {
          console.error('Failed to parse SSE notification payload:', err);
        }
      });

      eventSource.onerror = () => {
        // Browser EventSource automatically reconnects with exponential backoff
      };
    } catch (err) {
      console.warn('Could not establish LiveNotificationListener EventSource:', err);
    }

    return () => {
      isSubscribed = false;
      if (eventSource) {
        eventSource.close();
      }
    };
  }, [user, token, addToast]);

  return null;
};

export default LiveNotificationListener;
