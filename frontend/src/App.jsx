import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import HearingList from './pages/HearingList';
import AddHearing from './pages/AddHearing';
import EditHearing from './pages/EditHearing';
import HearingDetails from './pages/HearingDetails';
import RemindersPage from './pages/RemindersPage';

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
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/hearings" element={<HearingList />} />
            <Route path="/hearings/add" element={<AddHearing />} />
            <Route path="/hearings/:id" element={<HearingDetails />} />
            <Route path="/hearings/:id/edit" element={<EditHearing />} />
            <Route path="/reminders" element={<RemindersPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
