import React, { useCallback, useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Table, TableHead, TableRow, TableCell, TableBody,
  TablePagination, Chip, CircularProgress, TextField, MenuItem
} from '@mui/material';
import activityLogService from '../../services/activityLogService';
import { toast } from 'react-toastify';

const ACTION_COLORS = {
  CREATE: 'success',
  UPDATE: 'info',
  DELETE: 'error',
  RESTORE: 'warning',
  LOGIN: 'default',
  REGISTER: 'default',
  PROGRESS_UPDATE: 'primary',
};

const ENTITY_TYPES = ['EMPLOYEE', 'PROJECT', 'TASK', 'PROJECT_ATTACHMENT', 'AUTH'];

const ActivityLog = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [entityType, setEntityType] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalElements, setTotalElements] = useState(0);

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    try {
      const res = await activityLogService.search({
        entityType: entityType || undefined,
        page, size: rowsPerPage,
      });
      setLogs(res.data.data.content);
      setTotalElements(res.data.data.totalElements);
    } catch (err) {
      toast.error('Failed to load activity log');
    } finally {
      setLoading(false);
    }
  }, [entityType, page, rowsPerPage]);

  useEffect(() => { fetchLogs(); }, [fetchLogs]);

  return (
    <Box className="page-container">
      <Typography variant="h5" fontWeight={700} mb={1}>Activity Log</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        A complete audit trail of who did what, and when — every create, update, delete, restore, and login is recorded here.
      </Typography>

      <Paper sx={{ p: 2, mb: 2 }}>
        <TextField
          select size="small" label="Entity Type" value={entityType}
          onChange={(e) => { setEntityType(e.target.value); setPage(0); }}
          sx={{ width: 220 }}
        >
          <MenuItem value="">All</MenuItem>
          {ENTITY_TYPES.map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
        </TextField>
      </Paper>

      <Paper>
        {loading ? (
          <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>
        ) : (
          <>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Timestamp</TableCell>
                  <TableCell>Actor</TableCell>
                  <TableCell>Action</TableCell>
                  <TableCell>Entity</TableCell>
                  <TableCell>Description</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {logs.map((log) => (
                  <TableRow key={log.id} hover>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{new Date(log.timestamp).toLocaleString()}</TableCell>
                    <TableCell>{log.actorUsername}</TableCell>
                    <TableCell>
                      <Chip size="small" label={log.action} color={ACTION_COLORS[log.action] || 'default'} />
                    </TableCell>
                    <TableCell>{log.entityType}{log.entityId ? ` #${log.entityId}` : ''}</TableCell>
                    <TableCell>{log.description}</TableCell>
                  </TableRow>
                ))}
                {logs.length === 0 && (
                  <TableRow><TableCell colSpan={5} align="center">No activity recorded yet.</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div" count={totalElements} page={page}
              onPageChange={(e, newPage) => setPage(newPage)} rowsPerPage={rowsPerPage}
              rowsPerPageOptions={[10, 20, 50]}
              onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
            />
          </>
        )}
      </Paper>
    </Box>
  );
};

export default ActivityLog;
