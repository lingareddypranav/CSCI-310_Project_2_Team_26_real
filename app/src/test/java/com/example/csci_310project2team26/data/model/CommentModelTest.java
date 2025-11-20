package com.example.csci_310project2team26.data.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * White-box Test: Comment Model
 * 
 * Location: app/src/test/java/com/example/csci_310project2team26/data/model/CommentModelTest.java
 * Test Class: CommentModelTest
 * 
 * Description: Tests the Comment data model class to ensure proper data encapsulation,
 * getter/setter functionality, and object construction.
 * 
 * How to Execute: Run as JUnit test in Android Studio or via: ./gradlew test --tests CommentModelTest
 * 
 * Coverage: Tests all constructors, getters, setters, and edge cases for Comment model.
 */
public class CommentModelTest {
    
    private Comment comment;
    private String testId = "comment123";
    private String testPostId = "post456";
    private String testAuthorId = "user789";
    private String testAuthorName = "Jane Smith";
    private String testText = "This is a test comment";
    private String testCreatedAt = "2024-01-01T00:00:00Z";
    private String testUpdatedAt = "2024-01-01T00:00:00Z";
    private int testUpvotes = 5;
    private int testDownvotes = 1;
    
    @Before
    public void setUp() {
        comment = new Comment(testId, testPostId, testAuthorId, testAuthorName,
                             testText, testCreatedAt, testUpdatedAt,
                             testUpvotes, testDownvotes);
    }
    
    @Test
    public void testCommentCreationWithFullConstructor() {
        // Rationale: Verify that Comment object can be created with all parameters
        assertNotNull("Comment should not be null", comment);
        assertEquals("ID should match", testId, comment.getId());
        assertEquals("Post ID should match", testPostId, comment.getPost_id());
        assertEquals("Author ID should match", testAuthorId, comment.getAuthor_id());
        assertEquals("Author name should match", testAuthorName, comment.getAuthor_name());
        assertEquals("Text should match", testText, comment.getText());
        assertEquals("Created at should match", testCreatedAt, comment.getCreated_at());
        assertEquals("Updated at should match", testUpdatedAt, comment.getUpdated_at());
        assertEquals("Upvotes should match", testUpvotes, comment.getUpvotes());
        assertEquals("Downvotes should match", testDownvotes, comment.getDownvotes());
    }
    
    @Test
    public void testCommentCreationWithEmptyConstructor() {
        // Rationale: Test default constructor for JSON deserialization
        Comment emptyComment = new Comment();
        assertNotNull("Empty comment should not be null", emptyComment);
        assertNull("ID should be null initially", emptyComment.getId());
        assertNull("Text should be null initially", emptyComment.getText());
    }
    
    @Test
    public void testCommentSetters() {
        // Rationale: Verify all setter methods work correctly
        String newText = "Updated comment text";
        String newUpdatedAt = "2024-01-02T00:00:00Z";
        int newUpvotes = 10;
        int newDownvotes = 3;
        
        comment.setText(newText);
        comment.setUpdated_at(newUpdatedAt);
        comment.setUpvotes(newUpvotes);
        comment.setDownvotes(newDownvotes);
        
        assertEquals("Text should be updated", newText, comment.getText());
        assertEquals("Updated at should be updated", newUpdatedAt, comment.getUpdated_at());
        assertEquals("Upvotes should be updated", newUpvotes, comment.getUpvotes());
        assertEquals("Downvotes should be updated", newDownvotes, comment.getDownvotes());
    }
    
    @Test
    public void testCommentWithNullValues() {
        // Rationale: Test edge case where fields might be null
        Comment nullComment = new Comment();
        nullComment.setText(null);
        nullComment.setAuthor_name(null);
        
        assertNull("Text can be null", nullComment.getText());
        assertNull("Author name can be null", nullComment.getAuthor_name());
    }
    
    @Test
    public void testCommentVoteCounts() {
        // Rationale: Verify vote count fields can handle various numeric values
        comment.setUpvotes(0);
        comment.setDownvotes(0);
        assertEquals("Upvotes can be zero", 0, comment.getUpvotes());
        assertEquals("Downvotes can be zero", 0, comment.getDownvotes());
        
        comment.setUpvotes(999);
        comment.setDownvotes(100);
        assertEquals("Upvotes can be large", 999, comment.getUpvotes());
        assertEquals("Downvotes can be large", 100, comment.getDownvotes());
    }
    
    @Test
    public void testCommentPostIdRelationship() {
        // Rationale: Verify comment maintains relationship to parent post
        String newPostId = "post999";
        comment.setPost_id(newPostId);
        assertEquals("Post ID should be updated", newPostId, comment.getPost_id());
    }
    
    @Test
    public void testCommentAuthorRelationship() {
        // Rationale: Verify comment maintains relationship to author
        String newAuthorId = "user111";
        String newAuthorName = "New Author";
        comment.setAuthor_id(newAuthorId);
        comment.setAuthor_name(newAuthorName);
        
        assertEquals("Author ID should be updated", newAuthorId, comment.getAuthor_id());
        assertEquals("Author name should be updated", newAuthorName, comment.getAuthor_name());
    }
    
    @Test
    public void testCommentTimestampFields() {
        // Rationale: Test timestamp string fields
        String newCreatedAt = "2024-06-15T12:30:00Z";
        String newUpdatedAt = "2024-06-15T13:45:00Z";
        
        comment.setCreated_at(newCreatedAt);
        comment.setUpdated_at(newUpdatedAt);
        
        assertEquals("Created at should be updated", newCreatedAt, comment.getCreated_at());
        assertEquals("Updated at should be updated", newUpdatedAt, comment.getUpdated_at());
    }
    
    @Test
    public void testCommentLongText() {
        // Rationale: Test with very long comment text (edge case)
        String longText = "A".repeat(10000);
        comment.setText(longText);
        assertEquals("Comment can handle long text", longText, comment.getText());
    }

    @Test
    public void testCommentTitle() {
        // Rationale: Test new optional title field for comments
        String title = "Great insight!";
        comment.setTitle(title);
        assertEquals("Title should be set", title, comment.getTitle());
        
        comment.setTitle(null);
        assertNull("Title can be null (optional field)", comment.getTitle());
        
        comment.setTitle("");
        assertEquals("Title can be empty string", "", comment.getTitle());
    }
}

