// Date / time helpers
export function formatDate(isoString) {
  if (!isoString) return '—';
  const d = new Date(isoString);
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function formatDateTime(isoString) {
  if (!isoString) return '—';
  const d = new Date(isoString);
  return d.toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit', hour12: true,
  });
}

export function isWithin24Hours(isoString) {
  if (!isoString) return false;
  const diff = new Date(isoString) - new Date();
  return diff > 0 && diff <= 24 * 60 * 60 * 1000;
}

export function isWithin7Days(isoString) {
  if (!isoString) return false;
  const diff = new Date(isoString) - new Date();
  return diff > 0 && diff <= 7 * 24 * 60 * 60 * 1000;
}

// Status helpers
export const STATUS_LABELS = {
  SCHEDULED: 'Scheduled',
  PENDING: 'Pending',
  IN_PROGRESS: 'In Progress',
  ADJOURNED: 'Adjourned',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

export function statusClass(status) {
  const map = {
    SCHEDULED: 'badge-scheduled',
    PENDING: 'badge-pending',
    IN_PROGRESS: 'badge-in-progress',
    ADJOURNED: 'badge-adjourned',
    COMPLETED: 'badge-completed',
    CANCELLED: 'badge-cancelled',
  };
  return map[status] || 'badge-pending';
}

// Priority helpers
export const PRIORITY_LABELS = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  URGENT: 'Urgent',
};

export function priorityClass(priority) {
  const map = {
    LOW: 'priority-low',
    MEDIUM: 'priority-medium',
    HIGH: 'priority-high',
    URGENT: 'priority-urgent',
  };
  return map[priority] || 'priority-medium';
}

// Truncate text
export function truncate(str, n = 40) {
  if (!str) return '';
  return str.length > n ? str.slice(0, n) + '…' : str;
}

// Extract error message from axios error
export function getErrorMessage(err) {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    'An unexpected error occurred.'
  );
}
