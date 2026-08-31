const crypto = require('crypto');
const userStore = require('../services/userStore');
const User = require('../models/User');

// @desc    Get current user profile & all settings
// @route   GET /api/users/me
// @access  Private
const getProfile = async (req, res) => {
  return res.status(200).json({
    success: true,
    user: {
      userId: req.user.userId,
      name: req.user.name,
      email: req.user.email,
      phoneNumber: req.user.phoneNumber,
      gender: req.user.gender || 'Female',
      avatarUri: req.user.avatarUri || '',
      location: req.user.location || null,
      addresses: req.user.addresses || [],
      assistantSettings: req.user.assistantSettings || {},
      instructions: req.user.instructions || [],
      voiceSettings: req.user.voiceSettings || {},
      silentModeSettings: req.user.silentModeSettings || {},
      notificationSettings: req.user.notificationSettings || {},
      accountStatus: req.user.accountStatus,
      createdAt: req.user.createdAt,
      lastLogin: req.user.lastLogin,
      appVersion: req.user.appVersion || '1.0.0',
    },
  });
};

// @desc    Update user personal details (Name, Phone number, Gender)
// @route   PUT /api/users/me
// @access  Private
const updateProfile = async (req, res) => {
  try {
    const { name, phoneNumber, gender } = req.body;

    if (name !== undefined) {
      if (!name || name.trim().length === 0) {
        return res.status(400).json({ success: false, message: 'Name cannot be empty.' });
      }
      if (name.trim().length > 50) {
        return res.status(400).json({ success: false, message: 'Name cannot exceed 50 characters.' });
      }
      req.user.name = name.trim();
    }

    if (phoneNumber !== undefined) {
      req.user.phoneNumber = phoneNumber.trim();
    }

    if (gender !== undefined && ['Female', 'Male', 'Prefer not to say'].includes(gender)) {
      req.user.gender = gender;
    }

    req.user.updatedAt = new Date();
    if (req.user.save) {
      await req.user.save();
    }

    return res.status(200).json({
      success: true,
      message: 'Profile details updated in MongoDB Atlas.',
      user: {
        userId: req.user.userId,
        name: req.user.name,
        email: req.user.email,
        phoneNumber: req.user.phoneNumber,
        gender: req.user.gender,
        updatedAt: req.user.updatedAt,
      },
    });
  } catch (error) {
    console.error(`[User Update Error] ${error.message}`);
    return res.status(500).json({ success: false, message: 'Failed to update profile.' });
  }
};

// @desc    Update live GPS location & auto-detected address in MongoDB Atlas
// @route   PUT /api/users/location
// @access  Private
const updateLocation = async (req, res) => {
  try {
    const { latitude, longitude, address, accuracy } = req.body;

    if (latitude === undefined || longitude === undefined) {
      return res.status(400).json({
        success: false,
        message: 'Latitude and Longitude are required.',
      });
    }

    const resolvedAddress = address ? address.trim() : `Lat: ${latitude}, Lng: ${longitude}`;

    // 1. Update user.location object in MongoDB
    req.user.location = {
      latitude: parseFloat(latitude),
      longitude: parseFloat(longitude),
      address: resolvedAddress,
      accuracy: accuracy ? parseFloat(accuracy) : null,
      updatedAt: new Date(),
    };

    // 2. Automatically store/upsert into user.addresses array in MongoDB Atlas
    if (!req.user.addresses) req.user.addresses = [];
    
    const liveAddrIdx = req.user.addresses.findIndex((a) => a.id === 'addr_live_auto');
    const liveAddressEntry = {
      id: 'addr_live_auto',
      label: '📍 Live Current Location',
      addressName: 'Live Current Location',
      fullAddress: resolvedAddress,
      additionalDetails: `Accuracy: ±${accuracy || 50}m`,
      coordinates: {
        lat: parseFloat(latitude),
        lng: parseFloat(longitude),
      },
      updatedAt: new Date(),
    };

    if (liveAddrIdx >= 0) {
      req.user.addresses[liveAddrIdx] = liveAddressEntry;
    } else {
      req.user.addresses.unshift(liveAddressEntry);
    }

    req.user.updatedAt = new Date();
    if (req.user.save) {
      await req.user.save();
    }

    return res.status(200).json({
      success: true,
      message: 'Live location and street address stored in MongoDB Atlas.',
      location: req.user.location,
      addresses: req.user.addresses,
    });
  } catch (error) {
    console.error(`[Location Update Error] ${error.message}`);
    return res.status(500).json({ success: false, message: 'Failed to save location to MongoDB.' });
  }
};

// @desc    Get All Saved Delivery Addresses
// @route   GET /api/users/addresses
// @access  Private
const getAddresses = async (req, res) => {
  try {
    return res.status(200).json({
      success: true,
      addresses: req.user.addresses || [],
      location: req.user.location || null,
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to fetch addresses.' });
  }
};

// @desc    Save/Add Delivery Address
// @route   POST /api/users/addresses
// @access  Private
const addAddress = async (req, res) => {
  try {
    const { label, addressName, fullAddress, additionalDetails, coordinates } = req.body;

    if (!fullAddress || fullAddress.trim().length === 0) {
      return res.status(400).json({ success: false, message: 'Full address is required.' });
    }

    const newAddress = {
      id: `addr_${crypto.randomUUID()}`,
      label: label || 'Home',
      addressName: addressName || label || 'My Address',
      fullAddress: fullAddress.trim(),
      additionalDetails: additionalDetails ? additionalDetails.trim() : '',
      coordinates: coordinates || { lat: null, lng: null },
      updatedAt: new Date(),
    };

    if (!req.user.addresses) req.user.addresses = [];
    req.user.addresses.push(newAddress);
    req.user.updatedAt = new Date();

    if (req.user.save) {
      await req.user.save();
    }

    return res.status(201).json({
      success: true,
      message: 'Address saved to MongoDB Atlas.',
      address: newAddress,
      addresses: req.user.addresses,
    });
  } catch (error) {
    console.error(`[Add Address Error] ${error.message}`);
    return res.status(500).json({ success: false, message: 'Failed to add address.' });
  }
};

// @desc    Delete Address
// @route   DELETE /api/users/addresses/:id
// @access  Private
const deleteAddress = async (req, res) => {
  try {
    const addressId = req.params.id;
    if (req.user.addresses) {
      req.user.addresses = req.user.addresses.filter((a) => a.id !== addressId);
      req.user.updatedAt = new Date();
      if (req.user.save) {
        await req.user.save();
      }
    }
    return res.status(200).json({
      success: true,
      message: 'Address deleted.',
      addresses: req.user.addresses || [],
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to delete address.' });
  }
};

// @desc    Add or update AI Screening Instruction
// @route   POST /api/users/instructions
// @access  Private
const addInstruction = async (req, res) => {
  try {
    const { title, prompt, tag, icon } = req.body;

    if (!title || !prompt) {
      return res.status(400).json({ success: false, message: 'Title and prompt instruction are required.' });
    }

    const newInst = {
      id: `inst_${crypto.randomUUID()}`,
      title: title.trim(),
      tag: tag || 'CUSTOM',
      prompt: prompt.trim(),
      icon: icon || 'fa-robot',
      enabled: true,
      createdAt: new Date(),
    };

    if (!req.user.instructions) req.user.instructions = [];
    req.user.instructions.push(newInst);
    req.user.updatedAt = new Date();

    if (req.user.save) {
      await req.user.save();
    }

    return res.status(201).json({
      success: true,
      message: 'Instruction saved to assistant rules in MongoDB Atlas.',
      instruction: newInst,
      instructions: req.user.instructions,
    });
  } catch (error) {
    console.error(`[Add Instruction Error] ${error.message}`);
    return res.status(500).json({ success: false, message: 'Failed to save instruction.' });
  }
};

// @desc    Delete screening instruction
// @route   DELETE /api/users/instructions/:id
// @access  Private
const deleteInstruction = async (req, res) => {
  try {
    const instId = req.params.id;
    if (req.user.instructions) {
      req.user.instructions = req.user.instructions.filter((i) => i.id !== instId);
      req.user.updatedAt = new Date();
      if (req.user.save) {
        await req.user.save();
      }
    }
    return res.status(200).json({
      success: true,
      message: 'Instruction removed.',
      instructions: req.user.instructions || [],
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Failed to delete instruction.' });
  }
};

// @desc    Update assistant preferences & settings (Voice, Silent mode, Notifications)
// @route   PUT /api/users/settings
// @access  Private
const updateSettings = async (req, res) => {
  try {
    const { assistantSettings, voiceSettings, silentModeSettings, notificationSettings } = req.body;

    if (assistantSettings) req.user.assistantSettings = { ...req.user.assistantSettings, ...assistantSettings };
    if (voiceSettings) req.user.voiceSettings = { ...req.user.voiceSettings, ...voiceSettings };
    if (silentModeSettings) req.user.silentModeSettings = { ...req.user.silentModeSettings, ...silentModeSettings };
    if (notificationSettings) req.user.notificationSettings = { ...req.user.notificationSettings, ...notificationSettings };

    req.user.updatedAt = new Date();
    if (req.user.save) {
      await req.user.save();
    }

    return res.status(200).json({
      success: true,
      message: 'Settings updated in MongoDB Atlas.',
      settings: {
        assistantSettings: req.user.assistantSettings,
        voiceSettings: req.user.voiceSettings,
        silentModeSettings: req.user.silentModeSettings,
        notificationSettings: req.user.notificationSettings,
      },
    });
  } catch (error) {
    console.error(`[Settings Update Error] ${error.message}`);
    return res.status(500).json({ success: false, message: 'Failed to update settings.' });
  }
};

// @desc    Delete authenticated user account
// @route   DELETE /api/users/me
// @access  Private
const deleteAccount = async (req, res) => {
  try {
    const userId = req.user.userId;
    await userStore.deleteById(userId);
    console.log(`[Account Deletion] User account ${userId} was permanently removed.`);

    return res.status(200).json({
      success: true,
      message: 'Your account has been permanently deleted from CallMate cloud.',
    });
  } catch (error) {
    console.error(`[Account Deletion Error] ${error.message}`);
    return res.status(500).json({ success: false, message: 'Failed to delete account. Please try again.' });
  }
};

module.exports = {
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
};
