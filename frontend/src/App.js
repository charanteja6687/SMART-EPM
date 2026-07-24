import React, { useMemo } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './index.css';

import { AuthProvider } from './context/AuthContext';
import { ThemeModeProvider, useThemeMode } from './context/ThemeModeContext';
import PrivateRoute from './routes/PrivateRoute';
import Layout from './components/Layout';
import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';
import ForgotPassword from './pages/Auth/ForgotPassword';
import Dashboard from './pages/Dashboard/Dashboard';
import Employees from './pages/Employees/Employees';
import Projects from './pages/Projects/Projects';
import Tasks from './pages/Tasks/Tasks';
import Reports from './pages/Reports/Reports';
import ActivityLog from './pages/ActivityLog/ActivityLog';

const getTheme = (mode) =>
  createTheme({
    palette: {
      mode,
      primary: {
        main: '#3b82f6',
      },
      secondary: {
        main: '#8b5cf6',
      },

      background:
        mode === 'dark'
          ? {
              default: '#0f172a', // page background
              paper: '#1e293b',   // cards
            }
          : {
              default: '#f4f6f8',
              paper: '#ffffff',
            },
    },

    shape: {
      borderRadius: 14,
    },
  });

  
const ThemedApp = () => {
  const { mode } = useThemeMode();
  const theme = useMemo(() => getTheme(mode), [mode]);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />

            <Route element={<PrivateRoute><Layout /></PrivateRoute>}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/employees" element={<PrivateRoute roles={['ADMIN']}><Employees /></PrivateRoute>} />
              <Route path="/projects" element={<Projects />} />
              <Route path="/tasks" element={<Tasks />} />
              <Route path="/reports" element={<PrivateRoute roles={['ADMIN']}><Reports /></PrivateRoute>} />
              <Route path="/activity-logs" element={<PrivateRoute roles={['ADMIN']}><ActivityLog /></PrivateRoute>} />
            </Route>

            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </BrowserRouter>
        <ToastContainer position="top-right" autoClose={3000} theme={mode} />
      </AuthProvider>
    </ThemeProvider>
  );
};

function App() {
  return (
    <ThemeModeProvider>
      <ThemedApp />
    </ThemeModeProvider>
  );
}

export default App;
