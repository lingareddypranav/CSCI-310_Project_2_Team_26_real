/**
 * Authentication Controller
 * Handles user registration, login, token validation, and logout
 */

const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { query } = require('../config/database');

// Register new user
const register = async (req, res) => {
  try {
    const { name, email, student_id, password } = req.body;

    // Validation
    if (!name || !email || !student_id || !password) {
      return res.status(400).json({
        error: 'Missing required fields',
        message: 'Name, email, student_id, and password are required'
      });
    }

    // Validate USC email
    if (!email.endsWith('@usc.edu')) {
      return res.status(400).json({
        error: 'Invalid email',
        message: 'Email must be a USC email (@usc.edu)'
      });
    }

    // Validate student ID (10 digits)
    if (!/^\d{10}$/.test(student_id)) {
      return res.status(400).json({
        error: 'Invalid student ID',
        message: 'Student ID must be exactly 10 digits'
      });
    }

    // Validate password (at least 6 characters)
    if (password.length < 6) {
      return res.status(400).json({
        error: 'Invalid password',
        message: 'Password must be at least 6 characters'
      });
    }

    // Check if email or student_id already exists
    const existingUser = await query(
      'SELECT id FROM users WHERE email = $1 OR student_id = $2',
      [email, student_id]
    );

    if (existingUser.rows.length > 0) {
      return res.status(409).json({
        error: 'User already exists',
        message: 'Email or Student ID already registered'
      });
    }

    // Hash password
    const saltRounds = 10;
    const password_hash = await bcrypt.hash(password, saltRounds);

    // Insert user
    const result = await query(
      `INSERT INTO users (name, email, student_id, password_hash)
       VALUES ($1, $2, $3, $4)
       RETURNING id, name, email, student_id, created_at, has_profile`,
      [name, email, student_id, password_hash]
    );

    const user = result.rows[0];

    res.status(201).json({
      message: 'User registered successfully',
      userId: user.id,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        student_id: user.student_id,
        has_profile: user.has_profile
      }
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to register user'
    });
  }
};

// Login user
const login = async (req, res) => {
  try {
    const { email, password } = req.body;

    // Validation
    if (!email || !password) {
      return res.status(400).json({
        error: 'Missing credentials',
        message: 'Email and password are required'
      });
    }

    // Find user by email
    const result = await query(
      'SELECT id, name, email, student_id, password_hash, has_profile, created_at FROM users WHERE email = $1',
      [email]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({
        error: 'Invalid credentials',
        message: 'Email or password is incorrect'
      });
    }

    const user = result.rows[0];

    // Verify password
    const isValidPassword = await bcrypt.compare(password, user.password_hash);

    if (!isValidPassword) {
      return res.status(401).json({
        error: 'Invalid credentials',
        message: 'Email or password is incorrect'
      });
    }

    // Generate JWT token
    const token = jwt.sign(
      {
        userId: user.id,
        email: user.email,
        student_id: user.student_id
      },
      process.env.JWT_SECRET || 'default_secret',
      { expiresIn: '7d' }
    );

    // Return user and token
    res.json({
      message: 'Login successful',
      token: token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        student_id: user.student_id,
        has_profile: user.has_profile,
        created_at: user.created_at
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: 'Failed to login'
    });
  }
};

// Validate token
const validateToken = async (req, res) => {
  try {
    // User is already attached to req by authenticateToken middleware
    const userId = req.user.userId;

    // Get fresh user data
    const result = await query(
      'SELECT id, name, email, student_id, has_profile, created_at FROM users WHERE id = $1',
      [userId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        error: 'User not found',
        valid: false
      });
    }

    const user = result.rows[0];

    res.json({
      valid: true,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        student_id: user.student_id,
        has_profile: user.has_profile,
        created_at: user.created_at
      }
    });
  } catch (error) {
    console.error('Token validation error:', error);
    res.status(500).json({
      error: 'Internal server error',
      valid: false
    });
  }
};

// Logout user
const logout = async (req, res) => {
  // Since we're using JWT tokens, logout is handled client-side
  // by removing the token. However, we can implement token blacklisting
  // if needed in the future.
  res.json({
    message: 'Logout successful'
  });
};

module.exports = {
  register,
  login,
  validateToken,
  logout
};

