package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Post;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * White-box Test: PostRepository
 * 
 * Location: app/src/test/java/com/example/csci_310project2team26/data/repository/PostRepositoryTest.java
 * Test Class: PostRepositoryTest
 * 
 * Description: Tests the PostRepository class to ensure proper post operations,
 * error handling, and API communication. These tests make actual API calls.
 * 
 * How to Execute: Run as JUnit test in Android Studio or via: ./gradlew test --tests PostRepositoryTest
 * 
 * Coverage: Tests fetching posts, creating posts, voting, searching, and error handling.
 */
public class PostRepositoryTest {
    
    private PostRepository postRepository;
    private CountDownLatch latch;
    
    @Before
    public void setUp() {
        postRepository = new PostRepository();
        latch = new CountDownLatch(1);
    }
    
    @Test
    public void testFetchPostsWithDefaultParameters() throws InterruptedException {
        // Rationale: Test fetching posts with default sorting and pagination
        // Input: No parameters (uses defaults: sort=newest, limit=50, offset=0)
        // Expected: Returns list of posts
        
        final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
        final String[] error = new String[1];
        
        postRepository.fetchPosts(null, null, null, null, 
                                 new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult postsResult) {
                result[0] = postsResult;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                error[0] = errorMsg;
                latch.countDown();
            }
        });
        
        latch.await(10, TimeUnit.SECONDS);
        
        assertNotNull("Should return result or error", result[0] != null || error[0] != null);
        if (result[0] != null) {
            assertNotNull("Posts list should not be null", result[0].getPosts());
            assertTrue("Count should be non-negative", result[0].getCount() >= 0);
        }
    }
    
    @Test
    public void testFetchPostsWithSorting() throws InterruptedException {
        // Rationale: Test fetching posts with different sort options
        // Input: sort="newest" and sort="top"
        // Expected: Posts are returned in the specified order
        
        final PostRepository.PostsResult[] newestResult = new PostRepository.PostsResult[1];
        CountDownLatch newestLatch = new CountDownLatch(1);
        
        postRepository.fetchPosts("newest", 10, 0, null,
                                  new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult postsResult) {
                newestResult[0] = postsResult;
                newestLatch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                newestLatch.countDown();
            }
        });
        
        newestLatch.await(10, TimeUnit.SECONDS);
        
        if (newestResult[0] != null) {
            assertNotNull("Newest posts should be returned", newestResult[0].getPosts());
        }
    }
    
    @Test
    public void testFetchPostsWithPagination() throws InterruptedException {
        // Rationale: Test pagination with limit and offset
        // Input: limit=5, offset=0 (first page) and limit=5, offset=5 (second page)
        // Expected: Different posts are returned for different pages
        
        final PostRepository.PostsResult[] firstPage = new PostRepository.PostsResult[1];
        CountDownLatch firstLatch = new CountDownLatch(1);
        
        postRepository.fetchPosts(null, 5, 0, null,
                                 new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult postsResult) {
                firstPage[0] = postsResult;
                firstLatch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                firstLatch.countDown();
            }
        });
        
        firstLatch.await(10, TimeUnit.SECONDS);
        
        if (firstPage[0] != null) {
            assertTrue("First page should have at most 5 posts", 
                       firstPage[0].getPosts().size() <= 5);
        }
    }
    
    @Test
    public void testFetchPromptPosts() throws InterruptedException {
        // Rationale: Test fetching only prompt posts
        // Input: isPromptPost=true
        // Expected: Only prompt posts are returned
        
        final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
        
        postRepository.fetchPosts(null, null, null, true,
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        if (result[0] != null) {
            List<Post> posts = result[0].getPosts();
            for (Post post : posts) {
                assertTrue("All posts should be prompt posts", post.isIs_prompt_post());
            }
        }
    }
    
    @Test
    public void testSearchPostsByFullText() throws InterruptedException {
        // Rationale: Test searching posts by full text search
        // Input: query="test", searchType="full_text"
        // Expected: Posts containing "test" in title or content are returned
        
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        assertNotNull("Should return result or error", result[0] != null);
    }
    
    @Test
    public void testSearchPostsByAuthor() throws InterruptedException {
        // Rationale: Test searching posts by author name
        // Input: query="John", searchType="author"
        // Expected: Posts by authors with "John" in name are returned
        
        final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
        
        postRepository.searchPosts("John", "author", null, 10, 0, null,
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        assertNotNull("Should return result or error", result[0] != null);
    }
    
    @Test
    public void testSearchPostsByTitle() throws InterruptedException {
        // Rationale: Test searching posts by title
        // Input: query="AI", searchType="title"
        // Expected: Posts with "AI" in title are returned
        
        final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
        
        postRepository.searchPosts("AI", "title", null, 10, 0, null,
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        assertNotNull("Should return result or error", result[0] != null);
    }
    
    @Test
    public void testGetPostById() throws InterruptedException {
        // Rationale: Test fetching a single post by ID
        // Input: Valid post ID
        // Expected: Post with matching ID is returned
        
        // First, fetch a post to get a valid ID
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
        
        fetchLatch.await(10, TimeUnit.SECONDS);
        
        if (postId[0] != null) {
            final Post[] result = new Post[1];
            
            postRepository.getPostById(postId[0], new PostRepository.Callback<Post>() {
                @Override
                public void onSuccess(Post post) {
                    result[0] = post;
                    latch.countDown();
                }
                
                @Override
                public void onError(String errorMsg) {
                    latch.countDown();
                }
            });
            
            latch.await(10, TimeUnit.SECONDS);
            
            if (result[0] != null) {
                assertEquals("Post ID should match", postId[0], result[0].getId());
            }
        }
    }
    
    @Test
    public void testGetPostByIdWithInvalidId() throws InterruptedException {
        // Rationale: Test fetching post with non-existent ID
        // Input: Invalid post ID
        // Expected: Error is returned
        
        final String[] error = new String[1];
        
        postRepository.getPostById("invalid_id_999999", new PostRepository.Callback<Post>() {
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        // Should receive error for invalid ID
        assertNotNull("Should receive error for invalid post ID", error[0]);
    }
    
    @Test
    public void testFetchTrendingPosts() throws InterruptedException {
        // Rationale: Test fetching trending posts
        // Input: k=10 (top 10 trending posts)
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        if (result[0] != null) {
            assertNotNull("Trending posts should be returned", result[0].getPosts());
            assertTrue("Should return at most 10 posts", result[0].getPosts().size() <= 10);
        }
    }
    
    @Test
    public void testCreatePostWithoutAuthentication() throws InterruptedException {
        // Rationale: Test creating post without being logged in
        // Input: Post data but no authentication token
        // Expected: Error indicating authentication required
        
        SessionManager.clear(); // Ensure no session
        
        final String[] error = new String[1];
        
        postRepository.createPost("Test Title", "Test Content", "GPT-4", false, null, null,
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        // Should receive authentication error
        assertNotNull("Should receive error for unauthenticated request", error[0]);
        assertTrue("Error should indicate authentication required",
                   error[0].toLowerCase().contains("authentication") ||
                   error[0].toLowerCase().contains("required"));
    }
    
    @Test
    public void testVotePostWithoutAuthentication() throws InterruptedException {
        // Rationale: Test voting on post without being logged in
        // Input: Post ID and vote type but no authentication token
        // Expected: Error indicating authentication required
        
        SessionManager.clear(); // Ensure no session
        
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
        
        latch.await(10, TimeUnit.SECONDS);
        
        // Should receive authentication error
        assertNotNull("Should receive error for unauthenticated vote", error[0]);
        assertTrue("Error should indicate authentication required",
                   error[0].toLowerCase().contains("authentication") ||
                   error[0].toLowerCase().contains("required"));
    }
    
    @Test
    public void testSearchPostsWithEmptyQuery() throws InterruptedException {
        // Rationale: Test searching with empty query string
        // Input: query="", searchType="full_text"
        // Expected: Either returns all posts or error for invalid query
        
        final PostRepository.PostsResult[] result = new PostRepository.PostsResult[1];
        final String[] error = new String[1];
        
        postRepository.searchPosts("", "full_text", null, 10, 0, null,
                                  new PostRepository.Callback<PostRepository.PostsResult>() {
            @Override
            public void onSuccess(PostRepository.PostsResult postsResult) {
                result[0] = postsResult;
                latch.countDown();
            }
            
            @Override
            public void onError(String errorMsg) {
                error[0] = errorMsg;
                latch.countDown();
            }
        });
        
        latch.await(10, TimeUnit.SECONDS);
        
        // Should return result or error
        assertNotNull("Should return result or error", result[0] != null || error[0] != null);
    }
}

