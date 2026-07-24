import React, { useState } from 'react';
import { Box, Paper, TextField, Button, Typography, Link, Alert, CircularProgress } from '@mui/material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { keyframes } from '@mui/system';
import GroupsIcon from '@mui/icons-material/Groups';


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
0%{
transform:translateX(-10px);
}

50%{
transform:translateX(10px);
}

100%{
transform:translateX(-10px);
}
`;


const glow = keyframes`
0%{
filter:drop-shadow(0 0 5px #38bdf8);
}

50%{
filter:drop-shadow(0 0 25px #38bdf8);
}

100%{
filter:drop-shadow(0 0 5px #38bdf8);
}
`;


const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!username || !password) {
      setError('Please enter both username and password');
      return;
    }
    setLoading(true);
    try {
      await login(username, password);
      toast.success('Login successful!');
      navigate('/dashboard');
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please check your credentials.';
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
Management System
</Typography>

<Typography
mt={3}
fontSize={18}
color="#d1d5db"
>
Manage employees,
projects and tasks
efficiently.
</Typography>

<Box mt={5}>

<Typography mb={2}>
✔ Employee Management
</Typography>

<Typography mb={2}>
✔ Project Tracking
</Typography>

<Typography mb={2}>
✔ Task Assignment
</Typography>

<Typography>
✔ Real-time Reports
</Typography>

</Box>

</Box>
  
      <Paper
elevation={8}
sx={{
p:5,
width:430,
borderRadius:4,

animation:`${floating} 6s ease-in-out infinite`
}}
>
        <Typography variant="h5" fontWeight={700} textAlign="center" gutterBottom>
          Smart EPM Login
        </Typography>
        <Typography variant="body2" color="text.secondary" textAlign="center" mb={3}>
          Sign in to manage employees, projects &amp; tasks
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <form onSubmit={handleSubmit}>
          <TextField
  fullWidth
  label="Username"
  margin="normal"
  value={username}
  onChange={(e) => setUsername(e.target.value)}
  autoFocus
  sx={{
    "& .MuiOutlinedInput-root": {
      "&.Mui-focused fieldset": {
        borderColor: "#2563EB",
      },
    },
    "& .MuiInputLabel-root.Mui-focused": {
      color: "#2563EB",
    },
  }}
/>
        <TextField
  fullWidth
  label="Password"
  type="password"
  margin="normal"
  value={password}
  onChange={(e) => setPassword(e.target.value)}
  sx={{
    "& .MuiOutlinedInput-root": {
      "&.Mui-focused fieldset": {
        borderColor: "#2563EB",
      },
    },
    "& .MuiInputLabel-root.Mui-focused": {
      color: "#2563EB",
    },
  }}
/>
          <Box textAlign="right" mt={0.5}>
            <Link
  component={RouterLink}
  to="/forgot-password"
  variant="body2"
  sx={{
    color: "#2563EB",
    fontWeight: 600,
    textDecoration: "none",
    "&:hover": {
      color: "#1D4ED8",
    },
  }}
>
  Forgot Password?
</Link>
          </Box>
 <Box sx={{ display: "flex", justifyContent: "center", mt: 3, mb: 2 }}>
  <Button
    type="submit"
    variant="contained"
    size="large"
    disabled={loading}
    sx={{
      width: "60%", 
      height: 50,
      borderRadius: "12px",
      fontWeight: "bold",
      fontSize: "16px",
      textTransform: "uppercase",
      background: "linear-gradient(90deg, #06b6d4, #7c3aed)",
      boxShadow: "0 8px 20px rgba(124,58,237,0.35)",
      transition: "all 0.3s ease",

      "&:hover": {
        background: "linear-gradient(90deg, #0891b2, #6d28d9)",
        transform: "translateY(-2px)",
        boxShadow: "0 12px 28px rgba(124,58,237,0.45)",
      },
    }}
  >
    {loading ? <CircularProgress size={24} color="inherit" /> : "LOGIN"}
  </Button>
</Box>
        </form>

        <Typography variant="body2" textAlign="center">
          Don't have an account? <Link
  component={RouterLink}
  to="/register"
  sx={{
    color: "#2563EB",
    fontWeight: 600,
    textDecoration: "none",
    "&:hover": {
      color: "#1D4ED8",
    },
  }}
>
  Register
</Link>
        </Typography>
       
      </Paper>
    </Box>
  );
};

export default Login;
