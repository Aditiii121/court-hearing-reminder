import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import HearingList from './pages/HearingList';
import AddHearing from './pages/AddHearing';
import EditHearing from './pages/EditHearing';
import HearingDetails from './pages/HearingDetails';
import RemindersPage from './pages/RemindersPage';
import ClientDashboard from './pages/ClientDashboard';
import ClientHearingList from './pages/ClientHearingList';
import ClientHearingDetails from './pages/ClientHearingDetails';
import ClientRemindersPage from './pages/ClientRemindersPage';

/** Redirects to the correct home page based on role. */
function HomeRedirect() {
  const { user, isClient } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={isClient() ? '/client/dashboard' : '/dashboard'} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster
          position="top-right"
          toastOptions={{
            style: {
              background: '#1e293b',
              color: '#f1f5f9',
              border: '1px solid #334155',
              borderRadius: '10px',
            },
          }}
        />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<Layout />}>
            {/* Smart home redirect */}
            <Route path="/" element={<HomeRedirect />} />

            {/* Staff routes (ADMIN / LAWYER / CLERK) */}
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/hearings" element={<HearingList />} />
            <Route path="/hearings/add" element={<AddHearing />} />
            <Route path="/hearings/:id" element={<HearingDetails />} />
            <Route path="/hearings/:id/edit" element={<EditHearing />} />
            <Route path="/reminders" element={<RemindersPage />} />

            {/* Client routes */}
            <Route path="/client/dashboard" element={<ClientDashboard />} />
            <Route path="/client/hearings" element={<ClientHearingList />} />
            <Route path="/client/hearings/:id" element={<ClientHearingDetails />} />
            <Route path="/client/reminders" element={<ClientRemindersPage />} />
          </Route>
          <Route path="*" element={<HomeRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
