# Testing Guide

## Quick Start: Running Tests

### Run All White-Box Tests (Unit Tests) - Simple Command
```bash
./gradlew test
```

This runs all white-box tests and shows each test result. You'll see output like:
```
PostModelTest > testPostCreationWithFullConstructor PASSED
PostModelTest > testPostSetters PASSED
CommentModelTest > testCommentCreationWithFullConstructor PASSED
...
```

**View detailed results:**
- Open `app/build/reports/tests/test/index.html` in a browser to see a full report with all test names and results

### Run All Black-Box Tests (UI/Instrumented Tests)
```bash
./gradlew connectedAndroidTest
```

**Why does this need an emulator?**
- Black-box tests use **Espresso** to interact with actual Android UI components (buttons, text fields, etc.)
- Espresso requires the **Android framework** to be running, which only exists on Android devices/emulators
- JUnit alone can't test Android UI - you need Android's runtime environment
- This is a fundamental Android testing requirement, not a limitation of our setup

**Alternative:** If you want to test without an emulator, you'd need to use Robolectric (which mocks Android framework), but that's not true black-box testing since it doesn't test the real UI.

### Run Both Test Suites
```bash
./gradlew test connectedAndroidTest
```

### Run Specific Test Class
```bash
# White-box example
./gradlew test --tests "com.example.csci_310project2team26.data.model.PostModelTest"

# Black-box example (requires emulator)
./gradlew connectedAndroidTest --tests "com.example.csci_310project2team26.ui.auth.LoginActivityBlackBoxTest"
```

### In Android Studio
1. **White-box tests**: Right-click on `app/src/test/java/` → "Run Tests"
2. **Black-box tests**: Right-click on `app/src/androidTest/java/` → "Run Tests" (requires emulator running)
3. Click green play button ▶️ next to any test class/method

## Test Reports Location
- **Unit Tests (White-box)**: `app/build/reports/tests/test/index.html`
- **Instrumented Tests (Black-box)**: `app/build/reports/androidTests/connected/index.html`

---

## Understanding Test Types

### White-Box Tests (Unit Tests)
- **Location**: `app/src/test/java/`
- **Framework**: JUnit 4
- **Runs on**: JVM (no Android device needed)
- **What they test**: Individual classes, methods, data models
- **Example**: Testing that a Post object correctly stores and retrieves data

### Black-Box Tests (Instrumented/UI Tests)
- **Location**: `app/src/androidTest/java/`
- **Framework**: JUnit 4 + Espresso + AndroidJUnit4
- **Runs on**: Android device/emulator (required)
- **What they test**: User interactions, UI behavior, end-to-end flows
- **Example**: Testing that clicking a login button actually logs the user in

**Why Espresso needs an emulator:**
- Espresso interacts with real Android Views (TextView, Button, etc.)
- These Views are part of the Android framework, not Java
- The Android framework only runs on Android devices/emulators
- It's like testing a web app - you need a browser to test browser features

---

## Test Users Required for Database

**You must add these 3 test users to your database before running tests that require authentication.**

### Test Users Summary

| User | Email | Password | Student ID | Purpose |
|------|-------|----------|------------|---------|
| Test User 1 | `testuser@usc.edu` | `TestPassword123!` | `1234567890` | Primary auth & content tests |
| Test User 2 | `testuser2@usc.edu` | `TestPassword123!` | `0987654321` | Multi-user scenarios |
| Test User 3 | `profiletest@usc.edu` | `TestPassword123!` | `1122334455` | Profile tests |

### How to Add Test Users

#### Option 1: Via Backend API (Recommended)
```bash
# Test User 1
curl -X POST https://csci-310project2team26real-production.up.railway.app/api/auth/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=Test User One&email=testuser@usc.edu&student_id=1234567890&password=TestPassword123!"

# Test User 2
curl -X POST https://csci-310project2team26real-production.up.railway.app/api/auth/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=Test User Two&email=testuser2@usc.edu&student_id=0987654321&password=TestPassword123!"

# Test User 3
curl -X POST https://csci-310project2team26real-production.up.railway.app/api/auth/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=Profile Test User&email=profiletest@usc.edu&student_id=1122334455&password=TestPassword123!"
```

#### Option 2: Direct SQL Insert
```sql
-- Note: Replace password_hash with actual bcrypt hash from your backend
INSERT INTO users (name, email, student_id, password_hash, created_at)
VALUES 
    ('Test User One', 'testuser@usc.edu', '1234567890', '<hashed_password>', NOW()),
    ('Test User Two', 'testuser2@usc.edu', '0987654321', '<hashed_password>', NOW()),
    ('Profile Test User', 'profiletest@usc.edu', '1122334455', '<hashed_password>', NOW());
```

### Verify Test Users
```bash
curl -X POST https://csci-310project2team26real-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=testuser@usc.edu&password=TestPassword123!"
```

---

## Test Structure

### White-Box Tests (68 tests)
**Location**: `app/src/test/java/com/example/csci_310project2team26/`
- **PostModelTest**: 12 tests
- **CommentModelTest**: 10 tests
- **UserModelTest**: 8 tests
- **ProfileModelTest**: 9 tests
- **SessionManagerTest**: 7 tests
- **AuthRepositoryTest**: 10 tests
- **PostRepositoryTest**: 12 tests

### Black-Box Tests (28 tests)
**Location**: `app/src/androidTest/java/com/example/csci_310project2team26/`
- **LoginActivityBlackBoxTest**: 11 tests
- **HomeFragmentBlackBoxTest**: 7 tests
- **APIIntegrationBlackBoxTest**: 10 tests

---

## Prerequisites

1. **Backend Server**: Must be running at `https://csci-310project2team26real-production.up.railway.app/`
2. **Android Device/Emulator**: Required ONLY for black-box tests (API level 24+)
3. **Test Users**: Add the 3 test users above to your database

---

## Troubleshooting

### "No devices found" (Black-box tests)
- Start Android emulator: Tools → Device Manager → Create/Start Virtual Device
- Or connect physical device with USB debugging enabled
- **Note**: This is required because Espresso needs the Android framework to test UI

### Network/Authentication test failures
- Ensure backend server is running
- Verify test users exist in database
- Check BASE_URL in `ApiService.java`

### Tests fail with "email already exists"
- User already exists in database - this is fine, tests will still work

### White-box tests not showing individual results
- Run `./gradlew test --info` for more detailed output
- Check HTML report at `app/build/reports/tests/test/index.html` for full details

---

## Test Frameworks Used

- **JUnit 4**: Core testing framework (all tests)
- **AndroidJUnit4**: Android test runner (black-box tests)
- **Espresso**: UI testing framework (black-box UI tests) - requires Android device
- **Mockito**: Mocking framework (available but not heavily used)

---

## Quick Reference

| Command | What It Does | Needs Emulator? |
|---------|--------------|------------------|
| `./gradlew test` | Run all white-box tests | ❌ No |
| `./gradlew connectedAndroidTest` | Run all black-box tests | ✅ Yes |
| `./gradlew test --tests "PostModelTest"` | Run specific white-box test class | ❌ No |
| `./gradlew connectedAndroidTest --tests "LoginActivityBlackBoxTest"` | Run specific black-box test class | ✅ Yes |
