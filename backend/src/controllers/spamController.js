const SpamNumber = require('../models/SpamNumber');
const User = require('../models/User');

// Helper to normalize phone number
const normalizePhone = (phone) => {
  if (!phone) return '';
  return phone.replace(/[^\d+]/g, '').trim();
};

// In-memory fallback for spam numbers if DB reconnecting
const memSpamStore = new Map();

// @desc    Report a phone number as spam
// @route   POST /api/spam/report
// @access  Private
const reportSpam = async (req, res) => {
  try {
    const { phoneNumber, callerName, category, reason } = req.body;
    const userId = req.user ? req.user.userId : 'usr_anon';

    if (!phoneNumber || phoneNumber.trim().length < 5) {
      return res.status(400).json({
        success: false,
        message: 'A valid phone number is required to report spam.',
      });
    }

    const normPhone = normalizePhone(phoneNumber);
    const cat = category || 'TELEMARKETING';
    const rsn = reason || 'Unsolicited spam call reported by user';

    let spamEntry = null;

    try {
      spamEntry = await SpamNumber.findOne({ phoneNumber: normPhone });
      if (spamEntry) {
        if (!spamEntry.reporters.includes(userId)) {
          spamEntry.reporters.push(userId);
          spamEntry.reportCount += 1;
        }
        if (rsn && !spamEntry.reasons.includes(rsn)) {
          spamEntry.reasons.push(rsn);
        }
        spamEntry.category = cat;
        spamEntry.spamScore = Math.min(100, 60 + spamEntry.reportCount * 10);
        spamEntry.lastReportedAt = new Date();
        await spamEntry.save();
      } else {
        spamEntry = await SpamNumber.create({
          phoneNumber: normPhone,
          formattedNumber: phoneNumber,
          callerName: callerName || 'Unknown Spam Caller',
          category: cat,
          reportCount: 1,
          spamScore: 85,
          reporters: [userId],
          reasons: [rsn],
          lastReportedAt: new Date(),
        });
      }
    } catch (dbErr) {
      console.warn(`[SpamController] MongoDB Atlas save fallback: ${dbErr.message}`);
      const existing = memSpamStore.get(normPhone) || {
        phoneNumber: normPhone,
        formattedNumber: phoneNumber,
        callerName: callerName || 'Unknown Spam Caller',
        reportCount: 0,
        spamScore: 85,
        category: cat,
        reporters: [],
        reasons: [],
      };
      if (!existing.reporters.includes(userId)) {
        existing.reporters.push(userId);
        existing.reportCount += 1;
      }
      existing.lastReportedAt = new Date();
      memSpamStore.set(normPhone, existing);
      spamEntry = existing;
    }

    // Also update user's profile reportedSpam array in User model
    try {
      if (req.user && req.user._id) {
        const userDoc = await User.findById(req.user._id);
        if (userDoc) {
          const alreadyInList = userDoc.reportedSpam.some((s) => s.phoneNumber === normPhone);
          if (!alreadyInList) {
            userDoc.reportedSpam.push({
              phoneNumber: normPhone,
              reportedAt: new Date(),
              category: cat,
              reason: rsn,
            });
            await userDoc.save();
          }
        }
      }
    } catch (uErr) {
      console.warn(`[SpamController] User reportedSpam update notice: ${uErr.message}`);
    }

    return res.status(200).json({
      success: true,
      message: `Phone number ${phoneNumber} successfully reported as spam to community database.`,
      spamInfo: {
        phoneNumber: normPhone,
        reportCount: spamEntry.reportCount,
        spamScore: spamEntry.spamScore,
        category: spamEntry.category,
        lastReportedAt: spamEntry.lastReportedAt,
      },
    });
  } catch (error) {
    console.error(`[Report Spam Error] ${error.message}`);
    return res.status(500).json({
      success: false,
      message: 'Failed to report spam number.',
    });
  }
};

// @desc    Check if a number is flagged as spam globally
// @route   GET /api/spam/check/:phoneNumber
// @access  Public
const checkSpam = async (req, res) => {
  try {
    const { phoneNumber } = req.params;
    const normPhone = normalizePhone(phoneNumber);

    let spamEntry = null;
    try {
      spamEntry = await SpamNumber.findOne({ phoneNumber: normPhone });
    } catch (e) {
      spamEntry = memSpamStore.get(normPhone);
    }

    if (spamEntry) {
      return res.status(200).json({
        success: true,
        isSpam: true,
        reportCount: spamEntry.reportCount,
        spamScore: spamEntry.spamScore,
        category: spamEntry.category,
        callerName: spamEntry.callerName,
        lastReportedAt: spamEntry.lastReportedAt,
      });
    }

    return res.status(200).json({
      success: true,
      isSpam: false,
      reportCount: 0,
      spamScore: 0,
      category: 'CLEAN',
    });
  } catch (error) {
    console.error(`[Check Spam Error] ${error.message}`);
    return res.status(500).json({
      success: false,
      message: 'Failed to check spam status.',
    });
  }
};

// @desc    Get all spam numbers reported by the current user
// @route   GET /api/spam/my-spam
// @access  Private
const getMySpam = async (req, res) => {
  try {
    const userId = req.user.userId;
    let list = [];

    try {
      list = await SpamNumber.find({ reporters: userId }).sort({ lastReportedAt: -1 });
    } catch (e) {
      list = Array.from(memSpamStore.values()).filter((s) => s.reporters.includes(userId));
    }

    return res.status(200).json({
      success: true,
      count: list.length,
      spamNumbers: list,
    });
  } catch (error) {
    console.error(`[Get My Spam Error] ${error.message}`);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch your reported spam numbers.',
    });
  }
};

module.exports = {
  reportSpam,
  checkSpam,
  getMySpam,
};
