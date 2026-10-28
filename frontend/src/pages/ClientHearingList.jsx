import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { clientAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/helpers';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import Loading from '../components/Loading';
import Pagination from '../components/Pagination';

export default function ClientHearingList() {
  const { isClient } = useAuth();
  const navigate = useNavigate();
  const [hearings, setHearings] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const PAGE_SIZE = 10;

  useEffect(() => {
    if (!isClient()) {
      navigate('/hearings', { replace: true });
      return;
    }
    fetchHearings(page);
  }, [page]);

  const fetchHearings = (pageNum) => {
    setLoading(true);
    clientAPI.getMyHearings({ page: pageNum, size: PAGE_SIZE, sortBy: 'hearingDate', sortDir: 'asc' })
      .then((res) => {
        const data = res.data;
        setHearings(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      })
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  if (loading) return <Loading message="Loading your hearings…" />;

  if (error) return (
    <div className="page">
      <div className="alert alert-error">{error}</div>
    </div>
  );

  return (
    <div className="page" data-testid="client-hearing-list">
      <div className="page-header">
        <div>
          <h2 className="page-title">My Hearings</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            {totalElements} hearing{totalElements !== 1 ? 's' : ''} associated with your account
          </p>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate('/client/dashboard')}>
          ← Dashboard
        </button>
      </div>

      {hearings.length === 0 ? (
        <div className="empty-state" data-testid="client-no-hearings">
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📭</div>
          <h3 style={{ marginBottom: '0.5rem' }}>No hearings found</h3>
          <p style={{ color: 'var(--text-muted)' }}>
            No hearings are currently linked to your account.
            Please contact your lawyer or court clerk if you believe this is incorrect.
          </p>
        </div>
      ) : (
        <>
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Case Number</th>
                  <th>Case Title</th>
                  <th>Court</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Judge</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {hearings.map((h) => (
                  <tr
                    key={h.id}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/client/hearings/${h.id}`)}
                    data-testid="client-hearing-row"
                  >
                    <td>
                      <code style={{ color: 'var(--primary-light)', fontSize: '0.82rem' }}>
                        {h.caseNumber}
                      </code>
                    </td>
                    <td style={{ fontWeight: 500 }}>{h.caseTitle}</td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{h.courtName}</td>
                    <td>{h.hearingDate}</td>
                    <td>{h.hearingTime || '—'}</td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{h.judgeName || '—'}</td>
                    <td><StatusBadge status={h.status} /></td>
                    <td><PriorityBadge priority={h.priority} /></td>
                    <td>
                      <button
                        className="btn btn-sm btn-secondary"
                        onClick={(e) => { e.stopPropagation(); navigate(`/client/hearings/${h.id}`); }}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <Pagination
              page={page}
              totalPages={totalPages}
              onPageChange={setPage}
            />
          )}
        </>
      )}
    </div>
  );
}

