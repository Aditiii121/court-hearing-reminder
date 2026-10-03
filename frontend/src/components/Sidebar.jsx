import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/hearings', label: 'Hearings', icon: '⚖️' },
  { to: '/hearings/add', label: 'Add Hearing', icon: '➕', roles: ['ADMIN', 'LAWYER'] },
  { to: '/reminders', label: 'Reminders', icon: '🔔' },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const visibleItems = NAV_ITEMS.filter(
    (item) => !item.roles || (user && item.roles.includes(user.role))
  );

  return (
    <aside className="sidebar" aria-label="Navigation">
      <div className="sidebar-logo">
        <span className="logo-icon">⚖️</span>
        <span className="logo-text">CourtReminder</span>
      </div>

      <nav className="sidebar-nav">
        {visibleItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            end={item.to === '/dashboard'}
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
