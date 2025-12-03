# Deliverables Analysis - Current State vs Requirements

## ✅ **COMPLETED FEATURES**

### 1. **Anonymous Posting** ✅
- **Status**: FULLY IMPLEMENTED
- **Frontend**: 
  - `Post` model has `anonymous` boolean field
  - Create/Edit post fragments have anonymous switch
  - PostsAdapter shows "Anonymous" instead of author name when `post.isAnonymous()`
  - PostDetailFragment handles anonymous display
- **Backend**: 
  - Database schema needs `anonymous` column (check if exists)
  - API accepts `anonymous` field in create/update endpoints
- **Action Needed**: Verify backend database has `anonymous` column in posts table

### 2. **Drafts System** ✅
- **Status**: FRONTEND IMPLEMENTED (Local only)
- **Frontend**:
  - `Draft` model exists with all fields
  - `DraftsViewModel` manages drafts
  - `SavedDraftsFragment` displays drafts
  - `CreatePostFragment` can save/load drafts
  - Drafts are stored locally (in-memory/ViewModel)
- **Backend**: ❌ NOT IMPLEMENTED
- **Action Needed**: Backend implementation required for persistence

### 3. **Bookmarks** ✅
- **Status**: FRONTEND IMPLEMENTED (Local only)
- **Frontend**:
  - `BookmarkManager` manages bookmarks
  - `BookmarksFragment` displays bookmarked posts
  - Bookmark button in PostsAdapter
  - Bookmarked posts persist in memory only
- **Backend**: ❌ NOT IMPLEMENTED
- **Action Needed**: Backend implementation required for persistence

---

## ⚠️ **PARTIALLY COMPLETED / NEEDS FIXES**

### 4. **Vote Icons (Upvote/Downvote)** ⚠️
- **Status**: PARTIALLY WORKING - BACKEND ISSUE IDENTIFIED
- **Current Implementation**:
  - Icons exist: `ic_arrow_up_outline_24dp`, `ic_arrow_up_filled_24dp`, `ic_arrow_down_outline_24dp`, `ic_arrow_down_filled_24dp`
  - `PostsAdapter.updateVoteIcons()` switches between outline/filled based on `user_vote_type`
  - `CommentsAdapter` has similar logic
  - Frontend correctly updates icons locally after voting
- **Problem**: 
  - **BACKEND DOESN'T RETURN `user_vote_type`** in post/comment queries
  - Backend queries (`getPosts`, `getPostById`, `getComments`) only return vote counts, not the current user's vote
  - After reloading posts, icons reset because `user_vote_type` is null
- **Root Cause**: 
  - Backend SQL queries need LEFT JOIN with votes table to get current user's vote type
  - Need to add: `LEFT JOIN votes uv ON uv.post_id = p.id AND uv.user_id = $userId` 
  - Need to select: `uv.type as user_vote_type`
- **Files to Fix**:
  - **Backend**: `backend/controllers/postController.js` - `getPosts()`, `getPostById()`, `getPromptPosts()`, `searchPosts()`
  - **Backend**: `backend/controllers/commentController.js` - `getComments()`
  - Frontend is correct, just needs backend data
- **Action Needed**: 
  - **CRITICAL**: Update backend queries to include `user_vote_type` field
  - Add LEFT JOIN for current user's vote in all post/comment queries
  - Ensure `userId` is available from `req.user.userId` in queries

### 5. **Prompt Post Divider** ✅
- **Status**: IMPLEMENTED
- **Current Implementation**:
  - `item_post.xml` has `promptDivider` View (line 109-115)
  - `PostsAdapter` shows divider when both prompt and description sections are visible (lines 189-195)
  - Divider is a thin line (`1dp` height, `@color/secondary_text` with 0.2 alpha)
- **Action Needed**: Verify divider visibility logic is correct

### 6. **Activity Log** ⚠️
- **Status**: FUNCTIONAL BUT NEEDS UI FIXES
- **Current Implementation**:
  - `NotificationsFragment` displays user activity
  - `NotificationsViewModel` fetches posts and comments
  - Shows post titles and comment text
- **Issues to Fix**:
  1. ✅ Comments show post name (implemented in `NotificationsViewModel.updateCommentTitles()`)
  2. ⚠️ Post date resets on edit in activity log (uses `created_at` vs `updated_at`)
  3. ⚠️ Need to clean up edit post page UI
- **Action Needed**:
  - Fix date display: Use `created_at` consistently (don't show `updated_at` as creation date)
  - Verify comment post name display works correctly

### 7. **Edit Post Page** ⚠️
- **Status**: FUNCTIONAL BUT NEEDS UI IMPROVEMENTS
- **Current Implementation**:
  - `EditPostFragment` exists and works
  - Can edit title, content, tag, prompt sections, anonymous flag
  - Prompt toggle is disabled (can't change post type)
- **Issues to Fix**:
  1. ⚠️ UI doesn't match create post page styling
  2. ✅ Prompt toggle is disabled (prevents changing normal to prompt post)
  3. ✅ Shows only relevant fields based on post type (lines 155-165)
- **Action Needed**:
  - Align UI with `CreatePostFragment` layout
  - Ensure consistent styling and field visibility

### 8. **Prompt Post Display** ⚠️
- **Status**: PARTIALLY IMPLEMENTED
- **Current Implementation**:
  - `PostsAdapter` shows `prompt_section` for prompt posts (lines 171-195)
  - `description_section` is also shown if available
  - Divider between sections exists
- **Issue**: 
  - On DashboardFragment (prompt post page), posts may only show prompt section, not description
  - Need to verify both sections are visible in the list view
- **Action Needed**:
  - Verify `DashboardFragment` uses same `PostsAdapter` (it does - line 42)
  - Ensure both prompt and description sections show in list view
  - May need to adjust preview text length

---

## ❌ **NOT IMPLEMENTED**

### 9. **Version History** ❌
- **Status**: NOT IMPLEMENTED
- **Requirements**:
  - Add version history button to each post in activity log
  - Show versions (initial post + all edits)
  - Option to revert to a version
- **Backend Changes Required**: ✅ YES
  - Create `post_versions` table
  - Save version on every post edit
  - API endpoints to fetch versions
  - API endpoint to revert to version
- **Frontend Changes Required**:
  - New `PostVersion` model
  - New `PostVersionsFragment` or dialog
  - Version history button in activity log
  - Revert functionality
- **Database Schema Needed**:
```sql
CREATE TABLE IF NOT EXISTS post_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    prompt_section TEXT,
    description_section TEXT,
    llm_tag VARCHAR(100) NOT NULL,
    is_prompt_post BOOLEAN DEFAULT FALSE,
    anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL REFERENCES users(id),
    UNIQUE(post_id, version_number)
);
```
- **Implementation Approach**:
  1. Backend: Add version tracking to `updatePost` controller
  2. Backend: Create `GET /api/posts/:id/versions` endpoint
  3. Backend: Create `POST /api/posts/:id/revert/:versionId` endpoint
  4. Frontend: Add version button to `UserActivityAdapter`
  5. Frontend: Create `PostVersionsFragment`
  6. Frontend: Add revert functionality

---

## 📋 **IMPLEMENTATION CHECKLIST**

### UI Updates (Frontend Only)
- [ ] Fix vote icons to persist filled state after voting
  - Files: `PostsAdapter.java`, `CommentsAdapter.java`, `PostDetailFragment.java`
  - Ensure `user_vote_type` is updated after vote API response
- [ ] Verify prompt post divider is visible (already implemented, just verify)
- [ ] Clean up all page layouts (general UI polish)

### Activity Log Fixes (Frontend Only)
- [x] Comments show post name (already implemented)
- [ ] Fix post date display - use `created_at` consistently, not `updated_at`
  - File: `NotificationsViewModel.java` (line 163 - already uses `created_at`)
  - Verify this is working correctly
- [ ] Clean up edit post page UI to match create post page
  - File: `EditPostFragment.java` and `fragment_edit_post.xml`

### Prompt Post Page (Frontend Only)
- [ ] Verify both prompt and description sections show in DashboardFragment
  - Currently implemented in `PostsAdapter`, just verify it works
- [ ] Ensure preview shows beginning of both sections

### Bookmarks (Backend Required)
- [x] Frontend implemented (local only)
- [ ] **Backend**: Create `bookmarks` table
- [ ] **Backend**: Add `POST /api/bookmarks` endpoint
- [ ] **Backend**: Add `DELETE /api/bookmarks/:postId` endpoint
- [ ] **Backend**: Add `GET /api/bookmarks` endpoint
- [ ] **Frontend**: Update `BookmarkManager` to use API instead of local storage
- [ ] **Frontend**: Update `BookmarksViewModel` to fetch from API

**Database Schema for Bookmarks**:
```sql
CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, post_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_bookmarks_post ON bookmarks(post_id);
```

### Drafts (Backend Required)
- [x] Frontend implemented (local only)
- [ ] **Backend**: Create `drafts` table
- [ ] **Backend**: Add `POST /api/drafts` endpoint
- [ ] **Backend**: Add `GET /api/drafts` endpoint
- [ ] **Backend**: Add `PUT /api/drafts/:id` endpoint
- [ ] **Backend**: Add `DELETE /api/drafts/:id` endpoint
- [ ] **Frontend**: Update `DraftsViewModel` to use API instead of local storage

**Database Schema for Drafts**:
```sql
CREATE TABLE IF NOT EXISTS drafts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(500),
    content TEXT,
    prompt_section TEXT,
    description_section TEXT,
    llm_tag VARCHAR(100),
    is_prompt_post BOOLEAN DEFAULT FALSE,
    anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_drafts_user ON drafts(user_id);
```

### Version History (Backend Required)
- [ ] **Backend**: Create `post_versions` table (see schema above)
- [ ] **Backend**: Modify `updatePost` to save version before updating
- [ ] **Backend**: Add `GET /api/posts/:id/versions` endpoint
- [ ] **Backend**: Add `POST /api/posts/:id/revert/:versionId` endpoint
- [ ] **Frontend**: Create `PostVersion` model
- [ ] **Frontend**: Create `PostVersionsFragment` or dialog
- [ ] **Frontend**: Add version history button to activity log items
- [ ] **Frontend**: Implement revert functionality

---

## 🔧 **BACKEND CHANGES SUMMARY**

### Database Migrations Needed:
1. ❌ **Anonymous field** - Add `posts.anonymous` column (NOT in schema.sql)
2. ❌ **Bookmarks table** - Create new table
3. ❌ **Drafts table** - Create new table
4. ❌ **Post versions table** - Create new table

### API Endpoints Needed:

**Bookmarks**:
- `POST /api/bookmarks` - Add bookmark
- `DELETE /api/bookmarks/:postId` - Remove bookmark
- `GET /api/bookmarks` - Get user's bookmarks

**Drafts**:
- `POST /api/drafts` - Create draft
- `GET /api/drafts` - Get user's drafts
- `PUT /api/drafts/:id` - Update draft
- `DELETE /api/drafts/:id` - Delete draft

**Version History**:
- `GET /api/posts/:id/versions` - Get post versions
- `POST /api/posts/:id/revert/:versionId` - Revert to version

### Backend Controller Changes:
- **CRITICAL**: Update `postController.js` queries to include `user_vote_type`:
  - `getPosts()` - Add LEFT JOIN for user vote
  - `getPostById()` - Add LEFT JOIN for user vote
  - `getPromptPosts()` - Add LEFT JOIN for user vote
  - `searchPosts()` - Add LEFT JOIN for user vote
- **CRITICAL**: Update `commentController.js` queries to include `user_vote_type`:
  - `getComments()` - Add LEFT JOIN for user vote
- Modify `postController.updatePost()` to save version before updating
- Create `bookmarkController.js`
- Create `draftController.js`
- Add version endpoints to `postController.js`

---

## 📝 **RECOMMENDATIONS**

### Priority Order:
1. **High Priority** (UI fixes, no backend):
   - Fix vote icon persistence
   - Clean up edit post page UI
   - Verify prompt post display

2. **Medium Priority** (Backend required):
   - Bookmarks backend implementation
   - Drafts backend implementation

3. **Low Priority** (Complex feature):
   - Version history (most complex, requires careful design)

### Implementation Notes:

**For Drafts**: 
- Current local implementation works but doesn't persist
- Backend implementation is straightforward - just CRUD operations
- Can reuse existing `Draft` model structure

**For Bookmarks**:
- Current local implementation works but doesn't persist
- Backend is simple - just a join table
- Easy to migrate from local to API-based

**For Version History**:
- Most complex feature
- Need to decide: save full post data per version or just diffs?
- Recommend saving full post data (simpler, more reliable)
- Consider version limits (e.g., keep last 10 versions per post)
- Revert functionality should create a new version, not modify history

**For Anonymous Field**:
- Already implemented in frontend
- **Backend database is MISSING the column** (not in schema.sql)
- **Action Required**: Add migration: `ALTER TABLE posts ADD COLUMN IF NOT EXISTS anonymous BOOLEAN DEFAULT FALSE;`
- Create migration file: `backend/database/migrations/add_anonymous_field.sql`

---

## ✅ **VERIFICATION CHECKLIST**

Before marking as complete, verify:
- [ ] Vote icons stay filled after voting
- [ ] Prompt post divider is visible
- [ ] Activity log shows post names for comments (not IDs)
- [ ] Activity log uses `created_at` for post dates (not `updated_at`)
- [ ] Edit post page UI matches create post page
- [ ] Prompt posts show both prompt and description sections in list view
- [ ] Anonymous posts show "Anonymous" as author
- [ ] Bookmarks persist after app restart (requires backend)
- [ ] Drafts persist after app restart (requires backend)
- [ ] Version history works for posts (requires backend)

