import api from './api';

const employeeService = {
  getAll: (params) => api.get('/employees', { params }),
  getById: (id) => api.get(`/employees/${id}`),
  create: (data) => api.post('/employees', data),
  update: (id, data) => api.put(`/employees/${id}`, data),
  delete: (id) => api.delete(`/employees/${id}`),
  getDeleted: () => api.get('/employees/deleted'),
  restore: (id) => api.patch(`/employees/${id}/restore`),
};

export default employeeService;
