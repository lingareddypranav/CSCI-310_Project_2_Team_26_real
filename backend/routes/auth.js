/**
 * Authentication Routes
 * Handles user registration, login, token validation, and logout
 */

const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { authenticateToken } = require('../middleware/auth');

// Register new user
router.post('/register', authController.register);

// Login user
router.post('/login', authController.login);

// Validate token
router.post('/validate', authenticateToken, authController.validateToken);

// Logout user
router.post('/logout', authenticateToken, authController.logout);

module.exports = router;

