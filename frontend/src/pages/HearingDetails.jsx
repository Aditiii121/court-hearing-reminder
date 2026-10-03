import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { hearingAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatDateTime, getErrorMessage } from '../utils/helpers';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import Loading from '../components/Loading';

const ALLOWED_TRANSITIONS = {
  SCHEDULED: ['PENDING', 'IN_PROGRESS', 'CANCELLED'],
  PENDING: ['SCHEDULED', 'IN_PROGRESS', 'ADJOURNED', 'CANCELLED'],
  IN_PROGRESS: ['ADJOURNED', 'COMPLETED', 'CANCELLED'],
  ADJOURNED: ['SCHEDULED', 'PENDING', 'CANCELLED'],
  COMPLETED: [],
  CANCELLED: [],
};

// Clerks can only use these statuses
const CLERK_ALLOWED = ['SCHEDULED', 'PENDING'];

export default function HearingDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [hearing, setHearing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [newStatus, setNewStatus] = useState('');
  const [updating, setUpdating] = useState(false);

  const fetchHearing = () => {
    hearingAPI.getById(id)
      .then((res) => { setHearing(res.data); setNewStatus(''); })
      .catch((err) => { toast.error(getErrorMessage(err)); navigate('/hearings'); })
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchHearing(); }, [id]); // eslint-disable-line

  const handleStatusUpdate = async () => {
    if (!newStatus) return;
    setUpdating(true);
    try {
      await hearingAPI.updateStatus(id, newStatus);
      toast.success(`Status updated to ${newStatus}`);
      fetchHearing();
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setUpdating(false);
    }
  };

  if (loading) return <Loading message="Loading hearing details…" />;
  if (!hearing) return null;

  const transitions = ALLOWED_TRANSITIONS[hearing.status] || [];
  const availableStatuses = user?.role === 'CLERK'
    ? transitions.filter((s) => CLERK_ALLOWED.includes(s))
    : transitions;

  const canEdit = user?.role === 'ADMIN' || user?.role === 'LAWYER';

  return (
    <div className="page">
      <div className="page-header">
        <h2 className="page-title">Hearing Details</h2>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {canEdit && (
            <button className="btn btn-secondary" onClick={() => navigate(`/hearings/${id}/edit`)}>
              Edit
            </button>
          )}
          <button className="btn btn-secondary" onClick={() => navigate('/hearings')}>← Back</button>
        </div>
      </div>

      <div className="detail-card">
        <div className="detail-header">
          <div>
            <code className="case-number">{hearing.caseNumber}</code>
            <h3 className="hearing-title">{hearing.title}</h3>
          </div>
          <div className="detail-badges">
            <StatusBadge status={hearing.status} />
            <PriorityBadge priority={hearing.priority} />
          </div>
        </div>

        <div className="detail-grid">
          <div className="detail-item">
            <span className="detail-label">Court</span>
            <span className="detail-value">{hearing.courtName}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Court Room</span>
            <span className="detail-value">{hearing.courtRoom || '—'}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Judge</span>
            <span className="detail-value">{hearing.judgeName}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Hearing Type</span>
            <span className="detail-value">{hearing.hearingType}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Date & Time</span>
            <span className="detail-value">{formatDateTime(hearing.hearingDate)}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Petitioner</span>
            <span className="detail-value">{hearing.petitionerName || '—'}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Respondent</span>
            <span className="detail-value">{hearing.respondentName || '—'}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Lawyer</span>
            <span className="detail-value">{hearing.lawyerName || '—'}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Created</span>
            <span className="detail-value">{formatDateTime(hearing.createdAt)}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Updated</span>
            <span className="detail-value">{formatDateTime(hearing.updatedAt)}</span>
          </div>
        </div>

        {hearing.description && (
          <div className="detail-section">
            <h4>Description</h4>
            <p>{hearing.description}</p>
          </div>
        )}
        {hearing.notes && (
          <div className="detail-section">
            <h4>Notes</h4>
            <p>{hearing.notes}</p>
          </div>
        )}

        {/* Status update section */}
        {availableStatuses.length > 0 && (
          <div className="status-update-section">
            <h4>Update Status</h4>
            <div className="status-update-row">
              <select
                id="status-select"
                data-testid="status-select"
                className="form-control"
                value={newStatus}
                onChange={(e) => setNewStatus(e.target.value)}
                style={{ maxWidth: 220 }}
              >
                <option value="">— Select new status —</option>
                {availableStatuses.map((s) => (
                  <option key={s} value={s}>{s.replace('_', ' ')}</option>
                ))}
              </select>
              <button
                id="update-status-btn"
                data-testid="update-status-btn"
                className="btn btn-primary"
                onClick={handleStatusUpdate}
                disabled={!newStatus || updating}
              >
                {updating ? 'Updating…' : 'Update Status'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
