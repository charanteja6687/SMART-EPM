import React, { useState, useEffect, useCallback } from 'react';
import {
  AppBar, Toolbar, Typography, Drawer, List, ListItemButton, ListItemIcon,
  ListItemText, Box, IconButton, Avatar, Menu, MenuItem, Divider, Chip,
  Badge, Popover, ListItem, Button, Tooltip
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import WorkIcon from '@mui/icons-material/Work';
import AssignmentIcon from '@mui/icons-material/Assignment';
import LogoutIcon from '@mui/icons-material/Logout';
import AssessmentIcon from '@mui/icons-material/Assessment';
import HistoryIcon from '@mui/icons-material/History';
import NotificationsIcon from '@mui/icons-material/Notifications';
import Brightness4Icon from '@mui/icons-material/Brightness4';
import Brightness7Icon from '@mui/icons-material/Brightness7';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useThemeMode } from '../context/ThemeModeContext';
import notificationService from '../services/notificationService';
import GroupsIcon from '@mui/icons-material/Groups';

const drawerWidth = 240;

const Layout = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);
  const [notifAnchorEl, setNotifAnchorEl] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const { user, logout, isAdmin } = useAuth();
  const { mode, toggleMode } = useThemeMode();
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
    { text: 'Employees', icon: <PeopleIcon />, path: '/employees', adminOnly: true },
    { text: 'Projects', icon: <WorkIcon />, path: '/projects' },
    { text: 'Tasks', icon: <AssignmentIcon />, path: '/tasks' },
    { text: 'Reports', icon: <AssessmentIcon />, path: '/reports', adminOnly: true },
    { text: 'Activity Log', icon: <HistoryIcon />, path: '/activity-logs', adminOnly: true },
  ];

  const fetchUnreadCount = useCallback(async () => {
    try {
      const res = await notificationService.getUnreadCount();
      setUnreadCount(res.data.data.unreadCount);
    } catch (err) { /* non-fatal */ }
  }, []);

  useEffect(() => {
    fetchUnreadCount();
    // Poll every 30s so the badge stays reasonably fresh without a websocket
    const interval = setInterval(fetchUnreadCount, 30000);
    return () => clearInterval(interval);
  }, [fetchUnreadCount]);

  const openNotifications = async (e) => {
    setNotifAnchorEl(e.currentTarget);
    try {
      const res = await notificationService.getMyNotifications();
      setNotifications(res.data.data);
    } catch (err) { /* non-fatal */ }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setUnreadCount(0);
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (err) { /* non-fatal */ }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap sx={{ fontWeight: 700, color: 'primary.main' }}>
          Smart EPM
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.filter(item => !item.adminOnly || isAdmin).map((item) => (
     <ListItemButton
  key={item.text}
  selected={location.pathname === item.path}
  onClick={() => navigate(item.path)}
  sx={{
    mx: 1,
    my: 0.5,
    borderRadius: 2,
    color: (theme) =>
      theme.palette.mode === 'dark' ? '#ffffff' : '#000000',

    '& .MuiListItemIcon-root': {
      color: (theme) =>
        theme.palette.mode === 'dark' ? '#ffffff' : '#555',
      minWidth: 40,
    },

    '& .Mui-selected': {},

    '&.Mui-selected': {
      bgcolor: 'rgba(59,130,246,0.18)',
      color: '#38bdf8',

      '& .MuiListItemIcon-root': {
        color: '#38bdf8',
      },
    },

    '&:hover': {
      bgcolor: 'rgba(59,130,246,0.10)',
    },
  }}
>
  <ListItemIcon>
    {item.icon}
  </ListItemIcon>

  <ListItemText primary={item.text} />
</ListItemButton>
        ))}
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
    <AppBar
  position="fixed"
  elevation={0}
  sx={{
    zIndex: (theme) => theme.zIndex.drawer + 1,
    bgcolor: (theme) =>
      theme.palette.mode === "dark"
        ? "#111827"
        : theme.palette.primary.main,
    borderBottom: "1px solid rgba(56,189,248,0.25)",
boxShadow: "0 2px 10px rgba(56,189,248,0.15)",
  }}
>
        <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
         <Box sx={{ display: 'flex', alignItems: 'center' }}>
  <IconButton
    color="inherit"
    edge="start"
    onClick={() => setMobileOpen(!mobileOpen)}
    sx={{ mr: 2, display: { sm: 'none' } }}
  >
    <MenuIcon />
  </IconButton>

  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
    <GroupsIcon sx={{ color: '#38bdf8', fontSize: 30 }} />

    <Typography variant="h6" noWrap sx={{ fontWeight: 700 }}>
      Smart Employee &amp; Project Management
    </Typography>
  </Box>
</Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
<Avatar
  variant="rounded"
  sx={{
    px: 2,
    minWidth: user?.role === 'EMPLOYEE' ? 95 : 70,
    height: 34,
    fontSize: 13,
    fontWeight: 700,
    bgcolor: (theme) =>
      theme.palette.mode === 'dark'
        ? '#7c3aed'
        : '#2563eb',
    color: '#fff',
    borderRadius: '18px',
  }}
>
  {user?.role}
</Avatar>

            <Tooltip title={mode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}>
              <IconButton color="inherit" onClick={toggleMode}>
                {mode === 'dark' ? <Brightness7Icon /> : <Brightness4Icon />}
              </IconButton>
            </Tooltip>

            <IconButton color="inherit" onClick={openNotifications}>
              <Badge badgeContent={unreadCount} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
            <Popover
              open={Boolean(notifAnchorEl)}
              anchorEl={notifAnchorEl}
              onClose={() => setNotifAnchorEl(null)}
              anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              transformOrigin={{ vertical: 'top', horizontal: 'right' }}
            >
              <Box sx={{ width: 340, maxHeight: 420, overflowY: 'auto' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 1.5 }}>
                  <Typography variant="subtitle1" fontWeight={700}>Notifications</Typography>
                  {notifications.length > 0 && (
                    <Button size="small" onClick={handleMarkAllRead}>Mark all read</Button>
                  )}
                </Box>
                <Divider />
                <List dense>
                  {notifications.length === 0 ? (
                    <ListItem><ListItemText primary="No notifications yet." /></ListItem>
                  ) : notifications.map((n) => (
                    <ListItem key={n.id} sx={{ backgroundColor: n.read ? 'transparent' : 'action.hover' }}>
                      <ListItemText
                        primary={n.message}
                        secondary={new Date(n.createdAt).toLocaleString()}
                      />
                    </ListItem>
                  ))}
                </List>
              </Box>
            </Popover>

            <IconButton onClick={(e) => setAnchorEl(e.currentTarget)}>
              <Avatar
  sx={{
    width: 32,
    height: 32,
    bgcolor: (theme) =>
      theme.palette.mode === 'dark'
        ? '#7c3aed'
        : '#2563eb',
    color: '#fff',
  }}
>
                {user?.username?.charAt(0).toUpperCase()}
              </Avatar>
            </IconButton>
            <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
              <MenuItem disabled>{user?.username}</MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>
                <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
                Logout
              </MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': {
    width: drawerWidth,
    bgcolor: (theme) =>
      theme.palette.mode === "dark"
        ? "#0b1220"
        : "#ffffff",
    color: (theme) =>
      theme.palette.mode === "dark"
        ? "#ffffff"
        : "#000000",
    borderRight: "1px solid rgba(56,189,248,0.25)",
boxShadow: "2px 0 10px rgba(56,189,248,0.15)",
}, }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': {
    width: drawerWidth,
    bgcolor: (theme) =>
      theme.palette.mode === "dark"
        ? "#0b1220"
        : "#ffffff",
    color: (theme) =>
      theme.palette.mode === "dark"
        ? "#ffffff"
        : "#000000",
  borderRight: "1px solid rgba(56,189,248,0.25)",
boxShadow: "2px 0 10px rgba(56,189,248,0.15)",
}, }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, p: 0, width: { sm: `calc(100% - ${drawerWidth}px)` } }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};

export default Layout;
