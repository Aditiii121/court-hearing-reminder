import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import { dashboardAPI } from '../services/api';
import { getErrorMessage } from '../utils/helpers';
import Loading from '../components/Loading';

const STATUS_COLORS = {
  SCHEDULED: '#6366f1',
  PENDING: '#f59e0b',
  IN_PROGRESS: '#3b82f6',
  ADJOURNED: '#8b5cf6',
  COMPLETED: '#10b981',
  CANCELLED: '#ef4444',
};

export default function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    dashboardAPI.getSummary()
      .then((res) => setSummary(res.data))
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading message="Loading dashboard…" />;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!summary) return null;

  const statCards = [
    { label: 'Total Hearings', value: summary.totalHearings, icon: '⚖️', color: 'var(--accent-blue)' },
    { label: 'Today\'s Hearings', value: summary.todayHearings, icon: '📅', color: 'var(--accent-purple)' },
    { label: 'Due Within 24h', value: summary.within24Hours, icon: '🔴', color: 'var(--accent-red)' },
    { label: 'Due Within 7 Days', value: summary.within7Days, icon: '🟡', color: 'var(--accent-amber)' },
    { label: 'Completed', value: summary.completedHearings, icon: '✅', color: 'var(--accent-green)' },
    { label: 'Cancelled', value: summary.cancelledHearings, icon: '❌', color: 'var(--accent-red)' },
  ];

  const statusData = Object.entries(summary.byStatus || {}).map(([name, value]) => ({
    name: name.replace('_', ' '),
    value,
    fill: STATUS_COLORS[name] || '#6366f1',
  }));

  const priorityData = Object.entries(summary.byPriority || {}).map(([name, value]) => ({
    name,
    value,
  }));

  return (
    <div className="page" data-testid="dashboard-summary">
      <div className="page-header">
        <h2 className="page-title">Dashboard</h2>
        <button className="btn btn-primary" onClick={() => navigate('/hearings/add')} id="dash-add-btn">
          + New Hearing
        </button>
      </div>

      {/* Stat Cards */}
      <div className="stat-grid">
        {statCards.map((card) => (
          <div key={card.label} className="stat-card" style={{ borderTopColor: card.color }}>
            <div className="stat-icon">{card.icon}</div>
            <div className="stat-value" style={{ color: card.color }}>{card.value ?? 0}</div>
            <div className="stat-label">{card.label}</div>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div className="charts-grid">
        <div className="chart-card">
          <h3>Hearings by Status</h3>
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie data={statusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                {statusData.map((entry, i) => (
                  <Cell key={i} fill={entry.fill} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Hearings by Priority</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={priorityData} margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <Tooltip
                contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }}
                labelStyle={{ color: '#f1f5f9' }}
              />
              <Bar dataKey="value" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Upcoming reminders teaser */}
      {summary.within24Hours > 0 && (
        <div className="alert alert-urgent">
          🚨 <strong>{summary.within24Hours}</strong> hearing(s) due within the next 24 hours!&nbsp;
          <span
            className="link"
            style={{ cursor: 'pointer', textDecoration: 'underline' }}
            onClick={() => navigate('/reminders')}
          >
            View reminders →
          </span>
        </div>
      )}
    </div>
  );
}
