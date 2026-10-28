import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { hearingAPI } from '../services/api';
import { getErrorMessage } from '../utils/helpers';
import Loading from '../components/Loading';

const STATUSES = ['SCHEDULED', 'PENDING', 'IN_PROGRESS', 'ADJOURNED', 'COMPLETED', 'CANCELLED'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
const HEARING_TYPES = ['REGULAR', 'URGENT', 'APPEAL', 'BAIL', 'TRIAL', 'MOTION', 'SETTLEMENT'];

// Combine separate hearingDate (YYYY-MM-DD) and hearingTime (HH:mm) from API into datetime-local value
function toLocalDatetime(date, time) {
  if (!date) return '';
  return time ? `${date}T${time.substring(0, 5)}` : `${date}T00:00`;
}

export default function EditHearing() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    hearingAPI.getById(id)
      .then((res) => {
        const h = res.data;
        // Map backend field names → form state field names
        setForm({
          caseNumber:    h.caseNumber    || '',
          title:         h.caseTitle     || '',
          description:   h.description   || '',
          courtName:     h.courtName     || '',
          judgeName:     h.judgeName     || '',
          petitionerName: h.petitioner   || '',
          respondentName: h.respondent   || '',
          lawyerName:    h.assignedLawyer || '',
          hearingDate:   toLocalDatetime(h.hearingDate, h.hearingTime),
          hearingType:   h.caseType      || 'REGULAR',
          status:        h.status        || 'SCHEDULED',
          priority:      h.priority      || 'MEDIUM',
        });
      })
      .catch((err) => { toast.error(getErrorMessage(err)); navigate('/hearings'); })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      // Split datetime-local value into separate date and time for the backend
      let hearingDate = '';
      let hearingTime = null;
      if (form.hearingDate) {
        const [datePart, timePart] = form.hearingDate.split('T');
        hearingDate = datePart;
        hearingTime = timePart || null;
      }

      // Map form field names → backend DTO field names
      const payload = {
        caseNumber:     form.caseNumber,
        caseTitle:      form.title,
        courtName:      form.courtName,
        judgeName:      form.judgeName,
        petitioner:     form.petitionerName,
        respondent:     form.respondentName,
        assignedLawyer: form.lawyerName,
        caseType:       form.hearingType,
        hearingDate,
        hearingTime,
        status:         form.status,
        priority:       form.priority,
        description:    form.description,
      };

      await hearingAPI.update(id, payload);
      toast.success('Hearing updated!');
      navigate(`/hearings/${id}`);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Loading message="Loading hearing…" />;
  if (!form) return null;

  return (
    <div className="page">
      <div className="page-header">
        <h2 className="page-title">Edit Hearing</h2>
        <button className="btn btn-secondary" onClick={() => navigate(`/hearings/${id}`)}>← Back</button>
      </div>

      <div className="form-card">
        <form onSubmit={handleSubmit} noValidate>
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="caseNumber">Case Number *</label>
              <input id="caseNumber" name="caseNumber" className="form-control" required
                value={form.caseNumber} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="title">Title *</label>
              <input id="title" name="title" className="form-control" required
                value={form.title} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="courtName">Court Name *</label>
              <input id="courtName" name="courtName" className="form-control" required
                value={form.courtName} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="courtRoom">Court Room</label>
              <input id="courtRoom" name="courtRoom" className="form-control"
                value={form.courtRoom} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="judgeName">Judge Name *</label>
              <input id="judgeName" name="judgeName" className="form-control" required
                value={form.judgeName} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="hearingDate">Hearing Date & Time *</label>
              <input id="hearingDate" name="hearingDate" type="datetime-local" className="form-control" required
                value={form.hearingDate} onChange={handleChange} />
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
              <label htmlFor="petitionerName">Petitioner</label>
              <input id="petitionerName" name="petitionerName" className="form-control"
                value={form.petitionerName} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="respondentName">Respondent</label>
              <input id="respondentName" name="respondentName" className="form-control"
                value={form.respondentName} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="lawyerName">Lawyer</label>
              <input id="lawyerName" name="lawyerName" className="form-control"
                value={form.lawyerName} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea id="description" name="description" className="form-control textarea"
              rows={3} value={form.description} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label htmlFor="notes">Notes</label>
            <textarea id="notes" name="notes" className="form-control textarea"
              rows={3} value={form.notes} onChange={handleChange} />
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary"
              onClick={() => navigate(`/hearings/${id}`)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
