/**
 * Profile Controller
 * Handles profile creation, retrieval, updates, and password reset
 */

const bcrypt = require('bcrypt');
const { query } = require('../config/database');

// Create profile
const createProfile = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { affiliation, birth_date, bio, interests, profile_picture_url } = req.body;

    // Validation
    if (!affiliation || !bio) {
      return res.status(400).json({
        error: 'Missing required fields',
        message: 'Affiliation and bio are required'
      });
    }

    // Check if profile already exists
    const existingProfile = await query(
      'SELECT user_id FROM profiles WHERE user_id = $1',
      [userId]
    );

    if (existingProfile.rows.length > 0) {
      return res.status(409).json({
        error: 'Profile already exists',
        message: 'User already has a profile. Use update endpoint instead.'
      });
    }

    // Get user info for profile
    const userResult = await query(
      'SELECT name, email FROM users WHERE id = $1',
      [userId]
    );

    if (userResult.rows.length === 0) {
      return res.status(404).json({
        error: 'User not found'
      });
    }

    const user = userResult.rows[0];

    // Insert profile
    await query(
      `INSERT INTO profiles (user_id, name, email, affiliation, birth_date, bio, interests, profile_picture_url)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
      [userId, user.name, user.email, affiliation, birth_date || null, bio, interests || null, profile_picture_url || null]
    );

    // Update user has_profile flag
    await query(
      'UPDATE users SET has_profile = TRUE WHERE id = $1',
      [userId]
    );

    res.status(201).json({
      message: 'Profile created successfully'
    });
  } catch (error) {
    console.error('Create profile error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to create profile'
    });
  }
};

// Get profile by user ID
const getProfile = async (req, res) => {
  try {
    const { userId } = req.params;

    const result = await query(
      `SELECT user_id, name, email, affiliation, birth_date, bio, interests, 
              profile_picture_url, created_at, updated_at
       FROM profiles WHERE user_id = $1`,
      [userId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        error: 'Profile not found',
        message: 'User profile does not exist'
      });
    }

    const profile = result.rows[0];

    res.json({
      user_id: profile.user_id,
      name: profile.name,
      email: profile.email,
      affiliation: profile.affiliation,
      birth_date: profile.birth_date,
      bio: profile.bio,
      interests: profile.interests,
      profile_picture_url: profile.profile_picture_url,
      created_at: profile.created_at,
      updated_at: profile.updated_at
    });
  } catch (error) {
    console.error('Get profile error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to get profile'
    });
  }
};

// Update profile
const updateProfile = async (req, res) => {
  try {
    const { userId } = req.params;
    const authenticatedUserId = req.user.userId;

    // Check if user owns this profile
    if (userId !== authenticatedUserId) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'You can only update your own profile'
      });
    }

    const { birth_date, bio, interests, profile_picture_url } = req.body;

    // Build update query dynamically (only update provided fields)
    const updates = [];
    const values = [];
    let paramCount = 1;

    if (birth_date !== undefined) {
      updates.push(`birth_date = $${paramCount++}`);
      values.push(birth_date);
    }
    if (bio !== undefined) {
      updates.push(`bio = $${paramCount++}`);
      values.push(bio);
    }
    if (interests !== undefined) {
      updates.push(`interests = $${paramCount++}`);
      values.push(interests);
    }
    if (profile_picture_url !== undefined) {
      updates.push(`profile_picture_url = $${paramCount++}`);
      values.push(profile_picture_url);
    }

    if (updates.length === 0) {
      return res.status(400).json({
        error: 'No fields to update',
        message: 'Provide at least one field to update'
      });
    }

    // Add user_id to values for WHERE clause
    values.push(userId);

    const queryText = `
      UPDATE profiles 
      SET ${updates.join(', ')}
      WHERE user_id = $${paramCount}
      RETURNING user_id, name, email, affiliation, birth_date, bio, interests, profile_picture_url, updated_at
    `;

    const result = await query(queryText, values);

    if (result.rows.length === 0) {
      return res.status(404).json({
        error: 'Profile not found'
      });
    }

    res.json({
      message: 'Profile updated successfully',
      profile: result.rows[0]
    });
  } catch (error) {
    console.error('Update profile error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to update profile'
    });
  }
};

// Reset password
const resetPassword = async (req, res) => {
  try {
    const userId = req.user.userId;
    const { current_password, new_password } = req.body;

    // Validation
    if (!current_password || !new_password) {
      return res.status(400).json({
        error: 'Missing required fields',
        message: 'Current password and new password are required'
      });
    }

    if (new_password.length < 6) {
      return res.status(400).json({
        error: 'Invalid password',
        message: 'New password must be at least 6 characters'
      });
    }

    // Get current password hash
    const userResult = await query(
      'SELECT password_hash FROM users WHERE id = $1',
      [userId]
    );

    if (userResult.rows.length === 0) {
      return res.status(404).json({
        error: 'User not found'
      });
    }

    // Verify current password
    const isValidPassword = await bcrypt.compare(
      current_password,
      userResult.rows[0].password_hash
    );

    if (!isValidPassword) {
      return res.status(401).json({
        error: 'Invalid password',
        message: 'Current password is incorrect'
      });
    }

    // Hash new password
    const saltRounds = 10;
    const newPasswordHash = await bcrypt.hash(new_password, saltRounds);

    // Update password
    await query(
      'UPDATE users SET password_hash = $1 WHERE id = $2',
      [newPasswordHash, userId]
    );

    res.json({
      message: 'Password reset successfully'
    });
  } catch (error) {
    console.error('Reset password error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to reset password'
    });
  }
};

module.exports = {
  createProfile,
  getProfile,
  updateProfile,
  resetPassword
};

