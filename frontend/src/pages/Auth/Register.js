import React, { useState } from 'react';
import { Box, Paper, TextField, Button, Typography, Link, Alert, CircularProgress, MenuItem } from '@mui/material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { keyframes } from "@mui/system";
import GroupsIcon from "@mui/icons-material/Groups";

const gradientAnimation = keyframes`
  0% {
    background-position: 0% 50%;
  }
  25% {
    background-position: 100% 50%;
  }
  50% {
    background-position: 100% 100%;
  }
  75% {
    background-position: 0% 100%;
  }
  100% {
    background-position: 0% 50%;
  }
`;

const floating = keyframes`
  0% {
    transform: translateX(-10px);
  }
  50% {
    transform: translateX(10px);
  }
  100% {
    transform: translateX(-10px);
  }
`;

const glow = keyframes`
  0% {
    filter: drop-shadow(0 0 5px #38bdf8);
  }
  50% {
    filter: drop-shadow(0 0 25px #38bdf8);
  }
  100% {
    filter: drop-shadow(0 0 5px #38bdf8);
  }
`;

const Register = () => {
  // No "Employee ID" field — when registering as EMPLOYEE, the backend automatically
  // finds or creates the matching Employee record by email. Nothing to type here.
  const [form, setForm] = useState({ username: '', email: '', password: '', role: 'EMPLOYEE' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.username || !form.email || !form.password) {
      setError('Please fill in all required fields');
      return;
    }
    setLoading(true);
    try {
      await register(form);
      toast.success('Registration successful!');
      navigate('/dashboard');
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed.';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

 return (
<Box
  sx={{
    display: "flex",
    justifyContent: "space-evenly",
    alignItems: "center",
    minHeight: "100vh",
    px: 8,

    background: `
      radial-gradient(circle at 20% 20%, rgba(59,130,246,.5), transparent 35%),
      radial-gradient(circle at 80% 30%, rgba(147,51,234,.5), transparent 35%),
      radial-gradient(circle at 30% 80%, rgba(6,182,212,.5), transparent 35%),
      #0f172a
    `,

    backgroundSize: "250% 250%",
    animation: `${gradientAnimation} 18s ease infinite`,
  }}
>
  <Box
  sx={{
    color: "white",
    maxWidth: 380,
  }}
>
  <GroupsIcon
    sx={{
      fontSize: 90,
      color: "#38bdf8",
      mr: 2,
      animation: `${glow} 2s ease-in-out infinite`,
    }}
  />

  <Typography
    variant="h2"
    fontWeight="bold"
    color="#4FC3F7"
  >
    Smart EPM
  </Typography>

  <Typography
    variant="h5"
    mt={2}
    fontWeight={600}
  >
    Employee & Project
    <br />
    Management System
  </Typography>

  <Typography
    mt={3}
    fontSize={18}
    color="#d1d5db"
  >
    Create your Smart EPM account to manage employees, projects and tasks.
  </Typography>

  <Box mt={5}>
    <Typography mb={2}>✔ Employee Management</Typography>
    <Typography mb={2}>✔ Project Tracking</Typography>
    <Typography mb={2}>✔ Task Assignment</Typography>
    <Typography>✔ Real-time Reports</Typography>
  </Box>
</Box>

      <Paper
  elevation={8}
  sx={{
    p: 5,
    width: 430,
    borderRadius: 4,
    animation: `${floating} 6s ease-in-out infinite`,
  }}
>
        <Typography variant="h5" fontWeight={700} textAlign="center" gutterBottom>
          Create Account
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <form onSubmit={handleSubmit}>
          <TextField fullWidth label="Username" name="username" margin="normal" value={form.username} onChange={handleChange} />
          <TextField fullWidth label="Email" name="email" type="email" margin="normal" value={form.email} onChange={handleChange} />
          <TextField fullWidth label="Password" name="password" type="password" margin="normal" value={form.password} onChange={handleChange} />
          <TextField select fullWidth label="Role" name="role" margin="normal" value={form.role} onChange={handleChange}>
            <MenuItem value="EMPLOYEE">Employee</MenuItem>
            <MenuItem value="ADMIN">Admin</MenuItem>
          </TextField>
         <Button
  type="submit"
  variant="contained"
  sx={{
    width: 180,  
    mx: "auto",
    display: "block",
    mt: 3,
    mb: 2,
  }}
>
  REGISTER
</Button>
        </form>

        <Typography variant="body2" textAlign="center">
          Already have an account? <Link component={RouterLink} to="/login">Login</Link>
        </Typography>
      </Paper>
    </Box>
  );
};

export default Register;
