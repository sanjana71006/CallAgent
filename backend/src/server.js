const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');

// Load environment variables
dotenv.config();

const { connectDB, getDBStatus } = require('./config/database');
const authRoutes = require('./routes/authRoutes');
const userRoutes = require('./routes/userRoutes');
const spamRoutes = require('./routes/spamRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// Enable CORS for Android client & development
app.use(cors());

// Parse JSON request bodies
app.use(express.json());

// Request logging in development
if (process.env.NODE_ENV !== 'test') {
  app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.originalUrl}`);
    next();
  });
}

// Mount Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/spam', spamRoutes);

// System Health Check
app.get('/api/health', (req, res) => {
  const dbStatus = getDBStatus();
  return res.status(200).json({
    status: 'ok',
    service: 'CallMate AI Cloud Backend',
    timestamp: new Date().toISOString(),
    database: dbStatus,
  });
});

// Centralized 404 Handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `Cannot ${req.method} ${req.originalUrl}`,
  });
});

// Centralized Error Handling Middleware
app.use((err, req, res, next) => {
  console.error(`[Unhandled Server Error] ${err.stack || err.message}`);
  res.status(500).json({
    success: false,
    message: 'Internal server error occurred.',
  });
});

// Start Server
if (process.env.NODE_ENV !== 'test') {
  connectDB();
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`[CallMate AI Backend] Server listening on http://0.0.0.0:${PORT}`);
  });
}

module.exports = app;
