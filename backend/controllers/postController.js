/**
 * Post Controller
 * Handles post CRUD operations, search, sorting, and trending posts
 */

const { query } = require('../config/database');

// Get all posts with optional sorting and filtering
const getPosts = async (req, res) => {
  try {
    const { sort = 'newest', limit = 50, offset = 0, is_prompt_post } = req.query;
    const userId = req.user?.userId; // Optional auth

    let queryText = `
      SELECT 
        p.id,
        p.author_id,
        u.name as author_name,
        p.title,
        p.content,
        p.llm_tag,
        p.is_prompt_post,
        p.created_at,
        p.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id)::INTEGER as comment_count
      FROM posts p
      LEFT JOIN users u ON p.author_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id
    `;

    const params = [];
    let paramCount = 1;

    // Filter by prompt post type
    if (is_prompt_post !== undefined) {
      queryText += ` WHERE p.is_prompt_post = $${paramCount++}`;
      params.push(is_prompt_post === 'true');
    }

    queryText += ` GROUP BY p.id, u.name`;

    // Sorting
    switch (sort) {
      case 'newest':
        queryText += ` ORDER BY p.created_at DESC`;
        break;
      case 'oldest':
        queryText += ` ORDER BY p.created_at ASC`;
        break;
      case 'trending':
        // Trending = upvotes - downvotes, ordered by that, then by recency
        queryText += ` ORDER BY (upvotes - downvotes) DESC, p.created_at DESC`;
        break;
      case 'top':
        // Top = most upvotes
        queryText += ` ORDER BY upvotes DESC, p.created_at DESC`;
        break;
      default:
        queryText += ` ORDER BY p.created_at DESC`;
    }

    // Pagination
    queryText += ` LIMIT $${paramCount++} OFFSET $${paramCount++}`;
    params.push(parseInt(limit), parseInt(offset));

    const result = await query(queryText, params);

    res.json({
      posts: result.rows,
      count: result.rows.length,
      limit: parseInt(limit),
      offset: parseInt(offset)
    });
  } catch (error) {
    console.error('Get posts error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get posts'
    });
  }
};

// Get prompt posts only
const getPromptPosts = async (req, res) => {
  try {
    const { sort = 'newest', limit = 50, offset = 0 } = req.query;

    let queryText = `
      SELECT 
        p.id,
        p.author_id,
        u.name as author_name,
        p.title,
        p.content,
        p.llm_tag,
        p.is_prompt_post,
        p.created_at,
        p.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id)::INTEGER as comment_count
      FROM posts p
      LEFT JOIN users u ON p.author_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id
      WHERE p.is_prompt_post = TRUE
      GROUP BY p.id, u.name
    `;

    // Sorting
    switch (sort) {
      case 'newest':
        queryText += ` ORDER BY p.created_at DESC`;
        break;
      case 'trending':
        queryText += ` ORDER BY (upvotes - downvotes) DESC, p.created_at DESC`;
        break;
      case 'top':
        queryText += ` ORDER BY upvotes DESC, p.created_at DESC`;
        break;
      default:
        queryText += ` ORDER BY p.created_at DESC`;
    }

    queryText += ` LIMIT $1 OFFSET $2`;

    const result = await query(queryText, [parseInt(limit), parseInt(offset)]);

    res.json({
      posts: result.rows,
      count: result.rows.length,
      limit: parseInt(limit),
      offset: parseInt(offset)
    });
  } catch (error) {
    console.error('Get prompt posts error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get prompt posts'
    });
  }
};

// Get trending posts (top K)
const getTrendingPosts = async (req, res) => {
  try {
    const { k = 10 } = req.query;

    const queryText = `
      SELECT 
        p.id,
        p.author_id,
        u.name as author_name,
        p.title,
        p.content,
        p.llm_tag,
        p.is_prompt_post,
        p.created_at,
        p.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id)::INTEGER as comment_count,
        (COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0) - 
         COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)) as score
      FROM posts p
      LEFT JOIN users u ON p.author_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id
      WHERE p.created_at >= NOW() - INTERVAL '7 days'
      GROUP BY p.id, u.name
      ORDER BY score DESC, p.created_at DESC
      LIMIT $1
    `;

    const result = await query(queryText, [parseInt(k)]);

    res.json({
      posts: result.rows,
      count: result.rows.length
    });
  } catch (error) {
    console.error('Get trending posts error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get trending posts'
    });
  }
};

// Search posts
const searchPosts = async (req, res) => {
  try {
    const { q, search_type = 'full_text', limit = 50, offset = 0, is_prompt_post } = req.query;

    if (!q || q.trim().length === 0) {
      return res.status(400).json({
        error: 'Missing search query',
        message: 'Query parameter "q" is required'
      });
    }

    let queryText = `
      SELECT 
        p.id,
        p.author_id,
        u.name as author_name,
        p.title,
        p.content,
        p.llm_tag,
        p.is_prompt_post,
        p.created_at,
        p.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id)::INTEGER as comment_count
      FROM posts p
      LEFT JOIN users u ON p.author_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id
      WHERE
    `;

    const params = [];
    let paramCount = 1;

    // Search type filtering
    switch (search_type) {
      case 'tag':
        queryText += ` p.llm_tag ILIKE $${paramCount++}`;
        params.push(`%${q}%`);
        break;
      case 'prompt_tag':
        // Search for prompts with specific tag
        queryText += ` p.llm_tag ILIKE $${paramCount++} AND p.is_prompt_post = TRUE`;
        params.push(`%${q}%`);
        break;
      case 'author':
        queryText += ` u.name ILIKE $${paramCount++}`;
        params.push(`%${q}%`);
        break;
      case 'title':
        queryText += ` p.title ILIKE $${paramCount++}`;
        params.push(`%${q}%`);
        break;
      case 'full_text':
      default:
        queryText += ` (p.title ILIKE $${paramCount} OR p.content ILIKE $${paramCount++})`;
        params.push(`%${q}%`);
        break;
    }

    // Add is_prompt_post filter if specified (and not already handled by prompt_tag)
    if (search_type !== 'prompt_tag' && is_prompt_post !== undefined) {
      queryText += ` AND p.is_prompt_post = $${paramCount++}`;
      params.push(is_prompt_post === 'true' || is_prompt_post === true);
    }

    queryText += ` GROUP BY p.id, u.name ORDER BY p.created_at DESC`;
    queryText += ` LIMIT $${paramCount++} OFFSET $${paramCount++}`;
    params.push(parseInt(limit), parseInt(offset));

    const result = await query(queryText, params);

    res.json({
      posts: result.rows,
      count: result.rows.length,
      query: q,
      search_type: search_type,
      limit: parseInt(limit),
      offset: parseInt(offset)
    });
  } catch (error) {
    console.error('Search posts error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to search posts'
    });
  }
};

// Get single post by ID
const getPostById = async (req, res) => {
  try {
    const { id } = req.params;

    const result = await query(
      `SELECT 
        p.id,
        p.author_id,
        u.name as author_name,
        p.title,
        p.content,
        p.llm_tag,
        p.is_prompt_post,
        p.created_at,
        p.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id)::INTEGER as comment_count
      FROM posts p
      LEFT JOIN users u ON p.author_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id
      WHERE p.id = $1
      GROUP BY p.id, u.name`,
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        error: 'Post not found'
      });
    }

    res.json({
      post: result.rows[0]
    });
  } catch (error) {
    console.error('Get post by ID error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get post'
    });
  }
};

// Create new post
const createPost = async (req, res) => {
  try {
    const authorId = req.user.userId;
    const { title, content, llm_tag, is_prompt_post } = req.body;

    // Validation
    if (!title || !content || !llm_tag) {
      return res.status(400).json({
        error: 'Missing required fields',
        message: 'Title, content, and llm_tag are required'
      });
    }

    // Insert post
    const insertResult = await query(
      `INSERT INTO posts (author_id, title, content, llm_tag, is_prompt_post)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING id`,
      [authorId, title, content, llm_tag, is_prompt_post || false]
    );

    const postId = insertResult.rows[0].id;

    // Fetch full post data with author_name, vote counts, and comment count
    const result = await query(
      `SELECT 
        p.id,
        p.author_id,
        u.name as author_name,
        p.title,
        p.content,
        p.llm_tag,
        p.is_prompt_post,
        p.created_at,
        p.updated_at,
        COALESCE(SUM(CASE WHEN v.type = 'up' THEN 1 ELSE 0 END), 0)::INTEGER as upvotes,
        COALESCE(SUM(CASE WHEN v.type = 'down' THEN 1 ELSE 0 END), 0)::INTEGER as downvotes,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id)::INTEGER as comment_count
      FROM posts p
      LEFT JOIN users u ON p.author_id = u.id
      LEFT JOIN votes v ON v.post_id = p.id
      WHERE p.id = $1
      GROUP BY p.id, u.name`,
      [postId]
    );

    res.status(201).json({
      message: 'Post created successfully',
      post: result.rows[0]
    });
  } catch (error) {
    console.error('Create post error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to create post'
    });
  }
};

// Update post
const updatePost = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.userId;
    const { title, content, llm_tag, is_prompt_post } = req.body;

    // Check if post exists and user is author
    const postCheck = await query(
      'SELECT author_id FROM posts WHERE id = $1',
      [id]
    );

    if (postCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Post not found'
      });
    }

    if (postCheck.rows[0].author_id !== userId) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'You can only edit your own posts'
      });
    }

    // Build update query
    const updates = [];
    const values = [];
    let paramCount = 1;

    if (title !== undefined) {
      updates.push(`title = $${paramCount++}`);
      values.push(title);
    }
    if (content !== undefined) {
      updates.push(`content = $${paramCount++}`);
      values.push(content);
    }
    if (llm_tag !== undefined) {
      updates.push(`llm_tag = $${paramCount++}`);
      values.push(llm_tag);
    }
    if (is_prompt_post !== undefined) {
      updates.push(`is_prompt_post = $${paramCount++}`);
      values.push(is_prompt_post);
    }

    if (updates.length === 0) {
      return res.status(400).json({
        error: 'No fields to update'
      });
    }

    values.push(id);

    const queryText = `
      UPDATE posts 
      SET ${updates.join(', ')}
      WHERE id = $${paramCount}
      RETURNING id, author_id, title, content, llm_tag, is_prompt_post, created_at, updated_at
    `;

    const result = await query(queryText, values);

    res.json({
      message: 'Post updated successfully',
      post: result.rows[0]
    });
  } catch (error) {
    console.error('Update post error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to update post'
    });
  }
};

// Delete post
const deletePost = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.userId;

    // Check if post exists and user is author
    const postCheck = await query(
      'SELECT author_id FROM posts WHERE id = $1',
      [id]
    );

    if (postCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Post not found'
      });
    }

    if (postCheck.rows[0].author_id !== userId) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'You can only delete your own posts'
      });
    }

    // Delete post (cascade will handle comments and votes)
    await query('DELETE FROM posts WHERE id = $1', [id]);

    res.json({
      message: 'Post deleted successfully'
    });
  } catch (error) {
    console.error('Delete post error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to delete post'
    });
  }
};

module.exports = {
  getPosts,
  getPromptPosts,
  getTrendingPosts,
  searchPosts,
  getPostById,
  createPost,
  updatePost,
  deletePost
};

