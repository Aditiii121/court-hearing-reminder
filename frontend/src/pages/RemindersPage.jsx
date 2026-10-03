import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { reminderAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatDateTime, isWithin24Hours, isWithin7Days, getErrorMessage } from '../utils/helpers';
import Loading from '../components/Loading';

export default function RemindersPage() {
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [triggering, setTriggering] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const fetchReminders = () => {
    setLoading(true);
    reminderAPI.getUpcoming()
      .then((res) => {
        const data = res.data;
        setReminders(Array.isArray(data) ? data : (data.content || []));
      })
      .catch((err) => toast.error(getErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchReminders(); }, []);

  const handleTrigger = async () => {
    setTriggering(true);
    try {
      await reminderAPI.trigger();
      toast.success('Reminder scheduler triggered!');
      fetchReminders();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setTriggering(false);
    }
  };

  const urgent = reminders.filter((r) => isWithin24Hours(r.hearingDate));
  const soon = reminders.filter((r) => !isWithin24Hours(r.hearingDate) && isWithin7Days(r.hearingDate));
  const other = reminders.filter((r) => !isWithin24Hours(r.hearingDate) && !isWithin7Days(r.hearingDate));

  const ReminderRow = ({ reminder, highlight }) => (
    <div className={`reminder-card ${highlight}`} data-testid="reminder-item">
      <div className="reminder-info">
        <span className="reminder-case">{reminder.caseNumber || reminder.hearing?.caseNumber}</span>
        <span className="reminder-title">{reminder.hearingTitle || reminder.hearing?.title}</span>
        <span className="reminder-date">{formatDateTime(reminder.hearingDate || reminder.hearing?.hearingDate)}</span>
        <span className="reminder-type badge">{reminder.reminderType}</span>
      </div>
      <button
        className="btn btn-sm btn-secondary"
        onClick={() => navigate(`/hearings/${reminder.hearingId || reminder.hearing?.id}`)}
      >
        View
      </button>
    </div>
  );

  if (loading) return <Loading message="Loading reminders…" />;

  return (
    <div className="page">
      <div className="page-header">
        <h2 className="page-title">🔔 Reminders</h2>
        {user?.role === 'ADMIN' && (
          <button className="btn btn-primary" onClick={handleTrigger} disabled={triggering} id="trigger-reminders-btn">
            {triggering ? 'Running…' : '⚡ Trigger Scheduler'}
          </button>
        )}
      </div>

      {reminders.length === 0 ? (
        <div className="empty-state">
          <p>No upcoming reminders. All hearings are up to date!</p>
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
              <h3 className="reminder-section-title">📅 Other Upcoming ({other.length})</h3>
              {other.map((r, i) => <ReminderRow key={i} reminder={r} highlight="" />)}
            </section>
          )}
        </>
      )}
    </div>
  );
}
