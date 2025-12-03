/**
 * Comment Controller
 * Handles comment CRUD operations
 */

const { query } = require('../config/database');

// Get comments for a post
const getCommentsByPost = async (req, res) => {
  try {
    const { postId } = req.params;
    const userId = req.user?.userId; // Optional auth

    const params = [postId];
    let paramCount = 2;

    let queryText = `
      SELECT 
        c.id,
        c.post_id,
        c.author_id,
        u.name as author_name,
        c.title,
        c.text,
        c.created_at,
        c.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        uv.type as user_vote_type
      FROM comments c
      LEFT JOIN users u ON c.author_id = u.id
      LEFT JOIN votes v ON v.comment_id = c.id
      ${userId ? `LEFT JOIN votes uv ON uv.comment_id = c.id AND uv.user_id = $${paramCount++}` : 'LEFT JOIN votes uv ON false'}
      WHERE c.post_id = $1
    `;

    if (userId) {
      params.push(userId);
    }

    queryText += ` GROUP BY c.id, u.name, uv.type
      ORDER BY c.created_at ASC`;

    const result = await query(queryText, params);

    res.json({
      comments: result.rows,
      count: result.rows.length
    });
  } catch (error) {
    console.error('Get comments error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get comments'
    });
  }
};

// Create comment
const createComment = async (req, res) => {
  try {
    const authorId = req.user.userId;
    const { post_id, text, title } = req.body;

    // Validation
    if (!post_id || !text) {
      return res.status(400).json({
        error: 'Missing required fields',
        message: 'Post ID and text are required'
      });
    }

    // Verify post exists
    const postCheck = await query(
      'SELECT id FROM posts WHERE id = $1',
      [post_id]
    );

    if (postCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Post not found',
        message: 'Cannot comment on non-existent post'
      });
    }

    // Insert comment (title is optional)
    const result = await query(
      `INSERT INTO comments (post_id, author_id, text, title)
       VALUES ($1, $2, $3, $4)
       RETURNING id, post_id, author_id, title, text, created_at, updated_at`,
      [post_id, authorId, text, title || null]
    );

    res.status(201).json({
      message: 'Comment created successfully',
      comment: result.rows[0]
    });
  } catch (error) {
    console.error('Create comment error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to create comment'
    });
  }
};

// Update comment
const updateComment = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.userId;
    const { text, title } = req.body;

    // Validation
    if (!text) {
      return res.status(400).json({
        error: 'Missing required field',
        message: 'Text is required'
      });
    }

    // Check if comment exists and user is author
    const commentCheck = await query(
      'SELECT author_id FROM comments WHERE id = $1',
      [id]
    );

    if (commentCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Comment not found'
      });
    }

    if (commentCheck.rows[0].author_id !== userId) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'You can only edit your own comments'
      });
    }

    // Update comment
    const result = await query(
      `UPDATE comments 
       SET text = $1, title = $2
       WHERE id = $3
       RETURNING id, post_id, author_id, title, text, created_at, updated_at`,
      [text, title || null, id]
    );

    res.json({
      message: 'Comment updated successfully',
      comment: result.rows[0]
    });
  } catch (error) {
    console.error('Update comment error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to update comment'
    });
  }
};

// Delete comment
const deleteComment = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.userId;

    // Check if comment exists and user is author
    const commentCheck = await query(
      'SELECT author_id FROM comments WHERE id = $1',
      [id]
    );

    if (commentCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Comment not found'
      });
    }

    if (commentCheck.rows[0].author_id !== userId) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'You can only delete your own comments'
      });
    }

    // Delete comment (cascade will handle votes)
    await query('DELETE FROM comments WHERE id = $1', [id]);

    res.json({
      message: 'Comment deleted successfully'
    });
  } catch (error) {
    console.error('Delete comment error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to delete comment'
    });
  }
};

// Get comments by user ID
const getCommentsByUser = async (req, res) => {
  try {
    const { userId: targetUserId } = req.params;
    const currentUserId = req.user?.userId; // Optional auth for vote type

    const params = [targetUserId];
    let paramCount = 2;

    let queryText = `
      SELECT 
        c.id,
        c.post_id,
        c.author_id,
        u.name as author_name,
        c.title,
        c.text,
        c.created_at,
        c.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        uv.type as user_vote_type
      FROM comments c
      LEFT JOIN users u ON c.author_id = u.id
      LEFT JOIN votes v ON v.comment_id = c.id
      ${currentUserId ? `LEFT JOIN votes uv ON uv.comment_id = c.id AND uv.user_id = $${paramCount++}` : 'LEFT JOIN votes uv ON false'}
      WHERE c.author_id = $1
    `;

    if (currentUserId) {
      params.push(currentUserId);
    }

    queryText += ` GROUP BY c.id, u.name, uv.type
      ORDER BY c.created_at DESC`;

    const result = await query(queryText, params);

    res.json({
      comments: result.rows,
      count: result.rows.length
    });
  } catch (error) {
    console.error('Get comments by user error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get comments'
    });
  }
};

module.exports = {
  getCommentsByPost,
  getCommentsByUser,
  createComment,
  updateComment,
  deleteComment
};

