# BestLLM Backend API

Node.js + Express + PostgreSQL REST API for the BestLLM Android application.

## 🚀 Quick Start

### Prerequisites
- Node.js (v16 or higher)
- PostgreSQL (for local development)
- Railway account (for deployment)

### Installation

1. **Install dependencies:**
   ```bash
   cd backend
   npm install
   ```

2. **Setup environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start the server:**
   - Database migrations run automatically on server startup
   - No manual database setup needed!
   ```bash
   # Development (with auto-reload)
   npm run dev
   
   # Production
   npm start
   ```

The server will start on `http://localhost:3000` (or the PORT specified in .env)

## 📁 Project Structure

```
backend/
├── config/
│   └── database.js          # PostgreSQL connection
├── controllers/              # Business logic
│   ├── authController.js
│   ├── profileController.js
│   ├── postController.js
│   ├── commentController.js
│   └── voteController.js
├── database/
│   ├── schema.sql           # Database schema
│   ├── seed.sql             # Seed data
│   ├── autoMigrate.js       # Auto-migration on startup
│   └── README.md            # Database setup guide
├── middleware/
│   └── auth.js              # JWT authentication
├── routes/                  # API routes
│   ├── auth.js
│   ├── profile.js
│   ├── posts.js
│   ├── comments.js
│   └── votes.js
├── server.js                # Main entry point
├── package.json
└── README.md
```

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/validate` - Validate JWT token
- `POST /api/auth/logout` - Logout user

### Profiles
- `POST /api/profile/create` - Create profile (auth required)
- `GET /api/profile/:userId` - Get profile
- `PUT /api/profile/:userId` - Update profile (auth required)
- `POST /api/profile/reset-password` - Reset password (auth required)

### Posts
- `GET /api/posts` - Get all posts (with sorting/filtering)
- `GET /api/posts/prompts` - Get prompt posts only
- `GET /api/posts/trending` - Get trending posts
- `GET /api/posts/search` - Search posts
- `GET /api/posts/:id` - Get single post
- `POST /api/posts` - Create post (auth required)
- `PUT /api/posts/:id` - Update post (auth required)
- `DELETE /api/posts/:id` - Delete post (auth required)

### Comments
- `GET /api/comments/:postId` - Get comments for a post
- `POST /api/comments` - Create comment (auth required)
- `PUT /api/comments/:id` - Update comment (auth required)
- `DELETE /api/comments/:id` - Delete comment (auth required)

### Votes
- `POST /api/votes/post/:postId` - Vote on post (auth required)
- `POST /api/votes/comment/:commentId` - Vote on comment (auth required)
- `DELETE /api/votes/post/:postId` - Remove post vote (auth required)
- `DELETE /api/votes/comment/:commentId` - Remove comment vote (auth required)
- `GET /api/votes/post/:postId` - Get post vote counts
- `GET /api/votes/comment/:commentId` - Get comment vote counts

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## 🚢 Railway Deployment

1. **Create Railway project:**
   - Go to [Railway](https://railway.app)
   - Create new project
   - Add PostgreSQL database service

2. **Set environment variables:**
   - See `RAILWAY_ENV_VARS.md` for detailed instructions
   - **Required:** `JWT_SECRET` (generate: `openssl rand -base64 32`)
   - **Recommended:** `NODE_ENV=production`
   - **Auto-set by Railway:** `DATABASE_URL`, `PORT` (no action needed)

3. **Deploy:**
   - Connect your GitHub repository
   - Railway will auto-deploy on push
   - **Database migrations run automatically on server startup!**
   - No manual database setup needed

## 📝 Environment Variables

```env
# Server
PORT=3000
NODE_ENV=development

# Database (for local dev)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bestllm_db
DB_USER=postgres
DB_PASSWORD=your_password

# Or use Railway's DATABASE_URL (recommended for Railway)
DATABASE_URL=postgresql://user:password@host:port/database

# Auto-Migration (enabled by default)
# Set DISABLE_AUTO_MIGRATE=true to disable automatic migrations
# DISABLE_AUTO_MIGRATE=false

# JWT
JWT_SECRET=your_super_secret_key

# CORS
CORS_ORIGIN=http://localhost:3000
```

## 🧪 Testing

Test the API using curl or Postman:

```bash
# Health check
curl http://localhost:3000/api/health

# Register user
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@usc.edu","student_id":"1234567890","password":"password123"}'
```

## 🔄 Auto-Migration

The backend automatically runs database migrations on every server startup:

- **First deployment:** Creates all database tables automatically
- **Subsequent deployments:** Checks if tables exist and skips if already created
- **Safe:** Idempotent - can run multiple times without issues
- **Disable:** Set `DISABLE_AUTO_MIGRATE=true` environment variable

No manual database setup needed! Just deploy and the database will be ready.

## 📚 Documentation

For detailed API documentation, see the individual controller files or use Postman to explore the endpoints.

## 🐛 Troubleshooting

- **Database connection errors:** Check your `.env` file and database credentials
- **JWT errors:** Ensure `JWT_SECRET` is set
- **CORS errors:** Update `CORS_ORIGIN` in `.env` to match your frontend URL

## 📄 License

ISC

