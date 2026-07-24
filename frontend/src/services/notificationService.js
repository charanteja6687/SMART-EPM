import api from './api';

const notificationService = {
  getMyNotifications: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAllAsRead: () => api.patch('/notifications/mark-all-read'),
  markAsRead: (id) => api.patch(`/notifications/${id}/read`),
};

export default notificationService;
