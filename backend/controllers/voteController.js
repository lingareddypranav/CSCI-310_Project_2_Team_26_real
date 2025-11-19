/**
 * Vote Controller
 * Handles voting on posts and comments (upvote/downvote)
 */

const { query, getClient } = require('../config/database');

// Vote on a post
const votePost = async (req, res) => {
  try {
    const { postId } = req.params;
    const userId = req.user.userId;
    const { type } = req.body; // 'up' or 'down'

    // Validation
    if (!type || (type !== 'up' && type !== 'down')) {
      return res.status(400).json({
        error: 'Invalid vote type',
        message: 'Vote type must be "up" or "down"'
      });
    }

    // Verify post exists
    const postCheck = await query(
      'SELECT id FROM posts WHERE id = $1',
      [postId]
    );

    if (postCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Post not found'
      });
    }

    // Check if user already voted on this post
    const existingVote = await query(
      'SELECT id, type FROM votes WHERE user_id = $1 AND post_id = $2',
      [userId, postId]
    );

    const client = await getClient();
    try {
      await client.query('BEGIN');

      if (existingVote.rows.length > 0) {
        // Update existing vote
        if (existingVote.rows[0].type === type) {
          // Same vote type, remove vote (toggle)
          await client.query(
            'DELETE FROM votes WHERE id = $1',
            [existingVote.rows[0].id]
          );
          await client.query('COMMIT');
          return res.json({
            message: 'Vote removed',
            action: 'removed'
          });
        } else {
          // Different vote type, update vote
          await client.query(
            'UPDATE votes SET type = $1 WHERE id = $2',
            [type, existingVote.rows[0].id]
          );
          await client.query('COMMIT');
          return res.json({
            message: 'Vote updated',
            action: 'updated',
            type: type
          });
        }
      } else {
        // Create new vote
        await client.query(
          `INSERT INTO votes (user_id, post_id, type)
           VALUES ($1, $2, $3)`,
          [userId, postId, type]
        );
        await client.query('COMMIT');
        return res.json({
          message: 'Vote created',
          action: 'created',
          type: type
        });
      }
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  } catch (error) {
    console.error('Vote post error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to vote on post'
    });
  }
};

// Vote on a comment
const voteComment = async (req, res) => {
  try {
    const { commentId } = req.params;
    const userId = req.user.userId;
    const { type } = req.body; // 'up' or 'down'

    // Validation
    if (!type || (type !== 'up' && type !== 'down')) {
      return res.status(400).json({
        error: 'Invalid vote type',
        message: 'Vote type must be "up" or "down"'
      });
    }

    // Verify comment exists
    const commentCheck = await query(
      'SELECT id FROM comments WHERE id = $1',
      [commentId]
    );

    if (commentCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Comment not found'
      });
    }

    // Check if user already voted on this comment
    const existingVote = await query(
      'SELECT id, type FROM votes WHERE user_id = $1 AND comment_id = $2',
      [userId, commentId]
    );

    const client = await getClient();
    try {
      await client.query('BEGIN');

      if (existingVote.rows.length > 0) {
        // Update existing vote
        if (existingVote.rows[0].type === type) {
          // Same vote type, remove vote (toggle)
          await client.query(
            'DELETE FROM votes WHERE id = $1',
            [existingVote.rows[0].id]
          );
          await client.query('COMMIT');
          return res.json({
            message: 'Vote removed',
            action: 'removed'
          });
        } else {
          // Different vote type, update vote
          await client.query(
            'UPDATE votes SET type = $1 WHERE id = $2',
            [type, existingVote.rows[0].id]
          );
          await client.query('COMMIT');
          return res.json({
            message: 'Vote updated',
            action: 'updated',
            type: type
          });
        }
      } else {
        // Create new vote
        await client.query(
          `INSERT INTO votes (user_id, comment_id, type)
           VALUES ($1, $2, $3)`,
          [userId, commentId, type]
        );
        await client.query('COMMIT');
        return res.json({
          message: 'Vote created',
          action: 'created',
          type: type
        });
      }
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  } catch (error) {
    console.error('Vote comment error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to vote on comment'
    });
  }
};

// Remove vote from post
const removePostVote = async (req, res) => {
  try {
    const { postId } = req.params;
    const userId = req.user.userId;

    const result = await query(
      'DELETE FROM votes WHERE user_id = $1 AND post_id = $2',
      [userId, postId]
    );

    if (result.rowCount === 0) {
      return res.status(404).json({
        error: 'Vote not found'
      });
    }

    res.json({
      message: 'Vote removed successfully'
    });
  } catch (error) {
    console.error('Remove post vote error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to remove vote'
    });
  }
};

// Remove vote from comment
const removeCommentVote = async (req, res) => {
  try {
    const { commentId } = req.params;
    const userId = req.user.userId;

    const result = await query(
      'DELETE FROM votes WHERE user_id = $1 AND comment_id = $2',
      [userId, commentId]
    );

    if (result.rowCount === 0) {
      return res.status(404).json({
        error: 'Vote not found'
      });
    }

    res.json({
      message: 'Vote removed successfully'
    });
  } catch (error) {
    console.error('Remove comment vote error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to remove vote'
    });
  }
};

// Get vote counts for a post
const getPostVoteCounts = async (req, res) => {
  try {
    const { postId } = req.params;

    const result = await query(
      `SELECT 
        COUNT(CASE WHEN type = 'up' THEN 1 END)::INTEGER as upvotes,
        COUNT(CASE WHEN type = 'down' THEN 1 END)::INTEGER as downvotes
      FROM votes
      WHERE post_id = $1`,
      [postId]
    );

    res.json({
      upvotes: result.rows[0].upvotes || 0,
      downvotes: result.rows[0].downvotes || 0,
      total: (result.rows[0].upvotes || 0) + (result.rows[0].downvotes || 0)
    });
  } catch (error) {
    console.error('Get post vote counts error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get vote counts'
    });
  }
};

// Get vote counts for a comment
const getCommentVoteCounts = async (req, res) => {
  try {
    const { commentId } = req.params;

    const result = await query(
      `SELECT 
        COUNT(CASE WHEN type = 'up' THEN 1 END)::INTEGER as upvotes,
        COUNT(CASE WHEN type = 'down' THEN 1 END)::INTEGER as downvotes
      FROM votes
      WHERE comment_id = $1`,
      [commentId]
    );

    res.json({
      upvotes: result.rows[0].upvotes || 0,
      downvotes: result.rows[0].downvotes || 0,
      total: (result.rows[0].upvotes || 0) + (result.rows[0].downvotes || 0)
    });
  } catch (error) {
    console.error('Get comment vote counts error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get vote counts'
    });
  }
};

module.exports = {
  votePost,
  voteComment,
  removePostVote,
  removeCommentVote,
  getPostVoteCounts,
  getCommentVoteCounts
};

