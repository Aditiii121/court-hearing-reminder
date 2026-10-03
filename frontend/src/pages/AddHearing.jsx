import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { hearingAPI } from '../services/api';
import { getErrorMessage } from '../utils/helpers';

const STATUSES = ['SCHEDULED', 'PENDING', 'IN_PROGRESS', 'ADJOURNED', 'COMPLETED', 'CANCELLED'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
const HEARING_TYPES = ['REGULAR', 'URGENT', 'APPEAL', 'BAIL', 'TRIAL', 'MOTION', 'SETTLEMENT'];

const EMPTY = {
  caseNumber: '', title: '', description: '', courtName: '', courtRoom: '',
  judgeName: '', petitionerName: '', respondentName: '', lawyerName: '',
  hearingDate: '', hearingType: 'REGULAR', status: 'SCHEDULED', priority: 'MEDIUM',
  notes: '',
};

export default function AddHearing() {
  const [form, setForm] = useState(EMPTY);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await hearingAPI.create(form);
      toast.success('Hearing created successfully!');
      navigate('/hearings');
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2 className="page-title">Add New Hearing</h2>
        <button className="btn btn-secondary" onClick={() => navigate('/hearings')}>← Back</button>
      </div>

      <div className="form-card">
        <form id="add-hearing-form" data-testid="add-hearing-form" onSubmit={handleSubmit} noValidate>
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="caseNumber">Case Number *</label>
              <input id="caseNumber" name="caseNumber" className="form-control" required
                value={form.caseNumber} onChange={handleChange} placeholder="e.g. CIV-2024-0001"
                data-testid="case-number-input" />
            </div>

            <div className="form-group">
              <label htmlFor="title">Title *</label>
              <input id="title" name="title" className="form-control" required
                value={form.title} onChange={handleChange} placeholder="Hearing title"
                data-testid="title-input" />
            </div>

            <div className="form-group">
              <label htmlFor="courtName">Court Name *</label>
              <input id="courtName" name="courtName" className="form-control" required
                value={form.courtName} onChange={handleChange} placeholder="e.g. High Court of Delhi" />
            </div>

            <div className="form-group">
              <label htmlFor="courtRoom">Court Room</label>
              <input id="courtRoom" name="courtRoom" className="form-control"
                value={form.courtRoom} onChange={handleChange} placeholder="e.g. Room 3A" />
            </div>

            <div className="form-group">
              <label htmlFor="judgeName">Judge Name *</label>
              <input id="judgeName" name="judgeName" className="form-control" required
                value={form.judgeName} onChange={handleChange} placeholder="Hon. Justice..." />
            </div>

            <div className="form-group">
              <label htmlFor="hearingDate">Hearing Date & Time *</label>
              <input id="hearingDate" name="hearingDate" type="datetime-local" className="form-control" required
                value={form.hearingDate} onChange={handleChange}
                data-testid="hearing-date-input" />
            </div>

            <div className="form-group">
              <label htmlFor="hearingType">Hearing Type</label>
              <select id="hearingType" name="hearingType" className="form-control"
                value={form.hearingType} onChange={handleChange}>
                {HEARING_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="status">Status</label>
              <select id="status" name="status" className="form-control"
                value={form.status} onChange={handleChange}>
                {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="priority">Priority</label>
              <select id="priority" name="priority" className="form-control"
                value={form.priority} onChange={handleChange}>
                {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="petitionerName">Petitioner / Plaintiff</label>
              <input id="petitionerName" name="petitionerName" className="form-control"
                value={form.petitionerName} onChange={handleChange} />
            </div>

            <div className="form-group">
              <label htmlFor="respondentName">Respondent / Defendant</label>
              <input id="respondentName" name="respondentName" className="form-control"
                value={form.respondentName} onChange={handleChange} />
            </div>

            <div className="form-group">
              <label htmlFor="lawyerName">Lawyer / Advocate</label>
              <input id="lawyerName" name="lawyerName" className="form-control"
                value={form.lawyerName} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea id="description" name="description" className="form-control textarea"
              rows={3} value={form.description} onChange={handleChange}
              placeholder="Brief description of the hearing…" />
          </div>

          <div className="form-group">
            <label htmlFor="notes">Notes</label>
            <textarea id="notes" name="notes" className="form-control textarea"
              rows={3} value={form.notes} onChange={handleChange}
              placeholder="Additional notes…" />
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/hearings')}>
              Cancel
            </button>
            <button
              type="submit"
              id="submit-btn"
              data-testid="submit-btn"
              className="btn btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Creating…' : 'Create Hearing'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
