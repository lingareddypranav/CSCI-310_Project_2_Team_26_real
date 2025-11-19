# Database Setup Instructions

## PostgreSQL Setup on Railway

### Step 1: Create PostgreSQL Service on Railway

1. Go to [Railway](https://railway.app)
2. Create a new project
3. Click "New" → "Database" → "Add PostgreSQL"
4. Railway will automatically create a PostgreSQL database

### Step 2: Get Connection Details

Railway will provide you with:
- `DATABASE_URL` - Complete connection string
- Or individual variables: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

### Step 3: Run Schema Migration

You can run the schema in one of two ways:

**Option A: Using Railway's PostgreSQL Dashboard**
1. Go to your PostgreSQL service on Railway
2. Click "Query" tab
3. Copy and paste the contents of `schema.sql`
4. Click "Run"

**Option B: Using psql command line**
```bash
# Connect to Railway PostgreSQL
psql $DATABASE_URL

# Run schema
\i database/schema.sql

# (Optional) Run seed data
\i database/seed.sql
```

**Option C: Using a Node.js script (recommended)**
Create a script to run migrations programmatically.

### Step 4: Update Environment Variables

In your Railway project, add these environment variables:
- `DATABASE_URL` - (Already set by Railway)
- `DB_HOST` - (Extracted from DATABASE_URL if needed)
- `DB_PORT` - (Extracted from DATABASE_URL if needed)
- `DB_NAME` - (Extracted from DATABASE_URL if needed)
- `DB_USER` - (Extracted from DATABASE_URL if needed)
- `DB_PASSWORD` - (Extracted from DATABASE_URL if needed)

Railway's `DATABASE_URL` format:
```
postgresql://user:password@host:port/database
```

Our `database.js` config will use `DATABASE_URL` if available, which is perfect for Railway!

## Local Development Setup

If you want to test locally:

1. Install PostgreSQL locally
2. Create a database:
   ```sql
   CREATE DATABASE bestllm_db;
   ```
3. Create `.env` file in backend directory:
   ```env
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=bestllm_db
   DB_USER=postgres
   DB_PASSWORD=your_local_password
   ```
4. Run schema:
   ```bash
   psql -U postgres -d bestllm_db -f database/schema.sql
   ```

## Schema Overview

- **users**: User accounts with USC email and student ID
- **profiles**: Extended user profile information
- **tags**: LLM tags (ChatGPT, GPT-4, etc.)
- **posts**: User posts about LLMs
- **comments**: Comments on posts
- **votes**: Upvotes/downvotes on posts and comments

