import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('agentx_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const path = window.location.pathname;
      const isPublic = path.startsWith('/login') || path.startsWith('/signup') || path === '/' || path.startsWith('/forgot-password') || path.startsWith('/reset-password');
      // Only bounce to login if token is expired on protected actions, not during background /auth/me or public pages
      if (!isPublic && error.config && !error.config.url.includes('/auth/me')) {
        localStorage.removeItem('agentx_token');
        localStorage.removeItem('agentx_user');
        window.location.href = '/login?expired=true';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
