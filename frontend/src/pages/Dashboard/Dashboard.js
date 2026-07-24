import React, { useEffect, useState } from 'react';
import { Box, Grid, Paper, Typography, CircularProgress, List, ListItem, ListItemText, Chip, Divider } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import WorkIcon from '@mui/icons-material/Work';
import AssignmentIcon from '@mui/icons-material/Assignment';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useAuth } from '../../context/AuthContext';
import dashboardService from '../../services/dashboardService';
import { toast } from 'react-toastify';

const StatCard = ({ title, value, icon, color }) => (
  <Paper
  elevation={0}
  sx={{
    p: 3,
    display: 'flex',
    alignItems: 'center',
    gap: 2,
    height: '100%',
    bgcolor: (theme) =>
      theme.palette.mode === 'dark'
        ? '#1e293b'
        : '#ffffff',
  }}
>
    <Box sx={{ backgroundColor: color, borderRadius: '50%', p: 1.5, display: 'flex' }}>
      {icon}
    </Box>
    <Box>
      <Typography variant="h4" fontWeight={700}>{value ?? 0}</Typography>
      <Typography variant="body2" color="text.secondary">{title}</Typography>
    </Box>
  </Paper>
);

const Dashboard = () => {
  const { isAdmin, user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = isAdmin
          ? await dashboardService.getAdminDashboard()
          : await dashboardService.getEmployeeDashboard();
          console.log(response.data.data);
        setData(response.data.data);
      } catch (err) {
        toast.error('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [isAdmin]);

  if (loading) {
    return <Box display="flex" justifyContent="center" mt={10}><CircularProgress /></Box>;
  }

  return (
    <Box
  className="page-container"
  sx={{
    minHeight: "100vh",
    bgcolor: (theme) =>
      theme.palette.mode === "dark"
        ? "#111827"
        : "#f4f6f8",
    transition: "all .3s ease",
    p: 3,
  }}
>
<Typography
  variant="h5"
  fontWeight={400}
  sx={{  fontSize: "1.1rem",mb: 0.5 }}
>
  {isAdmin
    ? "👋 Welcome back, Admin ! "
    : "👋 Welcome back, Employee ! "}
</Typography>

<Typography
  variant="h4"
  fontWeight={500}
  sx={{  fontSize: "2rem",mb: 3 }}
>
  {isAdmin ? "Admin Dashboard" : "My Dashboard"}
</Typography>

      {isAdmin ? (
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard title="Total Employees" value={data?.totalEmployees} icon={<PeopleIcon sx={{ color: '#fff' }} />} color="#1976d2" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard title="Total Projects" value={data?.totalProjects} icon={<WorkIcon sx={{ color: '#fff' }} />} color="#ff9800" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard title="Total Tasks" value={data?.totalTasks} icon={<AssignmentIcon sx={{ color: '#fff' }} />} color="#9c27b0" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatCard title="Completed Tasks" value={data?.completedTasks} icon={<CheckCircleIcon sx={{ color: '#fff' }} />} color="#2e7d32" />
          </Grid>

          <Grid item xs={12} md={6}>
            <Paper
  elevation={0}
  sx={{
    p: 3,
    bgcolor: (theme) =>
      theme.palette.mode === 'dark'
        ? '#1e293b'
        : '#ffffff',
  }}
>
              <Typography variant="h6" gutterBottom>Project Status</Typography>
              <Typography>Active Projects: <b>{data?.activeProjects}</b></Typography>
              <Typography>Completed Projects: <b>{data?.completedProjects}</b></Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper
  elevation={0}
  sx={{
    p: 3,
    bgcolor: (theme) =>
      theme.palette.mode === 'dark'
        ? '#1e293b'
        : '#ffffff',
  }}
>
              <Typography variant="h6" gutterBottom>Task Status</Typography>
              <Typography>Pending Tasks: <b>{data?.pendingTasks}</b></Typography>
              <Typography>Completed Tasks: <b>{data?.completedTasks}</b></Typography>
            </Paper>
          </Grid>
        </Grid>
      ) : (
        <Grid container spacing={3}>
          <Grid item xs={12} sm={4}>
            <StatCard title="Assigned Tasks" value={data?.assignedTasks?.length} icon={<AssignmentIcon sx={{ color: '#fff' }} />} color="#1976d2" />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatCard title="Completed Tasks" value={data?.completedTasks} icon={<CheckCircleIcon sx={{ color: '#fff' }} />} color="#2e7d32" />
          </Grid>
          <Grid item xs={12} sm={4}>
            <StatCard title="Upcoming Deadlines" value={data?.upcomingDeadlines?.length} icon={<WorkIcon sx={{ color: '#fff' }} />} color="#d32f2f" />
          </Grid>

          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>My Assigned Tasks</Typography>
              <List>
                {data?.assignedTasks?.length ? data.assignedTasks.map((t) => (
                  <React.Fragment key={t.id}>
                    <ListItem>
                      <ListItemText primary={t.title} secondary={`Project: ${t.projectName} | Progress: ${t.progress}%`} />
                      <Chip label={t.status} size="small" />
                    </ListItem>
                    <Divider component="li" />
                  </React.Fragment>
                )) : <Typography color="text.secondary">No tasks assigned yet.</Typography>}
              </List>
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>Upcoming Deadlines (7 days)</Typography>
              <List>
                {data?.upcomingDeadlines?.length ? data.upcomingDeadlines.map((t) => (
                  <React.Fragment key={t.id}>
                    <ListItem>
                      <ListItemText primary={t.title} secondary={`Due: ${t.dueDate}`} />
                      <Chip label={t.priority} color={t.priority === 'HIGH' ? 'error' : 'default'} size="small" />
                    </ListItem>
                    <Divider component="li" />
                  </React.Fragment>
                )) : <Typography color="text.secondary">No upcoming deadlines.</Typography>}
              </List>
            </Paper>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default Dashboard;
