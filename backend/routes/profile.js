/**
 * Profile Routes
 * Handles profile creation, retrieval, updates, and password reset
 */

const express = require('express');
const router = express.Router();
const profileController = require('../controllers/profileController');
const { authenticateToken } = require('../middleware/auth');

// Create profile (requires auth)
router.post('/create', authenticateToken, profileController.createProfile);

// Get profile by user ID
router.get('/:userId', profileController.getProfile);

// Update profile (requires auth, user must own profile)
router.put('/:userId', authenticateToken, profileController.updateProfile);

// Reset password (requires auth, user must own profile)
router.post('/reset-password', authenticateToken, profileController.resetPassword);

module.exports = router;

