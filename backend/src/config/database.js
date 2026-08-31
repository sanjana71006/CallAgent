const mongoose = require('mongoose');

// Disable buffering so queries fail immediately or fallback instead of hanging for 10 seconds
mongoose.set('bufferCommands', false);

let isConnected = false;

const connectDB = async () => {
  const uri = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/callmate_ai';

  try {
    const conn = await mongoose.connect(uri, {
      serverSelectionTimeoutMS: 4000,
      connectTimeoutMS: 5000,
    });

    isConnected = true;
    console.log(`[MongoDB Atlas] Connected successfully: ${conn.connection.host}/${conn.connection.name}`);
  } catch (error) {
    isConnected = false;
    console.warn(`[MongoDB Atlas] Connection failed or offline (${error.message}). Running server with degraded cloud capabilities.`);
  }

  mongoose.connection.on('connected', () => {
    isConnected = true;
    console.log('[MongoDB] Mongoose connection established.');
  });

  mongoose.connection.on('disconnected', () => {
    isConnected = false;
    console.warn('[MongoDB] Mongoose connection lost.');
  });

  mongoose.connection.on('error', (err) => {
    isConnected = false;
    console.error(`[MongoDB] Mongoose runtime error: ${err.message}`);
  });
};

const getDBStatus = () => {
  return {
    connected: isConnected && mongoose.connection.readyState === 1,
    readyState: mongoose.connection.readyState,
    host: mongoose.connection.host || null,
    database: mongoose.connection.name || null,
  };
};

module.exports = { connectDB, getDBStatus };
