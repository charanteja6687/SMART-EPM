import api from './api';

const activityLogService = {
  search: (params) => api.get('/activity-logs', { params }),
};

export default activityLogService;
