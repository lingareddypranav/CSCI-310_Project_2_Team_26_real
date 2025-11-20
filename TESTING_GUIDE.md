# Testing Guide

## Quick Start: Running Tests

### Run All White-Box Tests (Unit Tests)
```bash
./gradlew test
```

### Run All Black-Box Tests (Instrumented Tests - Requires Device/Emulator)
```bash
./gradlew connectedAndroidTest
```

### Run Both Test Suites
```bash
./gradlew test connectedAndroidTest
```

### Run Specific Test Class
```bash
# White-box example
./gradlew test --tests "com.example.csci_310project2team26.data.model.PostModelTest"

# Black-box example
./gradlew connectedAndroidTest --tests "com.example.csci_310project2team26.ui.auth.LoginActivityBlackBoxTest"
```

### In Android Studio
1. Right-click on `app/src/test/java/` → "Run Tests" (for white-box)
2. Right-click on `app/src/androidTest/java/` → "Run Tests" (for black-box)
3. Click green play button next to any test class/method

## Test Reports Location
- **Unit Tests**: `app/build/reports/tests/test/index.html`
- **Instrumented Tests**: `app/build/reports/androidTests/connected/index.html`

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

### White-Box Tests (66 tests)
**Location**: `app/src/test/java/com/example/csci_310project2team26/`
- **PostModelTest**: 10 tests
- **CommentModelTest**: 9 tests
- **UserModelTest**: 8 tests
- **ProfileModelTest**: 9 tests
- **SessionManagerTest**: 7 tests
- **AuthRepositoryTest**: 10 tests
- **PostRepositoryTest**: 12 tests

### Black-Box Tests (29 tests)
**Location**: `app/src/androidTest/java/com/example/csci_310project2team26/`
- **LoginActivityBlackBoxTest**: 11 tests
- **HomeFragmentBlackBoxTest**: 7 tests
- **APIIntegrationBlackBoxTest**: 10 tests

---

## Prerequisites

1. **Backend Server**: Must be running at `https://csci-310project2team26real-production.up.railway.app/`
2. **Android Device/Emulator**: Required for black-box tests (API level 24+)
3. **Test Users**: Add the 3 test users above to your database

---

## Troubleshooting

### "No devices found" (Black-box tests)
- Start Android emulator: Tools → Device Manager → Create/Start Virtual Device
- Or connect physical device with USB debugging enabled

### Network/Authentication test failures
- Ensure backend server is running
- Verify test users exist in database
- Check BASE_URL in `ApiService.java`

### Tests fail with "email already exists"
- User already exists in database - this is fine, tests will still work

---

## Test Frameworks Used

- **JUnit 4**: Core testing framework (all tests)
- **AndroidJUnit4**: Android test runner (black-box tests)
- **Espresso**: UI testing framework (black-box UI tests)
- **Mockito**: Mocking framework (available but not heavily used)

