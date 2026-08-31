const userStore = require('../services/userStore');
const {
  generateToken,
  validateRegisterInput,
  validateLoginInput,
} = require('../services/authService');

// @desc    Register a new user
// @route   POST /api/auth/register
// @access  Public
const register = async (req, res) => {
  try {
    const { name, email, password, confirmPassword, phoneNumber } = req.body;

    const validationError = validateRegisterInput({
      name,
      email,
      password,
      confirmPassword,
      phoneNumber,
    });
    if (validationError) {
      return res.status(400).json({ success: false, message: validationError });
    }

    const normalizedEmail = email.trim().toLowerCase();

    // Check if user already exists
    const existingUser = await userStore.findByEmail(normalizedEmail);
    if (existingUser) {
      return res.status(400).json({
        success: false,
        message: 'An account with this email address already exists.',
      });
    }

    const user = await userStore.create({
      name: name.trim(),
      email: normalizedEmail,
      password,
      phoneNumber: phoneNumber ? phoneNumber.trim() : '',
    });

    const token = generateToken(user);

    return res.status(201).json({
      success: true,
      message: 'Account created successfully.',
      token,
      user: {
        userId: user.userId,
        name: user.name,
        email: user.email,
        phoneNumber: user.phoneNumber,
        accountStatus: user.accountStatus,
        createdAt: user.createdAt,
      },
    });
  } catch (error) {
    console.error(`[Auth Register Error] ${error.message}`);
    return res.status(500).json({
      success: false,
      message: 'Unable to complete registration. Please try again.',
    });
  }
};

// @desc    Authenticate user & get session token
// @route   POST /api/auth/login
// @access  Public
const login = async (req, res) => {
  try {
    const { email, password } = req.body;

    const validationError = validateLoginInput({ email, password });
    if (validationError) {
      return res.status(400).json({ success: false, message: validationError });
    }

    const normalizedEmail = email.trim().toLowerCase();

    // Find user
    const user = await userStore.findByEmail(normalizedEmail);
    if (!user || user.accountStatus === 'DELETED') {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password.',
      });
    }

    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid email or password.',
      });
    }

    // Update last login timestamp
    user.lastLogin = new Date();
    if (user.save) {
      await user.save();
    }

    const token = generateToken(user);

    return res.status(200).json({
      success: true,
      message: 'Login successful.',
      token,
      user: {
        userId: user.userId,
        name: user.name,
        email: user.email,
        phoneNumber: user.phoneNumber,
        accountStatus: user.accountStatus,
        createdAt: user.createdAt,
        lastLogin: user.lastLogin,
      },
    });
  } catch (error) {
    console.error(`[Auth Login Error] ${error.message}`);
    return res.status(500).json({
      success: false,
      message: 'Authentication failed. Please try again.',
    });
  }
};

// @desc    Logout user / invalidate session
// @route   POST /api/auth/logout
// @access  Public
const logout = async (req, res) => {
  return res.status(200).json({
    success: true,
    message: 'Logged out successfully.',
  });
};

// @desc    Get current session profile
// @route   GET /api/auth/me
// @access  Private
const getMe = async (req, res) => {
  try {
    return res.status(200).json({
      success: true,
      user: {
        userId: req.user.userId,
        name: req.user.name,
        email: req.user.email,
        phoneNumber: req.user.phoneNumber,
        accountStatus: req.user.accountStatus,
        createdAt: req.user.createdAt,
        lastLogin: req.user.lastLogin,
      },
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: 'Failed to retrieve profile.',
    });
  }
};

module.exports = {
  register,
  login,
  logout,
  getMe,
};
