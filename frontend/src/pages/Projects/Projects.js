import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, Button, TextField, Paper, Table, TableHead, TableRow, TableCell,
  TableBody, TablePagination, IconButton, Dialog, DialogTitle, DialogContent, DialogActions,
  Grid, MenuItem, Chip, CircularProgress, LinearProgress, Select, InputLabel, FormControl, OutlinedInput
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import projectService from '../../services/projectService';
import employeeService from '../../services/employeeService';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';

const emptyForm = { name: '', description: '', status: 'ACTIVE', priority: 'MEDIUM', startDate: '', deadline: '', employeeIds: [] };
const STATUS_OPTIONS = ['ACTIVE', 'COMPLETED', 'ON_HOLD', 'CANCELLED'];
const PRIORITY_OPTIONS = ['HIGH', 'MEDIUM', 'LOW'];

const priorityColor = (p) => p === 'HIGH' ? 'error' : p === 'MEDIUM' ? 'warning' : 'default';
const statusColor = (s) => s === 'ACTIVE' ? 'primary' : s === 'COMPLETED' ? 'success' : 'default';

const Projects = () => {
  const { isAdmin } = useAuth();
  const [projects, setProjects] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);

  // Note on role-based access: this page calls the SAME /api/projects endpoint for both
  // Admin and Employee users. The backend (ProjectServiceImpl.searchProjects) automatically
  // restricts results to "my assigned projects only" when the caller is an EMPLOYEE — the
  // frontend never needs to (and cannot) request "all projects" as an employee.
  const fetchProjects = useCallback(async () => {
    setLoading(true);
    try {
      const res = await projectService.getAll({
        keyword: keyword || undefined, status: status || undefined, priority: priority || undefined,
        page, size: rowsPerPage, sortBy: 'id', direction: 'ASC'
      });
      setProjects(res.data.data.content);
      setTotalElements(res.data.data.totalElements);
    } catch (err) {
      toast.error('Failed to load projects');
    } finally {
      setLoading(false);
    }
  }, [keyword, status, priority, page, rowsPerPage]);

  const fetchEmployees = useCallback(async () => {
    try {
      const res = await employeeService.getAll({ page: 0, size: 100, sortBy: 'id', direction: 'ASC' });
      setEmployees(res.data.data.content);
    } catch (err) { /* ignore for non-admins without access */ }
  }, []);

  useEffect(() => { fetchProjects(); }, [fetchProjects]);
  useEffect(() => { if (isAdmin) fetchEmployees(); }, [fetchEmployees, isAdmin]);

  const openCreate = () => { setForm(emptyForm); setEditingId(null); setOpen(true); };
  const openEdit = (p) => {
    setForm({
      name: p.name, description: p.description || '', status: p.status, priority: p.priority,
      startDate: p.startDate || '', deadline: p.deadline || '',
      employeeIds: p.employees ? p.employees.map(e => e.id) : []
    });
    setEditingId(p.id);
    setOpen(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async () => {
    try {
      if (editingId) {
        await projectService.update(editingId, form);
        toast.success('Project updated successfully');
      } else {
        await projectService.create(form);
        toast.success('Project created successfully');
      }
      setOpen(false);
      fetchProjects();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id) => {
    try {
      await projectService.delete(id);
      toast.success('Project deleted successfully');
      fetchProjects();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    } finally {
      setConfirmDeleteId(null);
    }
  };

  return (
    <Box className="page-container">
      <Box className="flex-between">
        <Typography variant="h5" fontWeight={700}>Projects</Typography>
        {isAdmin && <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>New Project</Button>}
      </Box>

      <Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField size="small" label="Search by name" value={keyword} onChange={(e) => { setKeyword(e.target.value); setPage(0); }} sx={{ flex: 1, minWidth: 200 }} />
        <TextField select size="small" label="Status" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }} sx={{ width: 160 }}>
          <MenuItem value="">All</MenuItem>
          {STATUS_OPTIONS.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
        </TextField>
        <TextField select size="small" label="Priority" value={priority} onChange={(e) => { setPriority(e.target.value); setPage(0); }} sx={{ width: 160 }}>
          <MenuItem value="">All</MenuItem>
          {PRIORITY_OPTIONS.map(p => <MenuItem key={p} value={p}>{p}</MenuItem>)}
        </TextField>
      </Paper>

      <Paper>
        {loading ? (
          <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>
        ) : (
          <>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Deadline</TableCell>
                  <TableCell>Progress</TableCell>
                  {isAdmin && <TableCell align="right">Actions</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>
                {projects.map((p) => (
                  <TableRow key={p.id} hover>
                    <TableCell>{p.name}</TableCell>
                    <TableCell><Chip size="small" label={p.status} color={statusColor(p.status)} /></TableCell>
                    <TableCell><Chip size="small" label={p.priority} color={priorityColor(p.priority)} /></TableCell>
                    <TableCell>{p.deadline || '-'}</TableCell>
                    <TableCell sx={{ width: 180 }}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <LinearProgress variant="determinate" value={p.progressPercent || 0} sx={{ flex: 1, height: 8, borderRadius: 4 }} />
                        <Typography variant="caption">{Math.round(p.progressPercent || 0)}%</Typography>
                      </Box>
                    </TableCell>
                    {isAdmin && (
                      <TableCell align="right">
                        <IconButton onClick={() => openEdit(p)}><EditIcon fontSize="small" /></IconButton>
                        <IconButton onClick={() => setConfirmDeleteId(p.id)}><DeleteIcon fontSize="small" color="error" /></IconButton>
                      </TableCell>
                    )}
                  </TableRow>
                ))}
                {projects.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={isAdmin ? 6 : 5} align="center">
                      {isAdmin ? 'No projects found' : 'No tasks/projects are assigned to you'}
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div" count={totalElements} page={page}
              onPageChange={(e, newPage) => setPage(newPage)} rowsPerPage={rowsPerPage}
              onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
            />
          </>
        )}
      </Paper>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Project' : 'New Project'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField fullWidth label="Project Name" name="name" value={form.name} onChange={handleChange} required />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth multiline rows={3} label="Description" name="description" value={form.description} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth label="Status" name="status" value={form.status} onChange={handleChange}>
                {STATUS_OPTIONS.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth label="Priority" name="priority" value={form.priority} onChange={handleChange}>
                {PRIORITY_OPTIONS.map(p => <MenuItem key={p} value={p}>{p}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Start Date" name="startDate" type="date" InputLabelProps={{ shrink: true }} value={form.startDate} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Deadline" name="deadline" type="date" InputLabelProps={{ shrink: true }} value={form.deadline} onChange={handleChange} />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Assign Employees</InputLabel>
                <Select
                  multiple
                  value={form.employeeIds}
                  onChange={(e) => setForm({ ...form, employeeIds: e.target.value })}
                  input={<OutlinedInput label="Assign Employees" />}
                  renderValue={(selected) => employees.filter(e => selected.includes(e.id)).map(e => e.fullName).join(', ')}
                >
                  {employees.map((emp) => (
                    <MenuItem key={emp.id} value={emp.id}>{emp.fullName} ({emp.department})</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>{editingId ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!confirmDeleteId} onClose={() => setConfirmDeleteId(null)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>Are you sure you want to delete this project? All its tasks will also be removed.</DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDeleteId(null)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={() => handleDelete(confirmDeleteId)}>Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Projects;
