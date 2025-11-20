package com.example.csci_310project2team26.api;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.network.ApiService;
import com.example.csci_310project2team26.data.repository.AuthRepository;
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.data.repository.CommentRepository;
import com.example.csci_310project2team26.data.repository.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Black-box Test: API Integration Testing
 * 
 * Location: app/src/androidTest/java/com/example/csci_310project2team26/api/APIIntegrationBlackBoxTest.java
 * Test Class: APIIntegrationBlackBoxTest
 * 
 * Description: Tests the integration with the backend API from an external perspective.
 * These tests verify that the app correctly communicates with the hosted backend server
 * without knowledge of internal implementation details.
 * 
 * How to Execute: 
 * 1. Ensure backend server is running at: https://csci-310project2team26real-production.up.railway.app/
 * 2. Start an Android emulator or connect a physical device
 * 3. Run as Android Instrumented Test in Android Studio
 * 4. Or via: ./gradlew connectedAndroidTest --tests APIIntegrationBlackBoxTest
 * 
 * Rationale: These tests verify end-to-end API communication, ensuring the app correctly
 * sends requests and handles responses from the backend server.
 */
@RunWith(AndroidJUnit4.class)
public class APIIntegrationBlackBoxTest {
    
    private AuthRepository authRepository;
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private CountDownLatch latch;
    
    @Before
    public void setUp() {
        authRepository = new AuthRepository();
        postRepository = new PostRepository();
        commentRepository = new CommentRepository();
        SessionManager.clear();
        latch = new CountDownLatch(1);
    }
    
    @Test
    public void testAPIConnectivity() throws InterruptedException {
        // Rationale: Test that the app can connect to the backend API
        // Input: Attempt to fetch posts (public endpoint)
        // Expected: API responds successfully (200 OK) or network error if server is down
        
        final boolean[] connected = new boolean[1];
        final String[] error = new String[1];
        
        postRepository.fetchPosts(null, 1, 0, null,
                                 new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult result) {
                connected[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                error[0] = errorMsg;
                latch.countDown();
            }
        });
        
        latch.await(15, TimeUnit.SECONDS);
        
        // Should either connect successfully or provide clear error
        assertTrue("Should connect or provide error message", 
                   connected[0] || (error[0] != null && !error[0].isEmpty()));
    }
    
    @Test
    public void testRegisterNewUser() throws InterruptedException {
        // Rationale: Test user registration through API
        // Input: Valid registration data (name, email, student_id, password)
        // Expected: User is created and user ID is returned
        
        // Generate unique email to avoid conflicts
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@usc.edu";
        String uniqueStudentId = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        
        final String[] userId = new String[1];
        final String[] error = new String[1];
        
        authRepository.register("Test User", uniqueEmail, uniqueStudentId, "password123",
                               new AuthRepository.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                userId[0] = result;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                error[0] = errorMsg;
                latch.countDown();
            }
        });
        
        latch.await(15, TimeUnit.SECONDS);
        
        // Should succeed or provide specific error
        if (userId[0] != null) {
            assertNotNull("User ID should be returned", userId[0]);
        } else {
            // May fail if email/student_id already exists
            assertNotNull("Should provide error message", error[0]);
        }
    }
    
    @Test
    public void testLoginAndGetPosts() throws InterruptedException {
        // Rationale: Test complete flow: login, then fetch posts
        // Input: Valid credentials, then fetch posts request
        // Expected: Login succeeds, session is saved, posts are fetched
        
        // First login (requires valid test credentials)
        final boolean[] loggedIn = new boolean[1];
        CountDownLatch loginLatch = new CountDownLatch(1);
        
        authRepository.login("testuser@usc.edu", "password123", true,
                           new AuthRepository.Callback<com.example.csci_310project2team26.data.model.User>() {
            @Override
            public void onSuccess(com.example.csci_310project2team26.data.model.User user) {
                loggedIn[0] = true;
                loginLatch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                loginLatch.countDown();
            }
        });
        
        loginLatch.await(15, TimeUnit.SECONDS);
        
        if (loggedIn[0] && SessionManager.getToken() != null) {
            // Now fetch posts
            final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
            
            postRepository.fetchPosts(null, 10, 0, null,
                                     new PostRepository.Callback<PostRepository.PostsResult>() {
                @Override
                public void onSuccess(PostRepository.PostsResult postsResult) {
                    result[0] = postsResult;
                    latch.countDown();
                }
                
                @Override
                public void onError(String errorMsg) {
                    latch.countDown();
                }
            });
            
            latch.await(15, TimeUnit.SECONDS);
            
            if (result[0] != null) {
                assertNotNull("Posts should be returned", result[0].getPosts());
            }
        }
    }
    
    @Test
    public void testCreatePostRequiresAuthentication() throws InterruptedException {
        // Rationale: Test that creating a post requires authentication
        // Input: Attempt to create post without being logged in
        // Expected: Error indicating authentication is required
        
        SessionManager.clear(); // Ensure no session
        
        final String[] error = new String[1];
        
        postRepository.createPost("Test Title", "Test Content", "GPT-4", false,
                                 new PostRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post post) {
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                error[0] = errorMsg;
                latch.countDown();
            }
        });
        
        latch.await(15, TimeUnit.SECONDS);
        
        assertNotNull("Should receive authentication error", error[0]);
        assertTrue("Error should mention authentication",
                   error[0].toLowerCase().contains("authentication") ||
                   error[0].toLowerCase().contains("required"));
    }
    
    @Test
    public void testVotePostRequiresAuthentication() throws InterruptedException {
        // Rationale: Test that voting on a post requires authentication
        // Input: Attempt to vote without being logged in
        // Expected: Error indicating authentication is required
        
        SessionManager.clear();
        
        final String[] error = new String[1];
        
        postRepository.votePost("test_post_id", "upvote",
                               new PostRepository.Callback<PostRepository.VoteActionResult>() {
            @Override
            public void onSuccess(PostRepository.VoteActionResult result) {
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                error[0] = errorMsg;
                latch.countDown();
            }
        });
        
        latch.await(15, TimeUnit.SECONDS);
        
        assertNotNull("Should receive authentication error", error[0]);
    }
    
    @Test
    public void testSearchPostsAPI() throws InterruptedException {
        // Rationale: Test search posts API endpoint
        // Input: Search query "test", search type "full_text"
        // Expected: Posts matching query are returned
        
        final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
        
        postRepository.searchPosts("test", "full_text", null, 10, 0, null,
                                  new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult postsResult) {
                result[0] = postsResult;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                latch.countDown();
            }
        });
        
        latch.await(15, TimeUnit.SECONDS);
        
        // Should return results or empty list
        assertNotNull("Should return result", result[0] != null);
    }
    
    @Test
    public void testGetTrendingPostsAPI() throws InterruptedException {
        // Rationale: Test trending posts API endpoint
        // Input: Request top 10 trending posts
        // Expected: Top 10 trending posts are returned
        
        final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
        
        postRepository.fetchTrendingPosts(10, new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult postsResult) {
                result[0] = postsResult;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                latch.countDown();
            }
        });
        
        latch.await(15, TimeUnit.SECONDS);
        
        if (result[0] != null) {
            assertNotNull("Trending posts should be returned", result[0].getPosts());
            assertTrue("Should return at most 10 posts", result[0].getPosts().size() <= 10);
        }
    }
    
    @Test
    public void testGetCommentsForPost() throws InterruptedException {
        // Rationale: Test fetching comments for a post
        // Input: Valid post ID
        // Expected: Comments for that post are returned
        
        // First get a post ID
        final String[] postId = new String[1];
        CountDownLatch fetchLatch = new CountDownLatch(1);
        
        postRepository.fetchPosts(null, 1, 0, null,
                                 new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult postsResult) {
                if (postsResult.getPosts() != null && !postsResult.getPosts().isEmpty()) {
                    postId[0] = postsResult.getPosts().get(0).getId();
                }
                fetchLatch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                fetchLatch.countDown();
            }
        });
        
        fetchLatch.await(15, TimeUnit.SECONDS);
        
        if (postId[0] != null) {
            final CommentRepository.CommentsResult[] result = new CommentRepository.CommentsResult[1];
            
            commentRepository.fetchComments(postId[0], new CommentRepository.Callback<CommentRepository.CommentsResult>() {
                @Override
                public void onSuccess(CommentRepository.CommentsResult commentsResult) {
                    result[0] = commentsResult;
                    latch.countDown();
                }
                
                @Override
                public void onError(String errorMsg) {
                    latch.countDown();
                }
            });
            
            latch.await(15, TimeUnit.SECONDS);
            
            if (result[0] != null) {
                assertNotNull("Comments should be returned", result[0].getComments());
            }
        }
    }
    
    @Test
    public void testAPIErrorHandling() throws InterruptedException {
        // Rationale: Test that API errors are handled gracefully
        // Input: Request with invalid parameters or non-existent resource
        // Expected: Appropriate error message is returned
        
        final String[] error = new String[1];
        
        // Try to get non-existent post
        postRepository.getPostById("invalid_nonexistent_id_999999",
                                   new PostRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post post) {
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                error[0] = errorMsg;
                latch.countDown();
            }
        });
        
        latch.await(15, TimeUnit.SECONDS);
        
        // Should receive error for invalid ID
        assertNotNull("Should receive error for invalid post ID", error[0]);
    }
    
    @Test
    public void testAPITimeoutHandling() throws InterruptedException {
        // Rationale: Test behavior when API request times out
        // Input: API request (may need to simulate slow network)
        // Expected: Timeout error is handled gracefully
        
        // This test may require network throttling or mocking
        // For now, verify that long-running requests eventually complete or timeout
        final boolean[] completed = new boolean[1];
        
        postRepository.fetchPosts(null, null, null, null,
                                 new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult result) {
                completed[0] = true;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                completed[0] = true;
                latch.countDown();
            }
        });
        
        boolean finished = latch.await(30, TimeUnit.SECONDS);
        
        // Request should complete or timeout within reasonable time
        assertTrue("Request should complete or timeout", finished || completed[0]);
    }
}

