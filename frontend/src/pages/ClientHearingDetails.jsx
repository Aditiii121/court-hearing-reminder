import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { clientAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatDateTime, getErrorMessage } from '../utils/helpers';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import Loading from '../components/Loading';

function DetailItem({ label, value }) {
  return (
    <div className="detail-item">
      <span className="detail-label">{label}</span>
      <span className="detail-value">{value || '—'}</span>
    </div>
  );
}

export default function ClientHearingDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isClient } = useAuth();
  const [hearing, setHearing] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isClient()) {
      navigate(`/hearings/${id}`, { replace: true });
      return;
    }

    clientAPI.getMyHearingById(id)
      .then((res) => setHearing(res.data))
      .catch((err) => {
        toast.error(getErrorMessage(err));
        navigate('/client/hearings');
      })
      .finally(() => setLoading(false));
  }, [id, isClient, navigate]);

  if (loading) return <Loading message="Loading hearing details…" />;
  if (!hearing) return null;

  return (
    <div className="page" data-testid="client-hearing-details">
      <div className="page-header">
        <h2 className="page-title">Hearing Details</h2>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={() => navigate('/client/hearings')}>
            ← Back to My Hearings
          </button>
        </div>
      </div>

      <div className="detail-card">
        <div className="detail-header">
          <div>
            <code className="case-number">{hearing.caseNumber}</code>
            <h3 className="hearing-title" style={{ marginTop: '0.5rem' }}>{hearing.caseTitle}</h3>
          </div>
          <div className="detail-badges">
            <StatusBadge status={hearing.status} />
            <PriorityBadge priority={hearing.priority} />
          </div>
        </div>

        <div className="detail-grid">
          <DetailItem label="Court" value={hearing.courtName} />
          <DetailItem label="Case Type" value={hearing.caseType} />
          <DetailItem label="Judge" value={hearing.judgeName} />
          <DetailItem label="Assigned Lawyer" value={hearing.assignedLawyer} />
          <DetailItem label="Hearing Date" value={hearing.hearingDate} />
          <DetailItem label="Hearing Time" value={hearing.hearingTime} />
          <DetailItem label="Petitioner" value={hearing.petitioner} />
          <DetailItem label="Respondent" value={hearing.respondent} />
          <DetailItem label="Created" value={formatDateTime(hearing.createdAt)} />
          <DetailItem label="Last Updated" value={formatDateTime(hearing.updatedAt)} />
        </div>

        {hearing.description && (
          <div className="detail-section">
            <h4>Description</h4>
            <p>{hearing.description}</p>
          </div>
        )}

        {/* Read-only notice — clients cannot change hearing status */}
        <div style={{
          marginTop: '1.5rem',
          padding: '0.75rem 1rem',
          background: 'rgba(99, 102, 241, 0.08)',
          border: '1px solid rgba(99, 102, 241, 0.2)',
          borderRadius: '8px',
          color: 'var(--text-secondary)',
          fontSize: '0.85rem',
        }}>
          ℹ️ This is a read-only view. Only authorised court staff can update hearing details or status.
        </div>
      </div>
    </div>
  );
}
