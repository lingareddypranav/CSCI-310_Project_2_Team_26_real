/**
 * BestLLM Backend Server
 * Node.js + Express + PostgreSQL
 * 
 * Main entry point for the BestLLM REST API
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

// Import database auto-migration
const { autoMigrate } = require('./database/autoMigrate');

// Import routes
const authRoutes = require('./routes/auth');
const profileRoutes = require('./routes/profile');
const postRoutes = require('./routes/posts');
const commentRoutes = require('./routes/comments');
const voteRoutes = require('./routes/votes');
const bookmarkRoutes = require('./routes/bookmarks');
const draftRoutes = require('./routes/drafts');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors({
  origin: process.env.CORS_ORIGIN || '*',
  credentials: true
}));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
  next();
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'ok', 
    message: 'BestLLM API is running',
    timestamp: new Date().toISOString()
  });
});

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/profile', profileRoutes);
app.use('/api/posts', postRoutes);
app.use('/api/comments', commentRoutes);
app.use('/api/votes', voteRoutes);
app.use('/api/bookmarks', bookmarkRoutes);
app.use('/api/drafts', draftRoutes);

// 404 handler
app.use((req, res) => {
  res.status(404).json({ 
    error: 'Not Found',
    message: `Route ${req.method} ${req.path} not found`
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(err.status || 500).json({
    error: err.message || 'Internal Server Error',
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  });
});

// Start server with auto-migration
async function startServer() {
  // Run database migrations automatically on startup
  // This is safe to run on every deployment - it checks if tables exist first
  if (process.env.DISABLE_AUTO_MIGRATE !== 'true') {
    try {
      await autoMigrate();
    } catch (error) {
      console.error('⚠️  Migration warning (server will continue):', error.message);
      // Continue even if migration fails - might be a temporary connection issue
    }
  } else {
    console.log('⚠️  Auto-migration disabled (DISABLE_AUTO_MIGRATE=true)');
  }

  // Start the server
  app.listen(PORT, () => {
    console.log(`🚀 BestLLM API server running on port ${PORT}`);
    console.log(`📝 Environment: ${process.env.NODE_ENV || 'development'}`);
    console.log(`🔗 Health check: http://localhost:${PORT}/api/health`);
  });
}

// Start the server
startServer().catch((error) => {
  console.error('❌ Failed to start server:', error);
  process.exit(1);
});

module.exports = app;

