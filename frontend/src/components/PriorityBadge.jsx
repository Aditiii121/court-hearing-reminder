import { PRIORITY_LABELS, priorityClass } from '../utils/helpers';

export default function PriorityBadge({ priority }) {
  return (
    <span className={`badge ${priorityClass(priority)}`}>
      {PRIORITY_LABELS[priority] || priority}
    </span>
  );
}
