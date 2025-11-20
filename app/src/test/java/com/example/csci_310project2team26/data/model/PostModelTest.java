package com.example.csci_310project2team26.data.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * White-box Test: Post Model
 * 
 * Location: app/src/test/java/com/example/csci_310project2team26/data/model/PostModelTest.java
 * Test Class: PostModelTest
 * 
 * Description: Tests the Post data model class to ensure proper data encapsulation,
 * getter/setter functionality, and object construction.
 * 
 * How to Execute: Run as JUnit test in Android Studio or via: ./gradlew test --tests PostModelTest
 * 
 * Coverage: Tests all constructors, getters, setters, and edge cases for Post model.
 */
public class PostModelTest {
    
    private Post post;
    private String testId = "post123";
    private String testAuthorId = "user456";
    private String testAuthorName = "John Doe";
    private String testTitle = "Test Post Title";
    private String testContent = "This is test content";
    private String testLlmTag = "GPT-4";
    private boolean testIsPromptPost = false;
    private String testCreatedAt = "2024-01-01T00:00:00Z";
    private String testUpdatedAt = "2024-01-01T00:00:00Z";
    private int testUpvotes = 10;
    private int testDownvotes = 2;
    private int testCommentCount = 5;
    
    @Before
    public void setUp() {
        post = new Post(testId, testAuthorId, testAuthorName, testTitle, testContent,
                       testLlmTag, testIsPromptPost, testCreatedAt, testUpdatedAt,
                       testUpvotes, testDownvotes, testCommentCount);
    }
    
    @Test
    public void testPostCreationWithFullConstructor() {
        // Rationale: Verify that Post object can be created with all parameters
        // and all fields are correctly initialized
        assertNotNull("Post should not be null", post);
        assertEquals("ID should match", testId, post.getId());
        assertEquals("Author ID should match", testAuthorId, post.getAuthor_id());
        assertEquals("Author name should match", testAuthorName, post.getAuthor_name());
        assertEquals("Title should match", testTitle, post.getTitle());
        assertEquals("Content should match", testContent, post.getContent());
        assertEquals("LLM tag should match", testLlmTag, post.getLlm_tag());
        assertEquals("Is prompt post should match", testIsPromptPost, post.isIs_prompt_post());
        assertEquals("Created at should match", testCreatedAt, post.getCreated_at());
        assertEquals("Updated at should match", testUpdatedAt, post.getUpdated_at());
        assertEquals("Upvotes should match", testUpvotes, post.getUpvotes());
        assertEquals("Downvotes should match", testDownvotes, post.getDownvotes());
        assertEquals("Comment count should match", testCommentCount, post.getComment_count());
    }
    
    @Test
    public void testPostCreationWithEmptyConstructor() {
        // Rationale: Test default constructor for JSON deserialization scenarios
        Post emptyPost = new Post();
        assertNotNull("Empty post should not be null", emptyPost);
        assertNull("ID should be null initially", emptyPost.getId());
        assertNull("Title should be null initially", emptyPost.getTitle());
    }
    
    @Test
    public void testPostSetters() {
        // Rationale: Verify all setter methods work correctly for updating post data
        String newTitle = "Updated Title";
        String newContent = "Updated Content";
        String newLlmTag = "Claude";
        boolean newIsPromptPost = true;
        int newUpvotes = 20;
        int newDownvotes = 5;
        
        post.setTitle(newTitle);
        post.setContent(newContent);
        post.setLlm_tag(newLlmTag);
        post.setIs_prompt_post(newIsPromptPost);
        post.setUpvotes(newUpvotes);
        post.setDownvotes(newDownvotes);
        
        assertEquals("Title should be updated", newTitle, post.getTitle());
        assertEquals("Content should be updated", newContent, post.getContent());
        assertEquals("LLM tag should be updated", newLlmTag, post.getLlm_tag());
        assertEquals("Is prompt post should be updated", newIsPromptPost, post.isIs_prompt_post());
        assertEquals("Upvotes should be updated", newUpvotes, post.getUpvotes());
        assertEquals("Downvotes should be updated", newDownvotes, post.getDownvotes());
    }
    
    @Test
    public void testPostWithNullValues() {
        // Rationale: Test edge case where fields might be null (e.g., from API responses)
        Post nullPost = new Post();
        nullPost.setTitle(null);
        nullPost.setContent(null);
        nullPost.setAuthor_name(null);
        
        assertNull("Title can be null", nullPost.getTitle());
        assertNull("Content can be null", nullPost.getContent());
        assertNull("Author name can be null", nullPost.getAuthor_name());
    }
    
    @Test
    public void testPostWithEmptyStrings() {
        // Rationale: Test edge case with empty string values
        post.setTitle("");
        post.setContent("");
        post.setLlm_tag("");
        
        assertEquals("Title can be empty string", "", post.getTitle());
        assertEquals("Content can be empty string", "", post.getContent());
        assertEquals("LLM tag can be empty string", "", post.getLlm_tag());
    }
    
    @Test
    public void testPostVoteCounts() {
        // Rationale: Verify vote count fields can handle various numeric values
        post.setUpvotes(0);
        post.setDownvotes(0);
        assertEquals("Upvotes can be zero", 0, post.getUpvotes());
        assertEquals("Downvotes can be zero", 0, post.getDownvotes());
        
        post.setUpvotes(1000);
        post.setDownvotes(500);
        assertEquals("Upvotes can be large", 1000, post.getUpvotes());
        assertEquals("Downvotes can be large", 500, post.getDownvotes());
    }
    
    @Test
    public void testPostPromptPostFlag() {
        // Rationale: Test boolean flag for prompt posts vs regular posts
        post.setIs_prompt_post(true);
        assertTrue("Post can be a prompt post", post.isIs_prompt_post());
        
        post.setIs_prompt_post(false);
        assertFalse("Post can be a regular post", post.isIs_prompt_post());
    }
    
    @Test
    public void testPostCommentCount() {
        // Rationale: Verify comment count field works correctly
        post.setComment_count(0);
        assertEquals("Comment count can be zero", 0, post.getComment_count());
        
        post.setComment_count(100);
        assertEquals("Comment count can be large", 100, post.getComment_count());
    }
    
    @Test
    public void testPostTimestampFields() {
        // Rationale: Test timestamp string fields for created/updated dates
        String newCreatedAt = "2024-12-31T23:59:59Z";
        String newUpdatedAt = "2025-01-01T00:00:00Z";
        
        post.setCreated_at(newCreatedAt);
        post.setUpdated_at(newUpdatedAt);
        
        assertEquals("Created at should be updated", newCreatedAt, post.getCreated_at());
        assertEquals("Updated at should be updated", newUpdatedAt, post.getUpdated_at());
    }

    @Test
    public void testPostPromptSection() {
        // Rationale: Test new prompt_section field for prompt posts
        String promptSection = "Write a story about a robot";
        post.setPrompt_section(promptSection);
        assertEquals("Prompt section should be set", promptSection, post.getPrompt_section());
        
        post.setPrompt_section(null);
        assertNull("Prompt section can be null", post.getPrompt_section());
    }

    @Test
    public void testPostDescriptionSection() {
        // Rationale: Test new description_section field for prompt posts
        String descriptionSection = "This prompt is designed to test creative writing";
        post.setDescription_section(descriptionSection);
        assertEquals("Description section should be set", descriptionSection, post.getDescription_section());
        
        post.setDescription_section(null);
        assertNull("Description section can be null", post.getDescription_section());
    }
}

