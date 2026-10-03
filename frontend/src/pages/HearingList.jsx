import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { hearingAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatDate, truncate, getErrorMessage } from '../utils/helpers';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import ConfirmDialog from '../components/ConfirmDialog';
import Pagination from '../components/Pagination';
import Loading from '../components/Loading';

export default function HearingList() {
  const [hearings, setHearings] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const { user } = useAuth();
  const navigate = useNavigate();

  const fetchHearings = useCallback(() => {
    setLoading(true);
    const req = activeSearch
      ? hearingAPI.search({ q: activeSearch, page, size: 10 })
      : hearingAPI.getAll({ page, size: 10 });

    req
      .then((res) => {
        const data = res.data;
        // API may return Page or plain list
        if (Array.isArray(data)) {
          setHearings(data);
          setTotalPages(1);
        } else {
          setHearings(data.content || []);
          setTotalPages(data.totalPages || 1);
        }
      })
      .catch((err) => toast.error(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [page, activeSearch]);

  useEffect(() => { fetchHearings(); }, [fetchHearings]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setActiveSearch(searchQuery.trim());
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    setActiveSearch('');
    setPage(0);
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await hearingAPI.delete(deleteTarget.id);
      toast.success('Hearing deleted.');
      setDeleteTarget(null);
      fetchHearings();
    } catch (err) {
      toast.error(getErrorMessage(err));
      setDeleteTarget(null);
    }
  };

  const canDelete = user?.role === 'ADMIN';
  const canAdd = user?.role === 'ADMIN' || user?.role === 'LAWYER';

  return (
    <div className="page">
      <div className="page-header">
        <h2 className="page-title">Hearings</h2>
        {canAdd && (
          <button className="btn btn-primary" id="add-hearing-btn" onClick={() => navigate('/hearings/add')}>
            + Add Hearing
          </button>
        )}
      </div>

      {/* Search bar */}
      <form className="search-bar" onSubmit={handleSearch}>
        <input
          type="text"
          className="form-control search-input"
          placeholder="Search by case number, title, judge, parties…"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          id="search-input"
          data-testid="search-input"
        />
        <button type="submit" className="btn btn-primary" id="search-btn" data-testid="search-btn">
          Search
        </button>
        {activeSearch && (
          <button type="button" className="btn btn-secondary" onClick={handleClearSearch}>
            Clear
          </button>
        )}
      </form>

      {activeSearch && (
        <p className="search-result-hint">
          Showing results for <strong>"{activeSearch}"</strong>
        </p>
      )}

      {loading ? (
        <Loading message="Fetching hearings…" />
      ) : hearings.length === 0 ? (
        <div className="empty-state">
          <p>No hearings found.</p>
          {canAdd && (
            <button className="btn btn-primary" onClick={() => navigate('/hearings/add')}>
              Add First Hearing
            </button>
          )}
        </div>
      ) : (
        <>
          <div className="table-wrapper">
            <table className="data-table" data-testid="hearing-table">
              <thead>
                <tr>
                  <th>Case No.</th>
                  <th>Title</th>
                  <th>Court</th>
                  <th>Judge</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {hearings.map((h) => (
                  <tr key={h.id} className="table-row">
                    <td><code>{h.caseNumber}</code></td>
                    <td>{truncate(h.title, 35)}</td>
                    <td>{truncate(h.courtName, 25)}</td>
                    <td>{truncate(h.judgeName, 20)}</td>
                    <td>{formatDate(h.hearingDate)}</td>
                    <td><StatusBadge status={h.status} /></td>
                    <td><PriorityBadge priority={h.priority} /></td>
                    <td>
                      <div className="action-buttons">
                        <button
                          className="btn btn-sm btn-secondary"
                          onClick={() => navigate(`/hearings/${h.id}`)}
                          id={`view-btn-${h.id}`}
                        >
                          View
                        </button>
                        {(user?.role === 'ADMIN' || user?.role === 'LAWYER') && (
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => navigate(`/hearings/${h.id}/edit`)}
                            id={`edit-btn-${h.id}`}
                          >
                            Edit
                          </button>
                        )}
                        {canDelete && (
                          <button
                            className="btn btn-sm btn-danger"
                            onClick={() => setDeleteTarget(h)}
                            id={`delete-btn-${h.id}`}
                          >
                            Del
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <ConfirmDialog
        isOpen={!!deleteTarget}
        title="Delete Hearing"
        message={`Delete case "${deleteTarget?.caseNumber}"? This cannot be undone.`}
        confirmLabel="Delete"
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
