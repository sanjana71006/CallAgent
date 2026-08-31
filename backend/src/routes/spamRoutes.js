const express = require('express');
const { reportSpam, checkSpam, getMySpam } = require('../controllers/spamController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.post('/report', protect, reportSpam);
router.get('/check/:phoneNumber', checkSpam);
router.get('/my-spam', protect, getMySpam);

module.exports = router;
