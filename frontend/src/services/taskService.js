import api from './api';

const taskService = {
  getAll: (params) => api.get('/tasks', { params }),
  getById: (id) => api.get(`/tasks/${id}`),
  create: (data) => api.post('/tasks', data),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  updateProgress: (id, data) => api.patch(`/tasks/${id}/progress`, data),
  delete: (id) => api.delete(`/tasks/${id}`),
};

export default taskService;
