import api from './api';

const dashboardService = {
  getAdminDashboard: () => api.get('/dashboard/admin'),
  getEmployeeDashboard: () => api.get('/dashboard/employee'),
};

export default dashboardService;
