package com.example.csci_310project2team26.data.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * White-box Test: User Model
 * 
 * Location: app/src/test/java/com/example/csci_310project2team26/data/model/UserModelTest.java
 * Test Class: UserModelTest
 * 
 * Description: Tests the User data model class to ensure proper data encapsulation,
 * getter/setter functionality, and object construction.
 * 
 * How to Execute: Run as JUnit test in Android Studio or via: ./gradlew test --tests UserModelTest
 * 
 * Coverage: Tests all constructors, getters, setters, and edge cases for User model.
 */
public class UserModelTest {
    
    private User user;
    private String testId = "user123";
    private String testName = "Test User";
    private String testEmail = "testuser@usc.edu";
    private String testStudentId = "1234567890";
    private Date testCreatedAt = new Date();
    private boolean testHasProfile = false;
    
    @Before
    public void setUp() {
        user = new User(testId, testName, testEmail, testStudentId, testCreatedAt, testHasProfile);
    }
    
    @Test
    public void testUserCreationWithFullConstructor() {
        // Rationale: Verify that User object can be created with all parameters
        assertNotNull("User should not be null", user);
        assertEquals("ID should match", testId, user.getId());
        assertEquals("Name should match", testName, user.getName());
        assertEquals("Email should match", testEmail, user.getEmail());
        assertEquals("Student ID should match", testStudentId, user.getStudentId());
        assertEquals("Created at should match", testCreatedAt, user.getCreatedAt());
        assertEquals("Has profile should match", testHasProfile, user.hasProfile());
    }
    
    @Test
    public void testUserCreationWithMinimalConstructor() {
        // Rationale: Test minimal constructor that sets hasProfile to false by default
        User minimalUser = new User(testId, testName, testEmail, testStudentId);
        assertNotNull("User should not be null", minimalUser);
        assertEquals("ID should match", testId, minimalUser.getId());
        assertEquals("Name should match", testName, minimalUser.getName());
        assertEquals("Email should match", testEmail, minimalUser.getEmail());
        assertEquals("Student ID should match", testStudentId, minimalUser.getStudentId());
        assertFalse("Has profile should default to false", minimalUser.hasProfile());
    }
    
    @Test
    public void testUserSetters() {
        // Rationale: Verify all setter methods work correctly
        String newName = "Updated Name";
        String newEmail = "newemail@usc.edu";
        String newStudentId = "9876543210";
        Date newCreatedAt = new Date(System.currentTimeMillis() + 1000);
        boolean newHasProfile = true;
        
        user.setName(newName);
        user.setEmail(newEmail);
        user.setStudentId(newStudentId);
        user.setCreatedAt(newCreatedAt);
        user.setHasProfile(newHasProfile);
        
        assertEquals("Name should be updated", newName, user.getName());
        assertEquals("Email should be updated", newEmail, user.getEmail());
        assertEquals("Student ID should be updated", newStudentId, user.getStudentId());
        assertEquals("Created at should be updated", newCreatedAt, user.getCreatedAt());
        assertEquals("Has profile should be updated", newHasProfile, user.hasProfile());
    }
    
    @Test
    public void testUserWithNullValues() {
        // Rationale: Test edge case where fields might be null
        User nullUser = new User(null, null, null, null);
        assertNull("ID can be null", nullUser.getId());
        assertNull("Name can be null", nullUser.getName());
        assertNull("Email can be null", nullUser.getEmail());
        assertNull("Student ID can be null", nullUser.getStudentId());
    }
    
    @Test
    public void testUserEmailValidation() {
        // Rationale: Test various email formats (validation logic would be in ViewModel/Repository)
        user.setEmail("valid@usc.edu");
        assertEquals("Valid USC email", "valid@usc.edu", user.getEmail());
        
        user.setEmail("test.email@usc.edu");
        assertEquals("Email with dot", "test.email@usc.edu", user.getEmail());
    }
    
    @Test
    public void testUserStudentIdFormat() {
        // Rationale: Test student ID field (10-digit validation would be in business logic)
        user.setStudentId("0000000000");
        assertEquals("Student ID with zeros", "0000000000", user.getStudentId());
        
        user.setStudentId("9999999999");
        assertEquals("Student ID with nines", "9999999999", user.getStudentId());
    }
    
    @Test
    public void testUserHasProfileFlag() {
        // Rationale: Test boolean flag for profile existence
        user.setHasProfile(true);
        assertTrue("User can have profile", user.hasProfile());
        
        user.setHasProfile(false);
        assertFalse("User can not have profile", user.hasProfile());
    }
    
    @Test
    public void testUserCreatedAtDate() {
        // Rationale: Test date field handling
        Date futureDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        user.setCreatedAt(futureDate);
        assertEquals("Created at can be set to future date", futureDate, user.getCreatedAt());
        
        Date pastDate = new Date(System.currentTimeMillis() - 86400000); // Yesterday
        user.setCreatedAt(pastDate);
        assertEquals("Created at can be set to past date", pastDate, user.getCreatedAt());
    }
    
}

