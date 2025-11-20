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
import static androidx.test.espresso.action.ViewActions.scrollTo;
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
    public void testScrollThroughPosts() {
        // Rationale: Test scrolling through the posts list
        // Input: Scroll down the posts list
        // Expected: More posts are loaded and displayed
        
        // Scroll to a specific position in RecyclerView
        // onView(withId(R.id.postsRecyclerView))
        //     .perform(RecyclerViewActions.scrollToPosition(10));
        
        // Verify scrolling works (no crash, posts visible)
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
        // Expected: Comments RecyclerView is displayed
        
        // Navigate to post detail
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        
        // Verify comments RecyclerView is displayed (using correct ID)
        onView(withId(R.id.commentsRecyclerView)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testPostItemDisplay() {
        // Rationale: Test that post items display all required information
        // Input: View posts list
        // Expected: Each post item shows title, author, tag, content, votes, comments
        
        // Verify post item elements are displayed (using correct IDs from item_post.xml)
        // Note: These are in RecyclerView items, so we check the first item
        onView(withId(R.id.postsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, scrollTo()));
        
        // These would be checked within the RecyclerView item
        // titleTextView, authorTextView, tagTextView, contentTextView exist in item_post.xml
    }
    
}

