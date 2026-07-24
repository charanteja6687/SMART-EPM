import api from './api';

const downloadFile = async (url, filename) => {
  const response = await api.get(url, { responseType: 'blob' });
  const blobUrl = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = blobUrl;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
};

const reportService = {
  downloadEmployeeTaskPdf: () => downloadFile('/reports/employee-tasks/pdf', 'employee_task_report.pdf'),
  downloadEmployeeTaskExcel: () => downloadFile('/reports/employee-tasks/excel', 'employee_task_report.xlsx'),
  downloadProjectProgressExcel: () => downloadFile('/reports/project-progress/excel', 'project_progress_report.xlsx'),
  downloadPendingTasksExcel: () => downloadFile('/reports/pending-tasks/excel', 'pending_tasks_report.xlsx'),
};

export default reportService;
