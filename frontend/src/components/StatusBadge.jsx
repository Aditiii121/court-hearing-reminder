import { STATUS_LABELS, statusClass } from '../utils/helpers';

export default function StatusBadge({ status }) {
  return (
    <span className={`badge ${statusClass(status)}`}>
      {STATUS_LABELS[status] || status}
    </span>
  );
}
