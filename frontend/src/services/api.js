import axios from 'axios';

const API_BASE = '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach JWT
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: handle 401 globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
};

export const hearingAPI = {
  getAll: (params) => api.get('/hearings', { params }),
  getById: (id) => api.get(`/hearings/${id}`),
  create: (data) => api.post('/hearings', data),
  update: (id, data) => api.put(`/hearings/${id}`, data),
  delete: (id) => api.delete(`/hearings/${id}`),
  updateStatus: (id, status) => api.patch(`/hearings/${id}/status`, { status }),
  search: (params) => api.get('/hearings/search', { params }),
};

export const dashboardAPI = {
  getSummary: () => api.get('/dashboard/summary'),
};

export const reminderAPI = {
  getUpcoming: (type) => api.get('/reminders/upcoming', { params: type ? { type } : {} }),
  trigger: () => api.post('/reminders/trigger'),
};

export const healthAPI = {
  check: () => api.get('/health'),
};

// Client-specific API — all endpoints are scoped to the authenticated user server-side
export const clientAPI = {
  getDashboard: () => api.get('/client/dashboard'),
  getMyHearings: (params) => api.get('/client/hearings', { params }),
  getMyHearingById: (id) => api.get(`/client/hearings/${id}`),
  getMyUpcomingHearings: () => api.get('/client/hearings/upcoming'),
  getMyReminders: () => api.get('/client/reminders'),
};

export default api;
