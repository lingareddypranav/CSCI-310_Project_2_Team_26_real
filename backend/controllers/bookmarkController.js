/**
 * Bookmark Controller
 * Handles bookmark operations for posts
 */

const { query } = require('../config/database');

// Add bookmark
const addBookmark = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { postId } = req.body;

    if (!postId) {
      return res.status(400).json({
        error: 'Missing required field',
        message: 'Post ID is required'
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

    // Check if bookmark already exists
    const existing = await query(
      'SELECT id FROM bookmarks WHERE user_id = $1 AND post_id = $2',
      [userId, postId]
    );

    if (existing.rows.length > 0) {
      return res.status(409).json({
        error: 'Bookmark already exists',
        message: 'Post is already bookmarked'
      });
    }

    // Create bookmark
    const result = await query(
      `INSERT INTO bookmarks (user_id, post_id)
       VALUES ($1, $2)
       RETURNING id, user_id, post_id, created_at`,
      [userId, postId]
    );

    res.status(201).json({
      message: 'Bookmark added successfully',
      bookmark: result.rows[0]
    });
  } catch (error) {
    console.error('Add bookmark error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to add bookmark'
    });
  }
};

// Remove bookmark
const removeBookmark = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { postId } = req.params;

    const result = await query(
      'DELETE FROM bookmarks WHERE user_id = $1 AND post_id = $2',
      [userId, postId]
    );

    if (result.rowCount === 0) {
      return res.status(404).json({
        error: 'Bookmark not found'
      });
    }

    res.json({
      message: 'Bookmark removed successfully'
    });
  } catch (error) {
    console.error('Remove bookmark error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to remove bookmark'
    });
  }
};

// Get user's bookmarks
const getBookmarks = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { is_prompt_post } = req.query;

    let queryText = `
      SELECT 
        p.id,
        p.author_id,
        u.name as author_name,
        p.title,
        p.content,
        p.prompt_section,
        p.description_section,
        p.llm_tag,
        p.is_prompt_post,
        p.anonymous,
        p.created_at,
        p.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id)::INTEGER as comment_count,
        uv.type as user_vote_type,
        b.created_at as bookmarked_at
      FROM bookmarks b
      INNER JOIN posts p ON b.post_id = p.id
      LEFT JOIN users u ON p.author_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id
      LEFT JOIN votes uv ON uv.post_id = p.id AND uv.user_id = $1
      WHERE b.user_id = $1
    `;

    const params = [userId];
    let paramCount = 2;

    if (is_prompt_post !== undefined) {
      queryText += ` AND p.is_prompt_post = $${paramCount++}`;
      params.push(is_prompt_post === 'true');
    }

    queryText += ` GROUP BY p.id, u.name, uv.type, b.created_at
      ORDER BY b.created_at DESC`;

    const result = await query(queryText, params);

    res.json({
      posts: result.rows,
      count: result.rows.length
    });
  } catch (error) {
    console.error('Get bookmarks error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get bookmarks'
    });
  }
};

// Check if post is bookmarked
const isBookmarked = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { postId } = req.params;

    const result = await query(
      'SELECT id FROM bookmarks WHERE user_id = $1 AND post_id = $2',
      [userId, postId]
    );

    res.json({
      bookmarked: result.rows.length > 0
    });
  } catch (error) {
    console.error('Check bookmark error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to check bookmark status'
    });
  }
};

module.exports = {
  addBookmark,
  removeBookmark,
  getBookmarks,
  isBookmarked
};

