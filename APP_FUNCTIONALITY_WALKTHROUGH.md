# BestLLM App - Complete Functionality Walkthrough

## 📱 App Structure Overview

The app follows a **Client-Server Layered Architecture** with **MVVM (Model-View-ViewModel)** pattern:
- **UI Layer**: Activities and Fragments (Android)
- **ViewModel Layer**: Business logic and state management
- **Repository Layer**: API communication with backend
- **Backend**: Node.js + Express + PostgreSQL on Railway

---

## 🚀 User Journey Flow

### Entry Point → Main App

```
LoginActivity (Launcher)
    ↓
[Register] → RegistrationActivity → ProfileCreationActivity
    ↓
[Login] → MainActivity (Bottom Navigation)
```

---

## 📄 Detailed Page Breakdown

## 1. **LoginActivity** (Entry Point/Launcher)

**Location**: `ui/auth/LoginActivity.java`

### Purpose
First screen users see - USC authentication

### Available Actions:
- ✅ **Login with USC Email**
  - Input: USC email (@usc.edu), password
  - Real-time email validation (must end with @usc.edu)
  - "Remember Me" checkbox (saves session)
  - Validates fields before submission
  - On success → Navigates to `MainActivity`

- ✅ **Navigate to Registration**
  - "Register" link → Opens `RegistrationActivity`

- ⚠️ **Forgot Password** (Placeholder)
  - Link exists but shows "Coming soon" toast
  - Not implemented yet

- ✅ **Auto-login Check**
  - Checks for saved session on app start
  - If valid session exists → Auto-navigates to MainActivity

### Features:
- Real-time email validation
- Password visibility toggle
- Loading states during authentication
- Error handling with user-friendly messages

---

## 2. **RegistrationActivity**

**Location**: `ui/auth/RegistrationActivity.java`

### Purpose
Register new USC users

### Available Actions:
- ✅ **Register New User**
  - **Required Fields:**
    - Name (text input)
    - USC Email (@usc.edu) - real-time validation
    - 10-digit Student ID - real-time validation (must be exactly 10 digits, numbers only)
    - Password (minimum 8 characters) - real-time validation
    - Confirm Password - must match password
  - Validates all fields before submission
  - Auto-logins after successful registration
  - On success → Navigates to `ProfileCreationActivity`

- ✅ **Navigate to Login**
  - "Already have account?" link → Returns to `LoginActivity`

### Features:
- Real-time field validation
- Password strength indicator (minimum 8 chars)
- Student ID format validation (exactly 10 digits)
- Email format validation (@usc.edu required)
- Progress indicator during registration
- Auto-login after registration

---

## 3. **ProfileCreationActivity**

**Location**: `ui/create_profile/ProfileCreationActivity.java`

### Purpose
Initial profile setup after registration

### Available Actions:
- ✅ **Create Profile**
  - **Required Fields:**
    - Name (pre-filled, non-editable)
    - Email (pre-filled, non-editable)
    - Affiliation (USC School/Department) - dropdown with 17 options:
      - Viterbi School of Engineering
      - Marshall School of Business
      - Dornsife College of Letters, Arts and Sciences
      - Annenberg School for Communication and Journalism
      - School of Cinematic Arts
      - Roski School of Art and Design
      - School of Architecture
      - Thornton School of Music
      - Keck School of Medicine
      - School of Pharmacy
      - Suzanne Dworak-Peck School of Social Work
      - Rossier School of Education
      - Gould School of Law
      - Price School of Public Policy
      - School of Dramatic Arts
      - Herman Ostrow School of Dentistry
      - Other
    - Birth Date - date picker (minimum age 13 per USC policy)
    - Bio - minimum 10 characters
  - **Optional Fields:**
    - Profile Picture - image picker
    - Interests - free text
  - On success → Navigates to `MainActivity`

- ✅ **Skip Profile Creation**
  - "Skip for now" button
  - Navigates to `MainActivity` (can complete profile later)

### Features:
- Date picker with age restriction
- Image picker for profile picture
- Real-time validation
- Affiliation autocomplete dropdown
- Bio length validation (minimum 10 characters)

---

## 4. **MainActivity** (Main App Container)

**Location**: `MainActivity.java`

### Purpose
Main container with bottom navigation

### Layout:
```
┌─────────────────────────┐
│   Action Bar            │  ← Profile Settings icon
├─────────────────────────┤
│                         │
│   Fragment Container    │  ← Shows active fragment
│   (changes on nav)      │
│                         │
├─────────────────────────┤
│ [Home] [Prompts] [+]   │  ← Bottom Navigation
│        [Notifications]  │
└─────────────────────────┘
```

### Bottom Navigation Tabs:
1. **Home** (🏠) → `HomeFragment` - Normal posts
2. **Dashboard/Prompts** (📊) → `DashboardFragment` - Prompt posts
3. **Create Post** (➕) → `CreatePostFragment` - Create new post
4. **Notifications** (🔔) → `NotificationsFragment` - User activity log

### Action Bar:
- **Profile Settings Icon** (👤) → Opens `ProfileSettingsFragment`

### Navigation:
- Uses Android Navigation Component
- Bottom navigation switches between fragments
- Back button navigation supported

---

## 5. **HomeFragment** (Normal Posts Feed)

**Location**: `ui/home/HomeFragment.java`

### Purpose
Display feed of normal posts (non-prompt posts)

### Available Actions:
- ✅ **View Posts**
  - Displays scrollable list of posts
  - Each post shows:
    - Title
    - Author name
    - LLM tag
    - Upvote/Downvote counts
    - Comment count
    - Content preview

- ✅ **Search Posts**
  - Search bar at top
  - Real-time search as you type
  - Searches: full text, title, author, tag
  - Works with database posts

- ✅ **Sort Posts**
  - Dropdown spinner:
    - "Newest" (default) - newest first
    - "Top" - by vote score (upvotes - downvotes)

- ✅ **Click Post**
  - Opens `PostDetailFragment` with full post details

### Features:
- Auto-refreshes when fragment becomes visible (onResume)
- Empty state message when no posts
- Loading indicator during data fetch
- Error handling with toast messages
- Pull-to-refresh behavior (implicit via onResume)

### Data Source:
- Fetches from backend API
- Filters: `isPromptPost = false`
- Supports pagination (limit/offset)

---

## 6. **DashboardFragment** (Prompt Posts Feed)

**Location**: `ui/dashboard/DashboardFragment.java`

### Purpose
Display feed of prompt posts only

### Available Actions:
- ✅ **View Prompt Posts**
  - Same layout as HomeFragment
  - Shows only posts where `isPromptPost = true`

- ✅ **Search Prompt Posts**
  - Same search functionality as HomeFragment
  - Filters results to prompt posts only

- ✅ **Sort Prompt Posts**
  - Same sorting options as HomeFragment
  - "Newest" or "Top"

- ✅ **Click Prompt Post**
  - Opens `PostDetailFragment` with full post details

### Features:
- Same as HomeFragment but filtered for prompt posts
- Auto-refreshes on resume

---

## 7. **CreatePostFragment** (Create New Post)

**Location**: `ui/createpost/CreatePostFragment.java`

### Purpose
Create new posts (normal or prompt)

### Available Actions:
- ✅ **Create Post**
  - **Required Fields:**
    - Title (text input)
    - Content/Body (multiline text)
    - LLM Tag (e.g., "ChatGPT", "GPT-4", "Claude")
  - **Optional:**
    - "Prompt Post" toggle switch
      - OFF = Normal post (appears in HomeFragment)
      - ON = Prompt post (appears in DashboardFragment)
  - "Publish" button
  - On success → Navigates to `PostDetailFragment` showing new post

### Features:
- Field validation (title and content required)
- Loading state during creation
- Success toast notification
- Auto-navigates to post detail after creation
- Form clears after successful creation

### Post Creation Flow:
```
Create Post → Backend API → Database → PostDetailFragment
                                    ↓
                         HomeFragment/DashboardFragment (refreshes on return)
```

---

## 8. **PostDetailFragment** (Post Details & Comments)

**Location**: `ui/home/PostDetailFragment.java`

### Purpose
View full post details, vote, and comment

### Available Actions:
- ✅ **View Post Details**
  - Full title
  - Full content
  - Author name
  - LLM tag
  - Upvote count
  - Downvote count
  - Comment count
  - Timestamps

- ✅ **Vote on Post**
  - Upvote button → Increases upvote count
  - Downvote button → Increases downvote count
  - Backend enforces: one vote per user (toggle if same, update if different)
  - Post reloads after voting to show updated counts

- ✅ **View Comments**
  - Scrollable list of comments
  - Each comment shows:
    - Author name
    - Comment text
    - Upvote/Downvote counts
    - Timestamps

- ✅ **Vote on Comments**
  - Upvote/Downvote buttons on each comment
  - Updates comment vote counts
  - Backend enforces one vote per user per comment

- ✅ **Write Comment**
  - "Write a comment" button → Focuses comment input field
  - Comment text input at bottom
  - "Add Comment" button
  - On success:
    - Comment appears in list
    - Comment count updates
    - Input field clears
    - Scrolls to top to show new comment

### Features:
- Real-time vote count updates
- Comment list auto-refreshes after adding comment
- Scrollable post content
- Keyboard handling for comment input
- Loading states
- Error handling

---

## 9. **NotificationsFragment** (User Activity Log)

**Location**: `ui/notifications/NotificationsFragment.java`

### Purpose
Shows user's own posts and comments for editing

### Available Actions:
- ✅ **View User Activity**
  - Displays scrollable list of:
    - User's posts
    - User's comments
  - Each item shows:
    - Post/comment title/preview
    - Type indicator (Post/Comment)
    - Timestamp

- ✅ **Edit Post**
  - Click on post item → Opens `EditPostFragment`
  - Passes postId

- ✅ **Edit Comment**
  - Click on comment item → Opens `EditCommentFragment`
  - Passes postId and commentId

### Features:
- Empty state message when no activity
- Loading indicator
- Error handling
- Click handlers for navigation

---

## 10. **EditPostFragment** (Edit Existing Post)

**Location**: `ui/editpost/EditPostFragment.java`

### Purpose
Edit a post the user created

### Available Actions:
- ✅ **View Post to Edit**
  - Loads existing post data:
    - Title (editable)
    - Content (editable)
    - LLM Tag (editable)
    - Prompt Post toggle (editable)

- ✅ **Save Changes**
  - "Save" button
  - Validates: title and content required
  - Updates post in database
  - On success → Returns to previous screen (back navigation)

### Features:
- Pre-populates form with existing data
- Field validation
- Loading state
- Success toast
- Auto-returns to previous screen on success

### Authorization:
- Backend enforces: users can only edit their own posts
- Returns 403 error if unauthorized

---

## 11. **EditCommentFragment** (Edit Existing Comment)

**Location**: `ui/editcomment/EditCommentFragment.java`

### Purpose
Edit a comment the user created

### Available Actions:
- ✅ **View Comment to Edit**
  - Loads existing comment text
  - Comment text input (editable)

- ✅ **Save Changes**
  - "Save" button
  - Validates: comment text required
  - Updates comment in database
  - On success → Returns to previous screen

### Features:
- Pre-populates form with existing comment
- Field validation
- Loading state
- Success toast
- Auto-returns on success

### Authorization:
- Backend enforces: users can only edit their own comments

---

## 12. **ProfileSettingsFragment** (Profile Management)

**Location**: `ui/profile/ProfileSettingsFragment.java`

### Purpose
View and update user profile

### Available Actions:
- ✅ **View Profile**
  - Displays current profile data:
    - Name (non-editable)
    - Email (non-editable)
    - Affiliation (non-editable)
    - Birth Date (editable)
    - Bio (editable)
    - Interests (editable)

- ✅ **Update Profile**
  - "Save" button
  - Updates: birth date, bio, interests
  - Note: Name, email, affiliation cannot be changed (per requirements)
  - On success → Profile reloads to show updated data

- ✅ **Reset Password**
  - Current password input
  - New password input
  - "Reset Password" button
  - Validates: both fields required, new password minimum 8 characters
  - On success → Password fields clear

### Features:
- Profile data loaded on view
- Field validation
- Separate actions for profile update vs password reset
- Loading states
- Success/error toasts
- Auto-refresh after update

### Access:
- Available via action bar icon (👤) in MainActivity

---

## 🔐 Authentication & Session Management

### Session Storage:
- **SessionManager** (`data/repository/SessionManager.java`)
  - In-memory storage of:
    - Auth token (JWT)
    - User ID
  - Used for authenticated API calls
  - Persists during app session

### Protected Routes:
- All post/comment creation, editing, voting requires authentication
- Token sent in `Authorization: Bearer <token>` header
- Backend validates token and extracts userId

---

## 🔄 Data Flow

### Reading Data:
```
Fragment → ViewModel → Repository → ApiService → Backend API → PostgreSQL
                                                              ↓
Fragment ← ViewModel ← Repository ← ApiService ← JSON Response
```

### Writing Data:
```
User Action → Fragment → ViewModel → Repository → ApiService → Backend API → PostgreSQL
                                                              ↓
Fragment ← ViewModel ← Repository ← ApiService ← Success/Error Response
```

---

## 📊 Features Summary

### ✅ Fully Implemented:
1. **User Registration & Authentication** (USC-only)
2. **Profile Creation & Management**
3. **Post Creation** (Normal & Prompt posts)
4. **Post Editing** (Own posts only)
5. **Comment Creation** (On posts)
6. **Comment Editing** (Own comments only)
7. **Voting** (Posts & Comments - one vote per user)
8. **Searching** (Full text, tag, author, title)
9. **Sorting** (Newest, Top)
10. **Post Feeds** (Home for normal, Dashboard for prompts)
11. **User Activity Log** (Posts & Comments)

### ⚠️ Placeholder/Not Implemented:
- **Forgot Password** - Shows "Coming soon" toast

---

## 🎨 UI/UX Features

- Material Design components
- Loading indicators
- Empty states
- Error handling with user-friendly messages
- Real-time validation
- Auto-refresh on navigation
- Smooth navigation transitions
- Responsive layouts

---

## 📱 Navigation Graph

```
LoginActivity
    ↓
RegistrationActivity → ProfileCreationActivity → MainActivity
    ↓                                              ↓
LoginActivity (loop)                          [Bottom Nav]
                                                 ├─ HomeFragment → PostDetailFragment
                                                 ├─ DashboardFragment → PostDetailFragment
                                                 ├─ CreatePostFragment → PostDetailFragment
                                                 └─ NotificationsFragment
                                                      ├─ EditPostFragment
                                                      └─ EditCommentFragment
                                              [Action Bar]
                                                 └─ ProfileSettingsFragment
```

---

This is a comprehensive overview of all functionality in the BestLLM Android app! Every screen and action has been documented.

