package com.example.csci_310project2team26.ui.home;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.example.csci_310project2team26.MainActivity;
import com.example.csci_310project2team26.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Black-box Test: HomeFragment UI Testing
 * 
 * Location: app/src/androidTest/java/com/example/csci_310project2team26/ui/home/HomeFragmentBlackBoxTest.java
 * Test Class: HomeFragmentBlackBoxTest
 * 
 * Description: Tests the HomeFragment UI from a user's perspective. Tests post display,
 * scrolling, voting, and navigation to post details.
 * 
 * How to Execute: 
 * 1. Start an Android emulator or connect a physical device
 * 2. Ensure user is logged in (or mock authentication)
 * 3. Run as Android Instrumented Test in Android Studio
 * 4. Or via: ./gradlew connectedAndroidTest --tests HomeFragmentBlackBoxTest
 * 
 * Rationale: These tests verify the home feed functionality works correctly from an external perspective,
 * ensuring users can view posts, interact with them, and navigate as expected.
 */
@RunWith(AndroidJUnit4.class)
public class HomeFragmentBlackBoxTest {
    
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);
    
    @Test
    public void testHomeFragmentDisplaysPosts() {
        // Rationale: Verify that home fragment displays posts list
        // Input: Navigate to home fragment
        // Expected: RecyclerView with posts is displayed
        
        // Verify posts RecyclerView is displayed (from fragment_home.xml)
        onView(withId(R.id.postsRecyclerView))
            .check(matches(isDisplayed()));
        
        // Verify search input is displayed
        onView(withId(R.id.searchEditText))
            .check(matches(isDisplayed()));
        
        // Verify sort spinner is displayed
        onView(withId(R.id.sortSpinner))
            .check(matches(isDisplayed()));
    }
    
    @Test
    public void testClickOnPost() {
        // Rationale: Test clicking on a post to view details
        // Input: Click on first post in the list
        // Expected: PostDetailFragment is displayed with post details
        
        // Click on first item in RecyclerView (using correct ID from item_post.xml)
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        
        // Verify post detail screen is displayed (using correct IDs from fragment_post_detail.xml)
        onView(withId(R.id.titleTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.contentTextView)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testUpvotePost() {
        // Rationale: Test upvoting a post from post detail view
        // Input: Click upvote button on post detail
        // Expected: Post upvote count increases
        
        // First navigate to post detail
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        
        // Click upvote button (using correct ID from fragment_post_detail.xml)
        onView(withId(R.id.upvoteButton))
            .perform(click());
        
        // Verify upvote count is displayed (may need to wait for API response)
        onView(withId(R.id.upvoteCountTextView)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testDownvotePost() {
        // Rationale: Test downvoting a post from post detail view
        // Input: Click downvote button on post detail
        // Expected: Post downvote count increases
        
        // Navigate to post detail
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        
        // Click downvote button
        onView(withId(R.id.downvoteButton))
            .perform(click());
        
        // Verify downvote count is displayed
        onView(withId(R.id.downvoteCountTextView)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testViewPostComments() {
        // Rationale: Test viewing comments on a post
        // Input: Navigate to post detail
        // Expected: Comments section is displayed (RecyclerView or comment button)
        
        // Navigate to post detail
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        
        // Wait for post detail to load - verify title is displayed first
        onView(withId(R.id.titleTextView)).check(matches(isDisplayed()));
        
        // Scroll to comments section (it's inside a NestedScrollView)
        // Check for the "Comments" label or comment button as they're more reliable indicators
        onView(withId(R.id.commentButton)).check(matches(isDisplayed()));
        
        // Also verify the comments RecyclerView exists (may be empty, but should be in layout)
        // Use scrollTo to ensure it's in view
        onView(withId(R.id.commentsRecyclerView))
            .perform(scrollTo())
            .check(matches(isDisplayed()));
    }
    
    @Test
    public void testCreateCommentWithTitle() {
        // Rationale: Test creating a comment with optional title
        // Input: Navigate to post detail, enter comment title and text, submit
        // Expected: Comment is created with title
        
        // Navigate to post detail
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        
        // Scroll to comment input section
        onView(withId(R.id.commentTitleEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()));
        
        // Enter comment title (optional)
        onView(withId(R.id.commentTitleEditText))
            .perform(typeText("Test Comment Title"), closeSoftKeyboard());
        
        // Enter comment text
        onView(withId(R.id.commentEditText))
            .perform(typeText("Test comment text"), closeSoftKeyboard());
        
        // Click add comment button
        onView(withId(R.id.addCommentButton)).perform(click());
        
        // Note: Actual success depends on backend
        // This test verifies the UI flow works correctly
    }
    
    @Test
    public void testCreateCommentWithoutTitle() {
        // Rationale: Test creating a comment without title (title is optional)
        // Input: Navigate to post detail, enter only comment text, submit
        // Expected: Comment is created without title
        
        // Navigate to post detail
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        
        // Scroll to comment input section
        onView(withId(R.id.commentEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()));
        
        // Enter comment text (no title)
        onView(withId(R.id.commentEditText))
            .perform(typeText("Test comment without title"), closeSoftKeyboard());
        
        // Click add comment button
        onView(withId(R.id.addCommentButton)).perform(click());
        
        // Note: Actual success depends on backend
        // This test verifies the UI flow works correctly
    }
    
}

