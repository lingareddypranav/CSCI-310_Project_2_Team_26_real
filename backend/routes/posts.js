/**
 * Posts Routes
 * Handles post CRUD operations, search, and trending posts
 */

const express = require('express');
const router = express.Router();
const postController = require('../controllers/postController');
const { authenticateToken, optionalAuth } = require('../middleware/auth');

// Get all posts (with optional auth for personalization)
router.get('/', optionalAuth, postController.getPosts);

// Get prompt posts only
router.get('/prompts', optionalAuth, postController.getPromptPosts);

// Get trending posts (top K)
router.get('/trending', optionalAuth, postController.getTrendingPosts);

// Search posts
router.get('/search', optionalAuth, postController.searchPosts);

// Get single post by ID
router.get('/:id', optionalAuth, postController.getPostById);

// Create new post (requires auth)
router.post('/', authenticateToken, postController.createPost);

// Update post (requires auth, user must be author)
router.put('/:id', authenticateToken, postController.updatePost);

// Delete post (requires auth, user must be author)
router.delete('/:id', authenticateToken, postController.deletePost);

module.exports = router;

