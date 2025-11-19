/**
 * Comments Routes
 * Handles comment CRUD operations
 */

const express = require('express');
const router = express.Router();
const commentController = require('../controllers/commentController');
const { authenticateToken } = require('../middleware/auth');

// Get comments for a post
router.get('/:postId', commentController.getCommentsByPost);

// Create comment (requires auth)
router.post('/', authenticateToken, commentController.createComment);

// Update comment (requires auth, user must be author)
router.put('/:id', authenticateToken, commentController.updateComment);

// Delete comment (requires auth, user must be author)
router.delete('/:id', authenticateToken, commentController.deleteComment);

module.exports = router;

