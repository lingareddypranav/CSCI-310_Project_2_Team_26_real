/**
 * Bookmarks Routes
 * Handles bookmark-related API endpoints
 */

const express = require('express');
const router = express.Router();
const bookmarkController = require('../controllers/bookmarkController');
const { authenticateToken } = require('../middleware/auth');

// All bookmark routes require authentication
router.use(authenticateToken);

// Add bookmark
router.post('/', bookmarkController.addBookmark);

// Remove bookmark
router.delete('/:postId', bookmarkController.removeBookmark);

// Get user's bookmarks
router.get('/', bookmarkController.getBookmarks);

// Check if post is bookmarked
router.get('/:postId/check', bookmarkController.isBookmarked);

module.exports = router;

