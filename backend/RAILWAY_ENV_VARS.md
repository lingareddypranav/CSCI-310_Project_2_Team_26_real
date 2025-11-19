# Railway Environment Variables Guide

## Quick Answer

**Yes, you need to set environment variables in Railway**, but Railway automatically sets some for you!

## Environment Variables Breakdown

### ✅ Automatically Set by Railway (You Don't Need to Do Anything)

1. **`DATABASE_URL`** 
   - Railway automatically sets this when you add PostgreSQL service
   - Format: `postgresql://user:password@host:port/database`
   - **You don't need to create this manually!**

2. **`PORT`**
   - Railway automatically sets the port
   - Your code has a fallback to `3000` if not set
   - **You don't need to create this manually!**

### 🔒 Required for Production (You Must Set These)

1. **`JWT_SECRET`** ⚠️ **REQUIRED FOR SECURITY**
   - **Why:** Currently uses `'default_secret'` as fallback, which is insecure
   - **What to set:** Generate a strong random string
   - **How to generate:** 
     ```bash
     openssl rand -base64 32
     ```
     Or use: https://randomkeygen.com/ (use "CodeIgniter Encryption Keys")
   - **Example value:** `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6`
   - **Where to set:** Railway → Your Backend Service → Variables tab

2. **`NODE_ENV`** ✅ **RECOMMENDED**
   - **Why:** Enables production optimizations (SSL, error handling, logging)
   - **What to set:** `production`
   - **Where to set:** Railway → Your Backend Service → Variables tab

### 📝 Optional (But Recommended)

3. **`CORS_ORIGIN`**
   - **Current:** Defaults to `'*'` (allows all origins)
   - **Recommended:** Set to your Android app's API URL or your domain
   - **Example:** `https://your-app.railway.app` or `*` (for development)
   - **Where to set:** Railway → Your Backend Service → Variables tab

4. **`DISABLE_AUTO_MIGRATE`**
   - **Default:** Auto-migration is enabled (leave unset)
   - **Only set if:** You want to disable automatic database migrations
   - **Value:** `true` (to disable) or leave unset (to enable)

## Step-by-Step: Setting Environment Variables in Railway

### Method 1: Railway Dashboard (Recommended)

1. **Go to Railway Dashboard:**
   - Open [railway.app](https://railway.app)
   - Select your project
   - Click on your **Backend Service** (not the PostgreSQL service)

2. **Open Variables Tab:**
   - Click on **"Variables"** tab (or **"Settings"** → **"Variables"**)

3. **Add Variables:**
   - Click **"New Variable"** or **"+"** button
   - Add each variable:

   **Variable 1:**
   - **Name:** `JWT_SECRET`
   - **Value:** (paste your generated secret key)
   - Click **"Add"**

   **Variable 2:**
   - **Name:** `NODE_ENV`
   - **Value:** `production`
   - Click **"Add"**

   **Variable 3 (Optional):**
   - **Name:** `CORS_ORIGIN`
   - **Value:** `*` (or your specific domain)
   - Click **"Add"**

4. **Verify DATABASE_URL:**
   - Railway should already have `DATABASE_URL` set automatically
   - If you see it, don't modify it!
   - If you don't see it, make sure PostgreSQL service is connected

### Method 2: Railway CLI (If Installed)

```bash
railway variables set JWT_SECRET=your_secret_here
railway variables set NODE_ENV=production
railway variables set CORS_ORIGIN=*
```

## Minimum Required Setup

For the app to work securely, you **must** set at minimum:

```
JWT_SECRET=<your-generated-secret>
NODE_ENV=production
```

Everything else either has safe defaults or is auto-set by Railway.

## Generate JWT_SECRET Now

Run this command to generate a secure JWT secret:

```bash
openssl rand -base64 32
```

Or if you don't have openssl:
```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

Copy the output and use it as your `JWT_SECRET` value.

## What Happens If You Don't Set Them?

- **DATABASE_URL:** Railway sets this automatically ✅
- **PORT:** Railway sets this automatically ✅  
- **JWT_SECRET:** Uses insecure `'default_secret'` ⚠️ **SECURITY RISK**
- **NODE_ENV:** Uses `'development'` (less secure, verbose logging) ⚠️
- **CORS_ORIGIN:** Uses `'*'` (allows all origins) ⚠️ **Less secure but works**

## Summary

**Minimum for Production:**
1. ✅ `DATABASE_URL` - Auto-set by Railway
2. ✅ `PORT` - Auto-set by Railway  
3. 🔒 `JWT_SECRET` - **YOU MUST SET THIS**
4. 🔒 `NODE_ENV=production` - **YOU SHOULD SET THIS**

**Total: You only need to manually set 2 variables!**

## Verification

After setting variables, Railway will automatically redeploy. Check the logs to verify:

1. Look for: `✅ Connected to PostgreSQL database`
2. Look for: `✅ Database schema already exists` or migration logs
3. Look for: `🚀 BestLLM API server running on port...`

If you see errors about JWT or database connection, check your environment variables.

