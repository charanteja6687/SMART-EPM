import React, { useState } from 'react';
import {
  Box, Paper, TextField, Button, Typography, Link, Alert, CircularProgress, Stepper, Step, StepLabel
} from '@mui/material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import authService from '../../services/authService';
import { toast } from 'react-toastify';
import { keyframes } from "@mui/system";
import GroupsIcon from "@mui/icons-material/Groups";

const gradientAnimation = keyframes`
0%{background-position:0% 50%;}
25%{background-position:100% 50%;}
50%{background-position:100% 100%;}
75%{background-position:0% 100%;}
100%{background-position:0% 50%;}
`;

const floating = keyframes`
0%{transform:translateX(-10px);}
50%{transform:translateX(10px);}
100%{transform:translateX(-10px);}
`;

const glow = keyframes`
0%{filter:drop-shadow(0 0 5px #38bdf8);}
50%{filter:drop-shadow(0 0 25px #38bdf8);}
100%{filter:drop-shadow(0 0 5px #38bdf8);}
`;

const STEPS = ['Enter Email', 'Enter OTP', 'Reset Password'];

const ForgotPassword = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Step 1: request an OTP be emailed to this address
  const handleRequestOtp = async (e) => {
    e.preventDefault();
    setError('');
    if (!email) {
      setError('Please enter your email address');
      return;
    }
    setLoading(true);
    try {
      await authService.forgotPassword(email);
      toast.success('OTP sent! Check your email — it is valid for 5 minutes.');
      setActiveStep(1);
    } catch (err) {
      const message = err.response?.data?.message || 'Failed to send OTP. Please check the email and try again.';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  // Step 2: verify the OTP is correct before moving on (doesn't consume it — reset-password re-checks it)
  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    if (!otp || otp.length !== 6) {
      setError('Please enter the 6-digit OTP sent to your email');
      return;
    }
    setLoading(true);
    try {
      await authService.verifyOtp(email, otp);
      toast.success('OTP verified!');
      setActiveStep(2);
    } catch (err) {
      const message = err.response?.data?.message || 'Invalid or expired OTP.';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  // Step 3: actually reset the password
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    if (!newPassword || newPassword.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      await authService.resetPassword(email, otp, newPassword);
      toast.success('Password reset successfully! Please log in with your new password.');
      navigate('/login');
    } catch (err) {
      const message = err.response?.data?.message || 'Failed to reset password.';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
   <Box
sx={{
display:"flex",
justifyContent:"space-evenly",
alignItems:"center",
minHeight:"100vh",
px:8,

background:`
radial-gradient(circle at 20% 20%, rgba(59,130,246,.5), transparent 35%),
radial-gradient(circle at 80% 30%, rgba(147,51,234,.5), transparent 35%),
radial-gradient(circle at 30% 80%, rgba(6,182,212,.5), transparent 35%),
#0f172a
`,

backgroundSize:"250% 250%",
animation:`${gradientAnimation} 18s ease infinite`,
}}
>
  <Box
sx={{
color:"white",
maxWidth:380
}}
>

<GroupsIcon
sx={{
fontSize:90,
color:"#38bdf8",
animation:`${glow} 2s ease-in-out infinite`
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
          Forgot Password
        </Typography>

        <Stepper
  activeStep={activeStep}
  alternativeLabel
  sx={{
    my: 3,

    "& .MuiStepIcon-root": {
      color: "#6B7280", // Inactive circles
    },

    "& .MuiStepIcon-root.Mui-active": {
      color: "#2563EB", // Current step
    },

    "& .MuiStepIcon-root.Mui-completed": {
      color: "#2563EB", // Completed steps
    },

    "& .MuiStepLabel-label": {
      color: "#D1D5DB", // Inactive text
      fontWeight: 500,
    },

    "& .MuiStepLabel-label.Mui-active": {
      color: "#FFFFFF", // Active text
      fontWeight: 700,
    },

    "& .MuiStepLabel-label.Mui-completed": {
      color: "#FFFFFF", // Completed text
      fontWeight: 700,
    },
  }}
>
  {STEPS.map((label) => (
    <Step key={label}>
      <StepLabel>{label}</StepLabel>
    </Step>
  ))}
</Stepper>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {activeStep === 0 && (
          <form onSubmit={handleRequestOtp}>
            <Typography variant="body2" color="text.secondary" mb={2}>
              Enter the email address associated with your account. We'll send you a one-time code.
            </Typography>
           <TextField
  fullWidth
  label="Email"
  type="email"
  margin="normal"
  value={email}
  onChange={(e) => setEmail(e.target.value)}
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
           <Button
  type="submit"
  variant="contained"
  disabled={loading}
  sx={{
    width: 180,
    height: 50,
    display: "block",
    mx: "auto",
    mt: 3,
    mb: 2,
    borderRadius: "12px",
    fontWeight: "bold",
    fontSize: "16px",
    textTransform: "uppercase",
    background: "linear-gradient(90deg, #06b6d4, #7c3aed)",
    boxShadow: "0 8px 20px rgba(124,58,237,.35)",

    "&:hover": {
      background: "linear-gradient(90deg, #0891b2, #6d28d9)",
      transform: "translateY(-2px)",
      boxShadow: "0 12px 28px rgba(124,58,237,.45)",
    },
  }}
>
  {loading ? <CircularProgress size={24} color="inherit" /> : "SEND OTP"}
</Button>
          </form>
        )}

        {activeStep === 1 && (
          <form onSubmit={handleVerifyOtp}>
            <Typography variant="body2" color="text.secondary" mb={2}>
              Enter the 6-digit code sent to <b>{email}</b>. It expires in 5 minutes.
            </Typography>
         <TextField
  fullWidth
  label="OTP"
  margin="normal"
  inputProps={{ maxLength: 6 }}
  value={otp}
  onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
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
           <Button
  type="submit"
  variant="contained"
  disabled={loading}
  sx={{
    width: 180,
    height: 50,
    display: "block",
    mx: "auto",
    mt: 3,
    mb: 2,
    borderRadius: "12px",
    fontWeight: "bold",
    fontSize: "16px",
    textTransform: "uppercase",
    background: "linear-gradient(90deg, #06b6d4, #7c3aed)",
    boxShadow: "0 8px 20px rgba(124,58,237,.35)",

    "&:hover": {
      background: "linear-gradient(90deg, #0891b2, #6d28d9)",
      transform: "translateY(-2px)",
      boxShadow: "0 12px 28px rgba(124,58,237,.45)",
    },
  }}
>
  {loading ? <CircularProgress size={24} color="inherit" /> : "VERIFY OTP"}
</Button>
           <Button
  onClick={() => setActiveStep(0)}
  disabled={loading}
  sx={{
    display: "block",
    mx: "auto",
    color: "#38bdf8",
    fontWeight: 600,
    textTransform: "none",
  }}
>
  ← Back
</Button>
          </form>
        )}

        {activeStep === 2 && (
          <form onSubmit={handleResetPassword}>
            <Typography variant="body2" color="text.secondary" mb={2}>
              Choose a new password for your account.
            </Typography>
            <TextField
  fullWidth
  label="New Password"
  type="password"
  margin="normal"
  value={newPassword}
  onChange={(e) => setNewPassword(e.target.value)}
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
  label="Confirm New Password"
  type="password"
  margin="normal"
  value={confirmPassword}
  onChange={(e) => setConfirmPassword(e.target.value)}
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
<Button
  type="submit"
  variant="contained"
  disabled={loading}
  sx={{
    width: 200,
    height: 50,
    display: "block",
    mx: "auto",
    mt: 3,
    mb: 2,
    borderRadius: "12px",
    fontWeight: "bold",
    fontSize: "16px",
    textTransform: "uppercase",
    background: "linear-gradient(90deg,#06b6d4,#7c3aed)",
    boxShadow: "0 8px 20px rgba(124,58,237,.35)",

    "&:hover": {
      background: "linear-gradient(90deg,#0891b2,#6d28d9)",
      transform: "translateY(-2px)",
      boxShadow: "0 12px 28px rgba(124,58,237,.45)",
    },
  }}
>
  {loading ? (
    <CircularProgress size={24} color="inherit" />
  ) : (
    "RESET PASSWORD"
  )}
</Button>
          </form>
        )}

        <Typography variant="body2" textAlign="center" mt={2}>
          Remembered your password? <Link component={RouterLink} to="/login">Back to Login</Link>
        </Typography>
      </Paper>
    </Box>
  );
};

export default ForgotPassword;
