/**
 * Comment Controller
 * Handles comment CRUD operations
 */

const { query } = require('../config/database');

// Get comments for a post
const getCommentsByPost = async (req, res) => {
  try {
    const { postId } = req.params;

    const result = await query(
      `SELECT 
        c.id,
        c.post_id,
        c.author_id,
        u.name as author_name,
        c.text,
        c.created_at,
        c.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes
      FROM comments c
      LEFT JOIN users u ON c.author_id = u.id
      LEFT JOIN votes v ON v.comment_id = c.id
      WHERE c.post_id = $1
      GROUP BY c.id, u.name
      ORDER BY c.created_at ASC`,
      [postId]
    );

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
    const { post_id, text } = req.body;

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

    // Insert comment
    const result = await query(
      `INSERT INTO comments (post_id, author_id, text)
       VALUES ($1, $2, $3)
       RETURNING id, post_id, author_id, text, created_at, updated_at`,
      [post_id, authorId, text]
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
    const { text } = req.body;

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
       SET text = $1
       WHERE id = $2
       RETURNING id, post_id, author_id, text, created_at, updated_at`,
      [text, id]
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

module.exports = {
  getCommentsByPost,
  createComment,
  updateComment,
  deleteComment
};

