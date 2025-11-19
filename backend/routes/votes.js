/**
 * Votes Routes
 * Handles voting on posts and comments
 */

const express = require('express');
const router = express.Router();
const voteController = require('../controllers/voteController');
const { authenticateToken } = require('../middleware/auth');

// Vote on a post (requires auth)
router.post('/post/:postId', authenticateToken, voteController.votePost);

// Vote on a comment (requires auth)
router.post('/comment/:commentId', authenticateToken, voteController.voteComment);

// Remove vote from post (requires auth)
router.delete('/post/:postId', authenticateToken, voteController.removePostVote);

// Remove vote from comment (requires auth)
router.delete('/comment/:commentId', authenticateToken, voteController.removeCommentVote);

// Get vote counts for a post
router.get('/post/:postId', voteController.getPostVoteCounts);

// Get vote counts for a comment
router.get('/comment/:commentId', voteController.getCommentVoteCounts);

module.exports = router;

