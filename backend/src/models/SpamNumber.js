const mongoose = require('mongoose');

const spamNumberSchema = new mongoose.Schema(
  {
    phoneNumber: {
      type: String,
      required: [true, 'Phone number is required'],
      unique: true,
      index: true,
      trim: true,
    },
    formattedNumber: {
      type: String,
      default: '',
    },
    callerName: {
      type: String,
      default: 'Unknown Telemarketer / Spam',
    },
    reportCount: {
      type: Number,
      default: 1,
    },
    spamScore: {
      type: Number,
      default: 85, // 0 to 100
      min: 0,
      max: 100,
    },
    category: {
      type: String,
      enum: ['TELEMARKETING', 'FINANCIAL_SCAM', 'ROBOCALL', 'PHISHING', 'FAKE_DELIVERY', 'SPAM'],
      default: 'SPAM',
    },
    reporters: [
      {
        type: String, // userId of the reporter
      },
    ],
    reasons: [
      {
        type: String,
      },
    ],
    lastReportedAt: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  }
);

// Helper to normalize phone numbers
spamNumberSchema.statics.normalizePhone = function (phone) {
  if (!phone) return '';
  return phone.replace(/[^\d+]/g, '').trim();
};

const SpamNumber = mongoose.model('SpamNumber', spamNumberSchema);

module.exports = SpamNumber;
