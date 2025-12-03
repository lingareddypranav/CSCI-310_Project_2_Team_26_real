/**
 * Version Controller
 * Handles post version history operations
 */

const { query } = require('../config/database');

// Get all versions for a post
const getPostVersions = async (req, res) => {
  try {
    const { postId } = req.params;
    const userId = req.user.userId;

    // Verify post exists and user is author
    const postCheck = await query(
      'SELECT author_id FROM posts WHERE id = $1',
      [postId]
    );

    if (postCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Post not found'
      });
    }

    if (postCheck.rows[0].author_id !== userId) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'You can only view versions of your own posts'
      });
    }

    // Get all versions
    const result = await query(
      `SELECT id, post_id, version_number, title, content, prompt_section, description_section, 
              llm_tag, is_prompt_post, anonymous, created_at, created_by
       FROM post_versions
       WHERE post_id = $1
       ORDER BY version_number DESC`,
      [postId]
    );

    res.json({
      versions: result.rows,
      count: result.rows.length
    });
  } catch (error) {
    console.error('Get post versions error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get post versions'
    });
  }
};

// Revert post to a specific version
const revertToVersion = async (req, res) => {
  try {
    const { postId, versionId } = req.params;
    const userId = req.user.userId;

    // Verify post exists and user is author
    const postCheck = await query(
      'SELECT author_id FROM posts WHERE id = $1',
      [postId]
    );

    if (postCheck.rows.length === 0) {
      return res.status(404).json({
        error: 'Post not found'
      });
    }

    if (postCheck.rows[0].author_id !== userId) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'You can only revert your own posts'
      });
    }

    // Get version data
    const versionResult = await query(
      `SELECT title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous
       FROM post_versions
       WHERE id = $1 AND post_id = $2`,
      [versionId, postId]
    );

    if (versionResult.rows.length === 0) {
      return res.status(404).json({
        error: 'Version not found'
      });
    }

    const version = versionResult.rows[0];

    // Get current post data to save as new version before reverting
    const currentPost = await query(
      `SELECT title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous
       FROM posts WHERE id = $1`,
      [postId]
    );

    if (currentPost.rows.length > 0) {
      // Get next version number
      const versionNumResult = await query(
        'SELECT COALESCE(MAX(version_number), 0) + 1 as next_version FROM post_versions WHERE post_id = $1',
        [postId]
      );
      const nextVersion = versionNumResult.rows[0].next_version;

      // Save current version before reverting
      await query(
        `INSERT INTO post_versions (post_id, version_number, title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous, created_by)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)`,
        [
          postId,
          nextVersion,
          currentPost.rows[0].title,
          currentPost.rows[0].content,
          currentPost.rows[0].prompt_section,
          currentPost.rows[0].description_section,
          currentPost.rows[0].llm_tag,
          currentPost.rows[0].is_prompt_post,
          currentPost.rows[0].anonymous,
          userId
        ]
      );
    }

    // Update post with version data
    const updateResult = await query(
      `UPDATE posts 
       SET title = $1, content = $2, prompt_section = $3, description_section = $4, 
           llm_tag = $5, is_prompt_post = $6, anonymous = $7
       WHERE id = $8
       RETURNING id, author_id, title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous, created_at, updated_at`,
      [
        version.title,
        version.content,
        version.prompt_section,
        version.description_section,
        version.llm_tag,
        version.is_prompt_post,
        version.anonymous,
        postId
      ]
    );

    res.json({
      message: 'Post reverted to version successfully',
      post: updateResult.rows[0]
    });
  } catch (error) {
    console.error('Revert to version error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to revert post to version'
    });
  }
};

module.exports = {
  getPostVersions,
  revertToVersion
};

