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

    // Get all saved versions from post_versions table
    const savedVersions = await query(
      `SELECT id, post_id, version_number, title, content, prompt_section, description_section, 
              llm_tag, is_prompt_post, anonymous, created_at, created_by
       FROM post_versions
       WHERE post_id = $1
       ORDER BY version_number DESC`,
      [postId]
    );

    // Get current post state
    const currentPost = await query(
      `SELECT id, author_id as post_id, title, content, prompt_section, description_section, 
              llm_tag, is_prompt_post, anonymous, updated_at as created_at, author_id as created_by
       FROM posts
       WHERE id = $1`,
      [postId]
    );

    // Get the max version number
    const maxVersion = savedVersions.rows.length > 0 
      ? Math.max(...savedVersions.rows.map(v => v.version_number))
      : 0;

    // Combine versions: current state first (as latest), then saved versions
    const allVersions = [];
    
    // Only add current post state as a separate version if:
    // 1. There are saved versions (meaning edits have been made)
    // 2. AND the current state is different from the latest saved version
    if (currentPost.rows.length > 0 && savedVersions.rows.length > 0) {
      const current = currentPost.rows[0];
      const latestSaved = savedVersions.rows[0]; // Already sorted DESC, so first is latest
      
      // Compare current state with latest saved version
      // Check if any field has changed
      const hasChanged = 
        current.title !== latestSaved.title ||
        current.content !== latestSaved.content ||
        current.prompt_section !== latestSaved.prompt_section ||
        current.description_section !== latestSaved.description_section ||
        current.llm_tag !== latestSaved.llm_tag ||
        current.is_prompt_post !== latestSaved.is_prompt_post ||
        current.anonymous !== latestSaved.anonymous;
      
      // Only add current as separate version if it's different from latest saved version
      if (hasChanged) {
        const currentVersionNumber = maxVersion + 1;
        allVersions.push({
          id: current.id, // Use post ID as version ID for current state
          post_id: current.post_id,
          version_number: currentVersionNumber,
          title: current.title,
          content: current.content,
          prompt_section: current.prompt_section,
          description_section: current.description_section,
          llm_tag: current.llm_tag,
          is_prompt_post: current.is_prompt_post,
          anonymous: current.anonymous,
          created_at: current.created_at,
          created_by: current.created_by,
          is_current: true // Flag to indicate this is the current version
        });
      }
    }
    
    // Add all saved versions
    allVersions.push(...savedVersions.rows.map(v => ({ ...v, is_current: false })));

    res.json({
      versions: allVersions,
      count: allVersions.length
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

    // Check if trying to revert to current version (versionId is the post ID)
    let version;
    if (versionId === postId) {
      // Reverting to current version - get from posts table
      const currentPost = await query(
        `SELECT title, content, prompt_section, description_section, llm_tag, is_prompt_post, anonymous
         FROM posts WHERE id = $1`,
        [postId]
      );
      
      if (currentPost.rows.length === 0) {
        return res.status(404).json({
          error: 'Post not found'
        });
      }
      
      version = currentPost.rows[0];
    } else {
      // Reverting to a saved version - get from post_versions table
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

      version = versionResult.rows[0];
    }

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

