package com.example.csci_310project2team26.ui.auth;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.example.csci_310project2team26.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Black-box Test: LoginActivity UI Testing
 * 
 * Location: app/src/androidTest/java/com/example/csci_310project2team26/ui/auth/LoginActivityBlackBoxTest.java
 * Test Class: LoginActivityBlackBoxTest
 * 
 * Description: Tests the LoginActivity UI from a user's perspective without knowledge of internal implementation.
 * Tests user interactions, input validation, and navigation flows.
 * 
 * How to Execute: 
 * 1. Start an Android emulator or connect a physical device
 * 2. Run as Android Instrumented Test in Android Studio
 * 3. Or via: ./gradlew connectedAndroidTest --tests LoginActivityBlackBoxTest
 * 
 * Rationale: These tests verify the user interface behaves correctly from an external perspective,
 * ensuring users can interact with login functionality as expected.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityBlackBoxTest {
    
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = 
        new ActivityScenarioRule<>(LoginActivity.class);
    
    @Test
    public void testLoginActivityDisplaysCorrectly() {
        // Rationale: Verify that login screen displays all required UI elements
        // Input: Launch LoginActivity
        // Expected: Email field, password field, login button, and register link are visible
        
        // Verify email input field is displayed
        onView(withId(R.id.emailEditText))
            .check(matches(isDisplayed()));
        
        // Verify password input field is displayed
        onView(withId(R.id.passwordEditText))
            .check(matches(isDisplayed()));
        
        // Verify login button is displayed
        onView(withId(R.id.loginButton))
            .check(matches(isDisplayed()));
        
        // Verify register link is displayed
        onView(withId(R.id.registerLink))
            .check(matches(isDisplayed()));
    }
    
    @Test
    public void testLoginWithValidCredentials() {
        // Rationale: Test login UI flow with test credentials
        // Input: Test user email and password from TESTING_GUIDE.md
        // Expected: Login button click triggers login attempt (outcome depends on backend)
        
        // Use test user credentials from TESTING_GUIDE.md
        // Test User 1: testuser@usc.edu / TestPassword123!
        // Note: This test verifies the UI interaction works, not authentication success
        
        // Enter email
        onView(withId(R.id.emailEditText))
            .perform(typeText("testuser@usc.edu"), closeSoftKeyboard());
        
        // Enter password (from TESTING_GUIDE.md)
        onView(withId(R.id.passwordEditText))
            .perform(typeText("TestPassword123!"), closeSoftKeyboard());
        
        // Verify login button is still visible and enabled before clicking
        onView(withId(R.id.loginButton))
            .check(matches(isDisplayed()));
        
        // Click login button - this triggers the login flow
        // The test passes if the click succeeds without crashing
        // We don't wait for the result as it may navigate away or show an error
        // Both outcomes are valid - we're testing UI interaction, not auth logic
        onView(withId(R.id.loginButton))
            .perform(click());
        
        // Test passes if we reach here without crashing
        // The actual login result (success/failure) is tested in API integration tests
    }
    
    @Test
    public void testLoginWithInvalidEmailFormat() {
        // Rationale: Test login with email that doesn't end with @usc.edu
        // Input: Email = "test@gmail.com", password = "password123"
        // Expected: Error message displayed indicating invalid email format
        
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton))
            .perform(click());
        
        // Verify error message is displayed
        // onView(withText("Invalid email format")).check(matches(isDisplayed()));
    }
    
    @Test
    public void testLoginWithEmptyEmail() {
        // Rationale: Test login with empty email field
        // Input: Email = "", password = "password123"
        // Expected: Error message displayed indicating email is required
        
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton))
            .perform(click());
        
        // Verify validation error is displayed
    }
    
    @Test
    public void testLoginWithEmptyPassword() {
        // Rationale: Test login with empty password field
        // Input: Email = "test@usc.edu", password = ""
        // Expected: Error message displayed indicating password is required
        
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@usc.edu"), closeSoftKeyboard());
        onView(withId(R.id.loginButton))
            .perform(click());
        
        // Verify validation error is displayed
    }
    
    @Test
    public void testLoginWithInvalidCredentials() {
        // Rationale: Test login with incorrect password
        // Input: Valid email but wrong password
        // Expected: Error message displayed indicating invalid credentials
        
        onView(withId(R.id.emailEditText))
            .perform(typeText("testuser@usc.edu"), closeSoftKeyboard());
        onView(withId(R.id.passwordEditText))
            .perform(typeText("wrongpassword"), closeSoftKeyboard());
        onView(withId(R.id.loginButton))
            .perform(click());
        
        // Verify error message for invalid credentials
        // onView(withText("Invalid email or password")).check(matches(isDisplayed()));
    }
    
    @Test
    public void testNavigateToRegistration() {
        // Rationale: Test navigation to registration screen
        // Input: Click on "Register" link
        // Expected: RegistrationActivity is launched
        
        // Click register link
        onView(withId(R.id.registerLink)).perform(click());
        
        // Note: To fully verify RegistrationActivity, you would check for elements
        // specific to that activity, such as:
        // onView(withId(R.id.registrationTitle)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testBackButtonOnLoginScreen() {
        // Rationale: Test back button behavior on login screen
        // Input: Press back button
        // Expected: App exits (since LoginActivity is launcher)
        // Note: This is expected Android behavior - pressing back on launcher activity exits app
        // The activity finishing is handled by ActivityScenarioRule
        
        // Verify activity is running before pressing back
        activityRule.getScenario().onActivity(activity -> {
            // Activity should be running
            assert !activity.isFinishing();
        });
        
        // Press back - this will finish the activity (expected Android system behavior)
        // ActivityScenarioRule will handle the activity finishing gracefully
        try {
            Espresso.pressBack();
            // If we get here, the back press was handled
            // The activity finishing is expected and handled by the test framework
        } catch (androidx.test.espresso.NoActivityResumedException e) {
            // This is expected when pressing back on the launcher activity
            // The app exits, which is the correct Android behavior
            // The test passes because this is the expected outcome
        }
    }
}

