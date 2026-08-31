const express = require('express');
const router = express.Router();
const {
  getProfile,
  updateProfile,
  updateLocation,
  getAddresses,
  addAddress,
  deleteAddress,
  addInstruction,
  deleteInstruction,
  updateSettings,
  deleteAccount,
} = require('../controllers/userController');
const { protect } = require('../middleware/authMiddleware');

router.get('/me', protect, getProfile);
router.put('/me', protect, updateProfile);
router.put('/location', protect, updateLocation);
router.get('/addresses', protect, getAddresses);
router.post('/addresses', protect, addAddress);
router.delete('/addresses/:id', protect, deleteAddress);
router.post('/instructions', protect, addInstruction);
router.delete('/instructions/:id', protect, deleteInstruction);
router.put('/settings', protect, updateSettings);
router.delete('/me', protect, deleteAccount);

module.exports = router;
