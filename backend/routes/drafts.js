/**
 * Drafts Routes
 * Handles draft-related API endpoints
 */

const express = require('express');
const router = express.Router();
const draftController = require('../controllers/draftController');
const { authenticateToken } = require('../middleware/auth');

// All draft routes require authentication
router.use(authenticateToken);

// Create draft
router.post('/', draftController.createDraft);

// Get user's drafts
router.get('/', draftController.getDrafts);

// Get single draft
router.get('/:id', draftController.getDraftById);

// Update draft
router.put('/:id', draftController.updateDraft);

// Delete draft
router.delete('/:id', draftController.deleteDraft);

module.exports = router;

