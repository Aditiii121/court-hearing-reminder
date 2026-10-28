import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Navigation items for staff roles (ADMIN, LAWYER, CLERK)
const STAFF_NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/hearings', label: 'Hearings', icon: '⚖️' },
  { to: '/hearings/add', label: 'Add Hearing', icon: '➕', roles: ['ADMIN', 'LAWYER'] },
  { to: '/reminders', label: 'Reminders', icon: '🔔' },
];

// Navigation items for CLIENT role
const CLIENT_NAV_ITEMS = [
  { to: '/client/dashboard', label: 'My Dashboard', icon: '📊' },
  { to: '/client/hearings', label: 'My Hearings', icon: '⚖️' },
  { to: '/client/reminders', label: 'My Reminders', icon: '🔔' },
];

export default function Sidebar() {
  const { user, logout, isClient } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = isClient() ? CLIENT_NAV_ITEMS : STAFF_NAV_ITEMS;

  const visibleItems = navItems.filter(
    (item) => !item.roles || (user && item.roles.includes(user.role))
  );

  return (
    <aside className="sidebar" aria-label="Navigation">
      <div className="sidebar-logo">
        <span className="logo-icon">⚖️</span>
        <span className="logo-text">CourtReminder</span>
      </div>

      {isClient() && (
        <div style={{
          padding: '0.5rem 1rem',
          fontSize: '0.7rem',
          color: 'var(--text-muted)',
          textTransform: 'uppercase',
          letterSpacing: '0.08em',
          borderBottom: '1px solid var(--border)',
          marginBottom: '0.25rem',
        }}>
          Client Portal
        </div>
      )}

      <nav className="sidebar-nav">
        {visibleItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            end={item.to === '/dashboard' || item.to === '/client/dashboard'}
          >
            <span className="nav-icon">{item.icon}</span>
            <span className="nav-label">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="user-info">
          <div className="user-avatar">{user?.fullName?.[0]?.toUpperCase() || '?'}</div>
          <div className="user-details">
            <span className="user-name">{user?.fullName || 'User'}</span>
            <span className="user-role">{user?.role || ''}</span>
          </div>
        </div>
        <button className="btn btn-danger logout-btn" onClick={handleLogout} id="logout-btn">
          Logout
        </button>
      </div>
    </aside>
  );
}
