import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { clientAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/helpers';
import Loading from '../components/Loading';

const STATUS_COLORS = {
  SCHEDULED: 'var(--accent-blue)',
  PENDING: 'var(--accent-amber)',
  IN_PROGRESS: '#3b82f6',
  ADJOURNED: 'var(--accent-purple)',
  COMPLETED: 'var(--accent-green)',
  CANCELLED: 'var(--accent-red)',
};

export default function ClientDashboard() {
  const { user, isClient } = useAuth();
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [upcomingHearings, setUpcomingHearings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // Redirect non-client users
    if (!isClient()) {
      navigate('/dashboard', { replace: true });
      return;
    }

    Promise.all([
      clientAPI.getDashboard(),
      clientAPI.getMyUpcomingHearings(),
    ])
      .then(([summaryRes, upcomingRes]) => {
        setSummary(summaryRes.data);
        setUpcomingHearings(Array.isArray(upcomingRes.data) ? upcomingRes.data : []);
      })
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading message="Loading your dashboard…" />;

  if (error) return (
    <div className="page">
      <div className="alert alert-error">{error}</div>
    </div>
  );

  if (!summary) return null;

  const statCards = [
    { label: 'Total Hearings', value: summary.totalHearings, icon: '⚖️', color: 'var(--accent-blue)' },
    { label: 'Upcoming', value: summary.upcomingHearings, icon: '📅', color: 'var(--accent-purple)' },
    { label: 'Scheduled', value: summary.scheduledHearings, icon: '🗓️', color: 'var(--accent-blue)' },
    { label: 'Pending', value: summary.pendingHearings, icon: '⏳', color: 'var(--accent-amber)' },
    { label: 'Completed', value: summary.completedHearings, icon: '✅', color: 'var(--accent-green)' },
    { label: 'Adjourned', value: summary.adjournedHearings, icon: '⏸️', color: 'var(--accent-purple)' },
  ];

  return (
    <div className="page" data-testid="client-dashboard">
      {/* Welcome header */}
      <div className="page-header">
        <div>
          <h2 className="page-title">Welcome, {summary.clientName || user?.fullName}!</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '0.25rem', fontSize: '0.9rem' }}>
            Here is a summary of your case hearings.
          </p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => navigate('/client/hearings')}
        >
          View All Hearings
        </button>
      </div>

      {/* Stat cards */}
      <div className="stat-grid">
        {statCards.map((card) => (
          <div key={card.label} className="stat-card" style={{ borderTopColor: card.color }}>
            <div className="stat-icon">{card.icon}</div>
            <div className="stat-value" style={{ color: card.color }}>{card.value ?? 0}</div>
            <div className="stat-label">{card.label}</div>
          </div>
        ))}
      </div>

      {/* Upcoming hearings section */}
      <div style={{ marginTop: '2rem' }}>
        <div className="page-header" style={{ marginBottom: '1rem' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Upcoming Hearings</h3>
          <button
            className="btn btn-secondary"
            style={{ fontSize: '0.8rem', padding: '0.4rem 0.9rem' }}
            onClick={() => navigate('/client/hearings')}
          >
            See All →
          </button>
        </div>

        {upcomingHearings.length === 0 ? (
          <div className="empty-state" data-testid="client-no-upcoming">
            <div style={{ fontSize: '2.5rem', marginBottom: '0.75rem' }}>📭</div>
            <p>No upcoming hearings scheduled.</p>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '0.5rem' }}>
              Contact your lawyer or clerk if you believe this is incorrect.
            </p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {upcomingHearings.slice(0, 5).map((h) => (
              <div
                key={h.id}
                className="hearing-row"
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/client/hearings/${h.id}`)}
                data-testid="client-hearing-item"
              >
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.25rem' }}>
                    <code style={{
                      background: 'var(--bg-dark)',
                      padding: '0.15rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.75rem',
                      color: 'var(--primary-light)',
                    }}>
                      {h.caseNumber}
                    </code>
                    <span style={{
                      fontSize: '0.72rem',
                      fontWeight: 600,
                      color: STATUS_COLORS[h.status] || 'var(--text-secondary)',
                      textTransform: 'uppercase',
                    }}>
                      {h.status?.replace('_', ' ')}
                    </span>
                  </div>
                  <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.2rem' }}>
                    {h.caseTitle}
                  </div>
                  <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                    📍 {h.courtName} &nbsp;|&nbsp; 📅 {h.hearingDate}
                    {h.hearingTime && <> &nbsp;|&nbsp; 🕐 {h.hearingTime}</>}
                    {h.judgeName && <> &nbsp;|&nbsp; 👨‍⚖️ {h.judgeName}</>}
                  </div>
                </div>
                <button
                  className="btn btn-secondary"
                  style={{ fontSize: '0.8rem', padding: '0.4rem 0.9rem', whiteSpace: 'nowrap' }}
                  onClick={(e) => { e.stopPropagation(); navigate(`/client/hearings/${h.id}`); }}
                >
                  View →
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Reminders teaser */}
      {summary.upcomingHearings > 0 && (
        <div className="alert alert-urgent" style={{ marginTop: '1.5rem' }}>
          🔔 You have <strong>{summary.upcomingHearings}</strong> upcoming hearing(s).{' '}
          <span
            style={{ cursor: 'pointer', textDecoration: 'underline' }}
            onClick={() => navigate('/client/reminders')}
          >
            View reminders →
          </span>
        </div>
      )}
    </div>
  );
}

