import api from './api';

const projectService = {
  getAll: (params) => api.get('/projects', { params }),
  getById: (id) => api.get(`/projects/${id}`),
  create: (data) => api.post('/projects', data),
  update: (id, data) => api.put(`/projects/${id}`, data),
  delete: (id) => api.delete(`/projects/${id}`),
  assignEmployees: (id, employeeIds) => api.put(`/projects/${id}/assign-employees`, employeeIds),
  getDeleted: () => api.get('/projects/deleted'),
  restore: (id) => api.patch(`/projects/${id}/restore`),
};

export default projectService;
