/**
 * Draft Controller
 * Handles draft operations for posts
 */

const { query } = require('../config/database');

// Create draft
const createDraft = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous } = req.body;

    // Validation - at least title should be provided
    if (!title || title.trim().length === 0) {
      return res.status(400).json({
        error: 'Missing required field',
        message: 'Title is required'
      });
    }

    const result = await query(
      `INSERT INTO drafts (user_id, title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
       RETURNING id, user_id, title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous, created_at, updated_at`,
      [
        userId,
        title.trim(),
        content ? content.trim() : null,
        prompt_section ? prompt_section.trim() : null,
        description_section ? description_section.trim() : null,
        llm_tag ? llm_tag.trim() : null,
        is_prompt_post === true || is_prompt_post === 'true',
        anonymous === true || anonymous === 'true'
      ]
    );

    res.status(201).json({
      message: 'Draft created successfully',
      draft: result.rows[0]
    });
  } catch (error) {
    console.error('Create draft error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to create draft'
    });
  }
};

// Get user's drafts
const getDrafts = async (req, res) => {
  try {
    const userId = req.user.userId;

    const result = await query(
      `SELECT id, user_id, title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous, created_at, updated_at
       FROM drafts
       WHERE user_id = $1
       ORDER BY updated_at DESC`,
      [userId]
    );

    res.json({
      drafts: result.rows,
      count: result.rows.length
    });
  } catch (error) {
    console.error('Get drafts error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get drafts'
    });
  }
};

// Get single draft
const getDraftById = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { id } = req.params;

    const result = await query(
      `SELECT id, user_id, title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous, created_at, updated_at
       FROM drafts
       WHERE id = $1 AND user_id = $2`,
      [id, userId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        error: 'Draft not found'
      });
    }

    res.json({
      draft: result.rows[0]
    });
  } catch (error) {
    console.error('Get draft error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get draft'
    });
  }
};

// Update draft
const updateDraft = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { id } = req.params;
    const { title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous } = req.body;

    // Check if draft exists and belongs to user
    const draftCheck = await query(
      'SELECT id FROM drafts WHERE id = $1 AND user_id = $2',
      [id, userId]
    );

    if (draftCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Draft not found'
      });
    }

    // Build update query
    const updates = [];
    const values = [];
    let paramCount = 1;

    if (title !== undefined) {
      updates.push(`title = $${paramCount++}`);
      values.push(title.trim());
    }
    if (content !== undefined) {
      updates.push(`content = $${paramCount++}`);
      values.push(content ? content.trim() : null);
    }
    if (prompt_section !== undefined) {
      updates.push(`prompt_section = $${paramCount++}`);
      values.push(prompt_section ? prompt_section.trim() : null);
    }
    if (description_section !== undefined) {
      updates.push(`description_section = $${paramCount++}`);
      values.push(description_section ? description_section.trim() : null);
    }
    if (llm_tag !== undefined) {
      updates.push(`llm_tag = $${paramCount++}`);
      values.push(llm_tag ? llm_tag.trim() : null);
    }
    if (is_prompt_post !== undefined) {
      updates.push(`is_prompt_post = $${paramCount++}`);
      values.push(is_prompt_post === true || is_prompt_post === 'true');
    }
    if (anonymous !== undefined) {
      updates.push(`anonymous = $${paramCount++}`);
      values.push(anonymous === true || anonymous === 'true');
    }

    if (updates.length === 0) {
      return res.status(400).json({
        error: 'No fields to update'
      });
    }

    values.push(id);
    values.push(userId);

    const queryText = `
      UPDATE drafts 
      SET ${updates.join(', ')}
      WHERE id = $${paramCount++} AND user_id = $${paramCount}
      RETURNING id, user_id, title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous, created_at, updated_at
    `;

    const result = await query(queryText, values);

    res.json({
      message: 'Draft updated successfully',
      draft: result.rows[0]
    });
  } catch (error) {
    console.error('Update draft error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to update draft'
    });
  }
};

// Delete draft
const deleteDraft = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { id } = req.params;

    const result = await query(
      'DELETE FROM drafts WHERE id = $1 AND user_id = $2',
      [id, userId]
    );

    if (result.rowCount === 0) {
      return res.status(404).json({
        error: 'Draft not found'
      });
    }

    res.json({
      message: 'Draft deleted successfully'
    });
  } catch (error) {
    console.error('Delete draft error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to delete draft'
    });
  }
};

module.exports = {
  createDraft,
  getDrafts,
  getDraftById,
  updateDraft,
  deleteDraft
};

