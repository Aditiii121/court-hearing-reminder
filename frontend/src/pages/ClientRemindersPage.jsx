import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { clientAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatDateTime, isWithin24Hours, isWithin7Days, getErrorMessage } from '../utils/helpers';
import Loading from '../components/Loading';

export default function ClientRemindersPage() {
  const { isClient } = useAuth();
  const navigate = useNavigate();
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isClient()) {
      navigate('/reminders', { replace: true });
      return;
    }

    clientAPI.getMyReminders()
      .then((res) => {
        const data = res.data;
        setReminders(Array.isArray(data) ? data : []);
      })
      .catch((err) => toast.error(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const urgent = reminders.filter((r) => isWithin24Hours(r.hearingDate));
  const soon = reminders.filter((r) => !isWithin24Hours(r.hearingDate) && isWithin7Days(r.hearingDate));
  const other = reminders.filter((r) => !isWithin24Hours(r.hearingDate) && !isWithin7Days(r.hearingDate));

  const ReminderRow = ({ reminder, highlight }) => (
    <div className={`reminder-card ${highlight}`} data-testid="client-reminder-item">
      <div className="reminder-info">
        <span className="reminder-case">{reminder.caseNumber}</span>
        <span className="reminder-title">{reminder.caseTitle}</span>
        <span className="reminder-date">{formatDateTime(reminder.hearingDate)}</span>
        <span className="reminder-type badge">{reminder.reminderType?.replace('_', ' ')}</span>
        {reminder.message && (
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            {reminder.message}
          </span>
        )}
      </div>
      <button
        className="btn btn-sm btn-secondary"
        onClick={() => navigate(`/client/hearings/${reminder.hearingId}`)}
      >
        View Hearing
      </button>
    </div>
  );

  if (loading) return <Loading message="Loading your reminders…" />;

  return (
    <div className="page" data-testid="client-reminders">
      <div className="page-header">
        <h2 className="page-title">🔔 My Reminders</h2>
        <button className="btn btn-secondary" onClick={() => navigate('/client/dashboard')}>
          ← Dashboard
        </button>
      </div>

      {reminders.length === 0 ? (
        <div className="empty-state" data-testid="client-no-reminders">
          <div style={{ fontSize: '2.5rem', marginBottom: '0.75rem' }}>✅</div>
          <p>No reminders at this time.</p>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '0.5rem' }}>
            You are all caught up! Check back closer to your hearing dates.
          </p>
        </div>
      ) : (
        <>
          {urgent.length > 0 && (
            <section className="reminder-section">
              <h3 className="reminder-section-title urgent">🔴 Due Within 24 Hours ({urgent.length})</h3>
              {urgent.map((r, i) => <ReminderRow key={i} reminder={r} highlight="urgent" />)}
            </section>
          )}

          {soon.length > 0 && (
            <section className="reminder-section">
              <h3 className="reminder-section-title soon">🟡 Due Within 7 Days ({soon.length})</h3>
              {soon.map((r, i) => <ReminderRow key={i} reminder={r} highlight="soon" />)}
            </section>
          )}

          {other.length > 0 && (
            <section className="reminder-section">
              <h3 className="reminder-section-title">📅 Other Reminders ({other.length})</h3>
              {other.map((r, i) => <ReminderRow key={i} reminder={r} highlight="" />)}
            </section>
          )}
        </>
      )}
    </div>
  );
}

