import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, Button, TextField, Paper, Table, TableHead, TableRow, TableCell,
  TableBody, TablePagination, IconButton, Dialog, DialogTitle, DialogContent, DialogActions,
  Grid, MenuItem, Chip, CircularProgress, InputAdornment
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';
import RestoreFromTrashIcon from '@mui/icons-material/RestoreFromTrash';
import RestoreIcon from '@mui/icons-material/Restore';
import employeeService from '../../services/employeeService';
import { toast } from 'react-toastify';

const emptyForm = { fullName: '', email: '', phone: '', department: '', designation: '', salary: '', dateOfJoining: '', active: true };

const Employees = () => {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [department, setDepartment] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);
  const [deletedDialogOpen, setDeletedDialogOpen] = useState(false);
  const [deletedEmployees, setDeletedEmployees] = useState([]);
  const [deletedLoading, setDeletedLoading] = useState(false);

  const fetchEmployees = useCallback(async () => {
    setLoading(true);
    try {
      const res = await employeeService.getAll({
        keyword: keyword || undefined,
        department: department || undefined,
        page, size: rowsPerPage, sortBy: 'id', direction: 'ASC'
      });
      setEmployees(res.data.data.content);
      setTotalElements(res.data.data.totalElements);
    } catch (err) {
      toast.error('Failed to load employees');
    } finally {
      setLoading(false);
    }
  }, [keyword, department, page, rowsPerPage]);

  useEffect(() => { fetchEmployees(); }, [fetchEmployees]);

  const openCreate = () => { setForm(emptyForm); setEditingId(null); setOpen(true); };
  const openEdit = (emp) => {
    setForm({ ...emp, salary: emp.salary ?? '', dateOfJoining: emp.dateOfJoining ?? '' });
    setEditingId(emp.id);
    setOpen(true);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async () => {
    try {
      const payload = { ...form, salary: form.salary === '' ? null : Number(form.salary) };
      if (editingId) {
        await employeeService.update(editingId, payload);
        toast.success('Employee updated successfully');
      } else {
        await employeeService.create(payload);
        toast.success('Employee created successfully');
      }
      setOpen(false);
      fetchEmployees();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id) => {
    try {
      await employeeService.delete(id);
      toast.success('Employee deleted successfully');
      fetchEmployees();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    } finally {
      setConfirmDeleteId(null);
    }
  };

  const openDeletedDialog = async () => {
    setDeletedDialogOpen(true);
    setDeletedLoading(true);
    try {
      const res = await employeeService.getDeleted();
      setDeletedEmployees(res.data.data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load deleted employees');
    } finally {
      setDeletedLoading(false);
    }
  };

  const handleRestore = async (id) => {
    try {
      await employeeService.restore(id);
      toast.success('Employee restored successfully');
      setDeletedEmployees((prev) => prev.filter((e) => e.id !== id));
      fetchEmployees();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Restore failed');
    }
  };

  return (
    <Box className="page-container">
      <Box className="flex-between">
        <Typography variant="h5" fontWeight={700}>Employees</Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<RestoreFromTrashIcon />} onClick={openDeletedDialog}>
            View Deleted
          </Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Add Employee</Button>
        </Box>
      </Box>

      <Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 2 }}>
        <TextField
          size="small" label="Search by name or email" value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0); }}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
          sx={{ flex: 1 }}
        />
        <TextField
          size="small" label="Department" value={department}
          onChange={(e) => { setDepartment(e.target.value); setPage(0); }}
          sx={{ width: 200 }}
        />
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
                  <TableCell>Email</TableCell>
                  <TableCell>Department</TableCell>
                  <TableCell>Designation</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {employees.map((emp) => (
                  <TableRow key={emp.id} hover>
                    <TableCell>{emp.fullName}</TableCell>
                    <TableCell>{emp.email}</TableCell>
                    <TableCell>{emp.department}</TableCell>
                    <TableCell>{emp.designation}</TableCell>
                    <TableCell><Chip size="small" label={emp.active ? 'Active' : 'Inactive'} color={emp.active ? 'success' : 'default'} /></TableCell>
                    <TableCell align="right">
                      <IconButton onClick={() => openEdit(emp)}><EditIcon fontSize="small" /></IconButton>
                      <IconButton onClick={() => setConfirmDeleteId(emp.id)}><DeleteIcon fontSize="small" color="error" /></IconButton>
                    </TableCell>
                  </TableRow>
                ))}
                {employees.length === 0 && (
                  <TableRow><TableCell colSpan={6} align="center">No employees found</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={totalElements}
              page={page}
              onPageChange={(e, newPage) => setPage(newPage)}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
            />
          </>
        )}
      </Paper>

      {/* Create/Edit Dialog */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Employee' : 'Add Employee'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Full Name" name="fullName" value={form.fullName} onChange={handleChange} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Email" name="email" value={form.email} onChange={handleChange} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Phone" name="phone" value={form.phone} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Department" name="department" value={form.department} onChange={handleChange} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Designation" name="designation" value={form.designation} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Salary" name="salary" type="number" value={form.salary} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Date of Joining" name="dateOfJoining" type="date" InputLabelProps={{ shrink: true }} value={form.dateOfJoining} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth label="Status" name="active" value={form.active} onChange={handleChange}>
                <MenuItem value={true}>Active</MenuItem>
                <MenuItem value={false}>Inactive</MenuItem>
              </TextField>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>{editingId ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      {/* Delete confirmation */}
      <Dialog open={!!confirmDeleteId} onClose={() => setConfirmDeleteId(null)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>Are you sure you want to delete this employee?</DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDeleteId(null)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={() => handleDelete(confirmDeleteId)}>Delete</Button>
        </DialogActions>
      </Dialog>

      {/* Deleted employees — soft-delete restore panel */}
      <Dialog open={deletedDialogOpen} onClose={() => setDeletedDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Deleted Employees</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" mb={2}>
            These employees were soft-deleted — their records are kept for history/reports and can be restored at any time.
          </Typography>
          {deletedLoading ? (
            <Box display="flex" justifyContent="center" p={3}><CircularProgress /></Box>
          ) : deletedEmployees.length === 0 ? (
            <Typography color="text.secondary">No deleted employees.</Typography>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell align="right">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {deletedEmployees.map((emp) => (
                  <TableRow key={emp.id}>
                    <TableCell>{emp.fullName}</TableCell>
                    <TableCell>{emp.email}</TableCell>
                    <TableCell align="right">
                      <Button size="small" startIcon={<RestoreIcon />} onClick={() => handleRestore(emp.id)}>
                        Restore
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeletedDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Employees;
