import test, { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  formatDate,
  formatDateTime,
  isWithin24Hours,
  isWithin7Days,
  statusClass,
  priorityClass,
  truncate,
  getErrorMessage
} from '../utils/helpers.js';
import { clientAPI } from '../services/api.js';

describe('Client Portal Helpers & Utilities', () => {
  it('formats dates and handles missing/empty values gracefully', () => {
    assert.equal(formatDate(null), '—');
    assert.equal(formatDate(''), '—');
    assert.equal(formatDateTime(null), '—');
  });

  it('correctly classifies urgent reminders (within 24 hours)', () => {
    const now = new Date();
    const in12Hours = new Date(now.getTime() + 12 * 60 * 60 * 1000).toISOString();
    const in3Days = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000).toISOString();
    const in10Days = new Date(now.getTime() + 10 * 24 * 60 * 60 * 1000).toISOString();
    const past = new Date(now.getTime() - 2 * 60 * 60 * 1000).toISOString();

    assert.equal(isWithin24Hours(in12Hours), true);
    assert.equal(isWithin24Hours(in3Days), false);
    assert.equal(isWithin24Hours(past), false);

    assert.equal(isWithin7Days(in12Hours), true);
    assert.equal(isWithin7Days(in3Days), true);
    assert.equal(isWithin7Days(in10Days), false);
  });

  it('correctly maps status and priority CSS classes for badges', () => {
    assert.equal(statusClass('SCHEDULED'), 'badge-scheduled');
    assert.equal(statusClass('COMPLETED'), 'badge-completed');
    assert.equal(statusClass('CANCELLED'), 'badge-cancelled');
    assert.equal(statusClass('UNKNOWN'), 'badge-pending');

    assert.equal(priorityClass('URGENT'), 'priority-urgent');
    assert.equal(priorityClass('HIGH'), 'priority-high');
    assert.equal(priorityClass('LOW'), 'priority-low');
  });

  it('truncates strings and extracts error messages reliably', () => {
    assert.equal(truncate('Short text', 20), 'Short text');
    assert.equal(truncate('A very long description that exceeds limit', 10), 'A very lon…');
    assert.equal(truncate(null), '');

    const apiError = { response: { data: { message: 'Unauthorized hearing access' } } };
    assert.equal(getErrorMessage(apiError), 'Unauthorized hearing access');

    const fallbackError = new Error('Network failure');
    assert.equal(getErrorMessage(fallbackError), 'Network failure');
  });
});

describe('Client API Service Configuration', () => {
  it('exposes client-scoped API endpoints with proper function definitions', () => {
    assert.equal(typeof clientAPI.getDashboard, 'function');
    assert.equal(typeof clientAPI.getMyHearings, 'function');
    assert.equal(typeof clientAPI.getMyHearingById, 'function');
    assert.equal(typeof clientAPI.getMyUpcomingHearings, 'function');
    assert.equal(typeof clientAPI.getMyReminders, 'function');
  });
});

describe('Client Access Control & Navigation Rules', () => {
  it('identifies CLIENT role correctly and denies staff mutation actions', () => {
    const clientUser = { role: 'CLIENT', email: 'client@example.com', fullName: 'Ravi Client' };
    const adminUser = { role: 'ADMIN', email: 'admin@example.com', fullName: 'Admin User' };

    // Role predicates
    const isClient = (u) => u?.role === 'CLIENT';
    const canDelete = (u) => u?.role === 'ADMIN';
    const canEdit = (u) => ['ADMIN', 'LAWYER'].includes(u?.role);
    const canCreate = (u) => ['ADMIN', 'LAWYER', 'CLERK'].includes(u?.role);

    // Assert client has view-only permissions
    assert.equal(isClient(clientUser), true);
    assert.equal(canDelete(clientUser), false, 'Client must NOT be allowed to delete hearings');
    assert.equal(canEdit(clientUser), false, 'Client must NOT be allowed to edit hearings');
    assert.equal(canCreate(clientUser), false, 'Client must NOT be allowed to create hearings');

    // Assert admin has appropriate permissions
    assert.equal(isClient(adminUser), false);
    assert.equal(canDelete(adminUser), true);
    assert.equal(canEdit(adminUser), true);
    assert.equal(canCreate(adminUser), true);
  });

  it('resolves home page destination according to user role', () => {
    const resolveHomeRoute = (role) => (role === 'CLIENT' ? '/client/dashboard' : '/dashboard');

    assert.equal(resolveHomeRoute('CLIENT'), '/client/dashboard');
    assert.equal(resolveHomeRoute('ADMIN'), '/dashboard');
    assert.equal(resolveHomeRoute('LAWYER'), '/dashboard');
    assert.equal(resolveHomeRoute('CLERK'), '/dashboard');
  });

  it('filters navigation items so clients only see their own sections', () => {
    const STAFF_NAV_ITEMS = [
      { to: '/dashboard', label: 'Dashboard' },
      { to: '/hearings', label: 'Hearings' },
      { to: '/hearings/add', label: 'Add Hearing', roles: ['ADMIN', 'LAWYER'] },
      { to: '/reminders', label: 'Reminders' },
    ];

    const CLIENT_NAV_ITEMS = [
      { to: '/client/dashboard', label: 'My Dashboard' },
      { to: '/client/hearings', label: 'My Hearings' },
      { to: '/client/reminders', label: 'My Reminders' },
    ];

    const getNavItems = (user) => (user?.role === 'CLIENT' ? CLIENT_NAV_ITEMS : STAFF_NAV_ITEMS);

    const clientNav = getNavItems({ role: 'CLIENT' });
    assert.deepEqual(clientNav.map(n => n.to), [
      '/client/dashboard',
      '/client/hearings',
      '/client/reminders'
    ]);

    const staffNav = getNavItems({ role: 'LAWYER' });
    assert.equal(staffNav.some(n => n.to.startsWith('/client/')), false, 'Staff should not see client routes by default');
  });
});

