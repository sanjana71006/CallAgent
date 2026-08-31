const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema(
  {
    userId: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
    name: {
      type: String,
      required: [true, 'Name is required'],
      trim: true,
      maxlength: [50, 'Name cannot exceed 50 characters'],
    },
    email: {
      type: String,
      required: [true, 'Email is required'],
      unique: true,
      index: true,
      lowercase: true,
      trim: true,
      match: [
        /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/,
        'Please provide a valid email address',
      ],
    },
    password: {
      type: String,
      required: [true, 'Password is required'],
      minlength: [6, 'Password must be at least 6 characters'],
      select: false,
    },
    passwordHash: {
      type: String,
      select: false,
    },
    phoneNumber: {
      type: String,
      default: '',
      trim: true,
    },
    gender: {
      type: String,
      enum: ['Female', 'Male', 'Prefer not to say'],
      default: 'Female',
    },
    avatarUri: {
      type: String,
      default: '',
    },
    location: {
      latitude: { type: Number, default: null },
      longitude: { type: Number, default: null },
      address: { type: String, default: '' },
      accuracy: { type: Number, default: null },
      updatedAt: { type: Date, default: Date.now },
    },
    addresses: [
      {
        id: { type: String, required: true },
        label: { type: String, default: 'Home' },
        addressName: { type: String, default: '' },
        fullAddress: { type: String, default: '' },
        additionalDetails: { type: String, default: '' },
        coordinates: {
          lat: { type: Number, default: null },
          lng: { type: Number, default: null },
        },
        updatedAt: { type: Date, default: Date.now },
      },
    ],
    assistantSettings: {
      assistantEnabled: { type: Boolean, default: true },
      assistantName: { type: String, default: 'CallMate AI' },
      greeting: { type: String, default: '' },
      autoScreenUnknown: { type: Boolean, default: true },
      autoScreenSpam: { type: Boolean, default: true },
    },
    instructions: [
      {
        id: { type: String, required: true },
        title: { type: String, required: true },
        tag: { type: String, default: 'CUSTOM' },
        prompt: { type: String, required: true },
        icon: { type: String, default: 'fa-robot' },
        enabled: { type: Boolean, default: true },
        createdAt: { type: Date, default: Date.now },
      },
    ],
    voiceSettings: {
      language: { type: String, default: 'en-IN' },
      voiceId: { type: String, default: 'default' },
      speechRate: { type: Number, default: 1.0 },
      speechPitch: { type: Number, default: 1.0 },
    },
    silentModeSettings: {
      enabled: { type: Boolean, default: false },
      silenceTelemarketing: { type: Boolean, default: true },
      silenceSpam: { type: Boolean, default: true },
      silenceUnknown: { type: Boolean, default: false },
      silencePotentialScam: { type: Boolean, default: true },
    },
    notificationSettings: {
      assistantUpdates: { type: Boolean, default: true },
      importantAlerts: { type: Boolean, default: true },
      featureUpdates: { type: Boolean, default: true },
      promotionalUpdates: { type: Boolean, default: false },
    },
    reportedSpam: [
      {
        phoneNumber: { type: String, required: true },
        reportedAt: { type: Date, default: Date.now },
        category: { type: String, default: 'TELEMARKETING' },
        reason: { type: String, default: '' },
      },
    ],
    accountStatus: {
      type: String,
      enum: ['ACTIVE', 'SUSPENDED', 'DELETED'],
      default: 'ACTIVE',
    },
    appVersion: {
      type: String,
      default: '1.0.0',
    },
    lastLogin: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  }
);

// Encrypt password before saving and sync passwordHash
userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) {
    return next();
  }
  const salt = await bcrypt.genSalt(10);
  const hash = await bcrypt.hash(this.password, salt);
  this.password = hash;
  this.passwordHash = hash;
  next();
});

// Match user password
userSchema.methods.comparePassword = async function (enteredPassword) {
  const hash = this.password || this.passwordHash;
  return await bcrypt.compare(enteredPassword, hash);
};

// Clean output: Remove sensitive fields from JSON serialization
userSchema.methods.toJSON = function () {
  const obj = this.toObject();
  delete obj.password;
  delete obj.passwordHash;
  delete obj.__v;
  return obj;
};

const User = mongoose.model('User', userSchema);

module.exports = User;
