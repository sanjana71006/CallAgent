const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'callmate_ai_jwt_dev_secret_2026';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '30d';

const generateToken = (user) => {
  return jwt.sign(
    {
      userId: user.userId,
      email: user.email,
    },
    JWT_SECRET,
    {
      expiresIn: JWT_EXPIRES_IN,
    }
  );
};

const verifyToken = (token) => {
  try {
    return jwt.verify(token, JWT_SECRET);
  } catch (error) {
    return null;
  }
};

const validateRegisterInput = ({ name, email, password, confirmPassword, phoneNumber }) => {
  if (!name || name.trim().length === 0) {
    return 'Name is required.';
  }
  if (name.trim().length > 50) {
    return 'Name cannot exceed 50 characters.';
  }
  if (!email || email.trim().length === 0) {
    return 'Email is required.';
  }
  const emailRegex = /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/;
  if (!emailRegex.test(email.trim())) {
    return 'Please provide a valid email address.';
  }
  if (phoneNumber && phoneNumber.trim().length > 0) {
    const phoneClean = phoneNumber.trim().replace(/[\s\-\(\)\+]/g, '');
    if (phoneClean.length < 7 || phoneClean.length > 15 || !/^\d+$/.test(phoneClean)) {
      return 'Please enter a valid phone number.';
    }
  }
  if (!password || password.length < 6) {
    return 'Password must be at least 6 characters.';
  }
  if (confirmPassword !== undefined && password !== confirmPassword) {
    return 'Passwords do not match.';
  }
  return null;
};

const validateLoginInput = ({ email, password }) => {
  if (!email || email.trim().length === 0) {
    return 'Email is required.';
  }
  if (!password || password.length === 0) {
    return 'Password is required.';
  }
  return null;
};

module.exports = {
  generateToken,
  verifyToken,
  validateRegisterInput,
  validateLoginInput,
};
