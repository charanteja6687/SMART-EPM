import React, { useState } from 'react';
import { Box, Typography, Paper, Grid, Button, CircularProgress } from '@mui/material';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import GridOnIcon from '@mui/icons-material/GridOn';
import reportService from '../../services/reportService';
import { toast } from 'react-toastify';

const REPORTS = [
  {
    key: 'empTaskPdf',
    title: 'Employee-wise Task Report (PDF)',
    description: 'Per-employee breakdown of total, completed, and pending tasks.',
    icon: <PictureAsPdfIcon sx={{ color: '#fff' }} />,
    color: '#d32f2f',
    action: () => reportService.downloadEmployeeTaskPdf(),
  },
  {
    key: 'empTaskExcel',
    title: 'Employee-wise Task Report (Excel)',
    description: 'Same report as above in .xlsx format for further analysis.',
    icon: <GridOnIcon sx={{ color: '#fff' }} />,
    color: '#2e7d32',
    action: () => reportService.downloadEmployeeTaskExcel(),
  },
  {
    key: 'projectProgress',
    title: 'Project Progress Report (Excel)',
    description: 'Status, priority, deadline, and completion % for every project.',
    icon: <GridOnIcon sx={{ color: '#fff' }} />,
    color: '#1976d2',
    action: () => reportService.downloadProjectProgressExcel(),
  },
  {
    key: 'pendingTasks',
    title: 'Pending Tasks Report (Excel)',
    description: 'All tasks that are not yet marked COMPLETED, across all projects.',
    icon: <GridOnIcon sx={{ color: '#fff' }} />,
    color: '#ff9800',
    action: () => reportService.downloadPendingTasksExcel(),
  },
];

const Reports = () => {
  const [loadingKey, setLoadingKey] = useState(null);

  const handleDownload = async (report) => {
    setLoadingKey(report.key);
    try {
      await report.action();
      toast.success('Report downloaded successfully');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to download report');
    } finally {
      setLoadingKey(null);
    }
  };

  return (
    <Box className="page-container">
      <Typography variant="h5" fontWeight={700} mb={1}>Reports</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Download reports as PDF or Excel. Files are saved to your browser's default downloads folder.
      </Typography>

      <Grid container spacing={3}>
        {REPORTS.map((report) => (
          <Grid item xs={12} sm={6} key={report.key}>
            <Paper elevation={2} sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
              <Box display="flex" alignItems="center" gap={2} mb={2}>
                <Box sx={{ backgroundColor: report.color, borderRadius: '50%', p: 1.5, display: 'flex' }}>
                  {report.icon}
                </Box>
                <Typography variant="h6">{report.title}</Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ flexGrow: 1, mb: 2 }}>
                {report.description}
              </Typography>
              <Button
                variant="contained"
                onClick={() => handleDownload(report)}
                disabled={loadingKey === report.key}
                sx={{ alignSelf: 'flex-start' }}
              >
                {loadingKey === report.key ? <CircularProgress size={22} color="inherit" /> : 'Download'}
              </Button>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default Reports;
