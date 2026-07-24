import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, Button, TextField, Paper, Table, TableHead, TableRow, TableCell,
  TableBody, TablePagination, IconButton, Dialog, DialogTitle, DialogContent, DialogActions,
  Grid, MenuItem, Chip, CircularProgress, Slider
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import UpdateIcon from '@mui/icons-material/Update';
import taskService from '../../services/taskService';
import projectService from '../../services/projectService';
import employeeService from '../../services/employeeService';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';

const emptyForm = { title: '', description: '', status: 'TODO', priority: 'MEDIUM', progress: 0, dueDate: '', remarks: '', projectId: '', employeeId: '' };
const STATUS_OPTIONS = ['TODO', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED'];
const PRIORITY_OPTIONS = ['HIGH', 'MEDIUM', 'LOW'];

const statusColor = (s) => ({ TODO: 'default', IN_PROGRESS: 'info', COMPLETED: 'success', BLOCKED: 'error' }[s] || 'default');
const priorityColor = (p) => p === 'HIGH' ? 'error' : p === 'MEDIUM' ? 'warning' : 'default';

const Tasks = () => {
  const { isAdmin } = useAuth();
  const [tasks, setTasks] = useState([]);
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
  const [progressDialog, setProgressDialog] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);

  const fetchTasks = useCallback(async () => {
    setLoading(true);
    try {
      const res = await taskService.getAll({
        keyword: keyword || undefined, status: status || undefined, priority: priority || undefined,
        page, size: rowsPerPage, sortBy: 'id', direction: 'ASC'
      });
      setTasks(res.data.data.content);
      setTotalElements(res.data.data.totalElements);
    } catch (err) {
      toast.error('Failed to load tasks');
    } finally {
      setLoading(false);
    }
  }, [keyword, status, priority, page, rowsPerPage]);

  const fetchLookups = useCallback(async () => {
    try {
      const [projRes, empRes] = await Promise.all([
        projectService.getAll({ page: 0, size: 100 }),
        isAdmin ? employeeService.getAll({ page: 0, size: 100 }) : Promise.resolve({ data: { data: { content: [] } } })
      ]);
      setProjects(projRes.data.data.content);
      setEmployees(empRes.data.data.content);
    } catch (err) { /* non-fatal */ }
  }, [isAdmin]);

  useEffect(() => { fetchTasks(); }, [fetchTasks]);
  useEffect(() => { fetchLookups(); }, [fetchLookups]);

  const openCreate = () => { setForm(emptyForm); setEditingId(null); setOpen(true); };
  const openEdit = (t) => {
    setForm({
      title: t.title, description: t.description || '', status: t.status, priority: t.priority,
      progress: t.progress, dueDate: t.dueDate || '', remarks: t.remarks || '',
      projectId: t.projectId, employeeId: t.employeeId || ''
    });
    setEditingId(t.id);
    setOpen(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async () => {
    try {
      const payload = { ...form, employeeId: form.employeeId || null, projectId: Number(form.projectId) };
      if (editingId) {
        await taskService.update(editingId, payload);
        toast.success('Task updated successfully');
      } else {
        await taskService.create(payload);
        toast.success('Task created successfully');
      }
      setOpen(false);
      fetchTasks();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id) => {
    try {
      await taskService.delete(id);
      toast.success('Task deleted successfully');
      fetchTasks();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    } finally {
      setConfirmDeleteId(null);
    }
  };

  const handleProgressUpdate = async () => {
    try {
      await taskService.updateProgress(progressDialog.id, {
        progress: progressDialog.progress, status: progressDialog.status, remarks: progressDialog.remarks
      });
      toast.success('Progress updated successfully');
      setProgressDialog(null);
      fetchTasks();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed');
    }
  };

  return (
    <Box className="page-container">
      <Box className="flex-between">
        <Typography variant="h5" fontWeight={700}>Tasks</Typography>
        {isAdmin && <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>New Task</Button>}
      </Box>

      <Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField size="small" label="Search by title" value={keyword} onChange={(e) => { setKeyword(e.target.value); setPage(0); }} sx={{ flex: 1, minWidth: 200 }} />
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
                  <TableCell>Title</TableCell>
                  <TableCell>Project</TableCell>
                  <TableCell>Assigned To</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Progress</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {tasks.map((t) => (
                  <TableRow key={t.id} hover>
                    <TableCell>{t.title}</TableCell>
                    <TableCell>{t.projectName}</TableCell>
                    <TableCell>{t.employeeName || 'Unassigned'}</TableCell>
                    <TableCell><Chip size="small" label={t.status} color={statusColor(t.status)} /></TableCell>
                    <TableCell><Chip size="small" label={t.priority} color={priorityColor(t.priority)} /></TableCell>
                    <TableCell>{t.progress}%</TableCell>
                    <TableCell align="right">
                      <IconButton onClick={() => setProgressDialog({ id: t.id, progress: t.progress, status: t.status, remarks: t.remarks || '' })}>
                        <UpdateIcon fontSize="small" color="primary" />
                      </IconButton>
                      {isAdmin && (
                        <>
                          <IconButton onClick={() => openEdit(t)}><EditIcon fontSize="small" /></IconButton>
                          <IconButton onClick={() => setConfirmDeleteId(t.id)}><DeleteIcon fontSize="small" color="error" /></IconButton>
                        </>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
                {tasks.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      {isAdmin ? 'No tasks found' : 'No tasks/projects are assigned to you'}
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

      {/* Create/Edit dialog (Admin) */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Task' : 'New Task'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField fullWidth label="Title" name="title" value={form.title} onChange={handleChange} required />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth multiline rows={2} label="Description" name="description" value={form.description} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth label="Project" name="projectId" value={form.projectId} onChange={handleChange} required>
                {projects.map(p => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth label="Assign To" name="employeeId" value={form.employeeId} onChange={handleChange}>
                <MenuItem value="">Unassigned</MenuItem>
                {employees.map(e => <MenuItem key={e.id} value={e.id}>{e.fullName}</MenuItem>)}
              </TextField>
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
              <TextField fullWidth label="Due Date" name="dueDate" type="date" InputLabelProps={{ shrink: true }} value={form.dueDate} onChange={handleChange} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Remarks" name="remarks" value={form.remarks} onChange={handleChange} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>{editingId ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      {/* Progress update dialog (Employee/Admin) */}
      <Dialog open={!!progressDialog} onClose={() => setProgressDialog(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Update Task Progress</DialogTitle>
        <DialogContent>
          {progressDialog && (
            <>
              <Typography gutterBottom>Progress: {progressDialog.progress}%</Typography>
              <Slider
                value={progressDialog.progress}
                onChange={(e, val) => setProgressDialog({ ...progressDialog, progress: val })}
                valueLabelDisplay="auto"
              />
              <TextField
                select fullWidth label="Status" sx={{ mt: 2 }}
                value={progressDialog.status}
                onChange={(e) => setProgressDialog({ ...progressDialog, status: e.target.value })}
              >
                {STATUS_OPTIONS.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
              </TextField>
              <TextField
                fullWidth multiline rows={2} label="Remarks" sx={{ mt: 2 }}
                value={progressDialog.remarks}
                onChange={(e) => setProgressDialog({ ...progressDialog, remarks: e.target.value })}
              />
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setProgressDialog(null)}>Cancel</Button>
          <Button variant="contained" onClick={handleProgressUpdate}>Save</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!confirmDeleteId} onClose={() => setConfirmDeleteId(null)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>Are you sure you want to delete this task?</DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDeleteId(null)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={() => handleDelete(confirmDeleteId)}>Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Tasks;
