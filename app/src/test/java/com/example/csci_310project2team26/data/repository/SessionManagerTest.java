package com.example.csci_310project2team26.data.repository;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * White-box Test: SessionManager
 * 
 * Location: app/src/test/java/com/example/csci_310project2team26/data/repository/SessionManagerTest.java
 * Test Class: SessionManagerTest
 * 
 * Description: Tests the SessionManager singleton class to ensure proper session management,
 * token storage, and cleanup functionality.
 * 
 * How to Execute: Run as JUnit test in Android Studio or via: ./gradlew test --tests SessionManagerTest
 * 
 * Coverage: Tests all static methods, session lifecycle, and edge cases.
 */
public class SessionManagerTest {
    
    private String testToken = "test_auth_token_12345";
    private String testUserId = "user123";
    
    @Before
    public void setUp() {
        // Clear any existing session before each test
        SessionManager.clear();
    }
    
    @Test
    public void testSessionManagerSetSession() {
        // Rationale: Verify that session can be set with token and user ID
        SessionManager.setSession(testToken, testUserId);
        
        assertEquals("Token should be stored", testToken, SessionManager.getToken());
        assertEquals("User ID should be stored", testUserId, SessionManager.getUserId());
    }
    
    @Test
    public void testSessionManagerGetToken() {
        // Rationale: Test token retrieval after setting session
        SessionManager.setSession(testToken, testUserId);
        String retrievedToken = SessionManager.getToken();
        
        assertNotNull("Token should not be null", retrievedToken);
        assertEquals("Token should match stored value", testToken, retrievedToken);
    }
    
    @Test
    public void testSessionManagerGetUserId() {
        // Rationale: Test user ID retrieval after setting session
        SessionManager.setSession(testToken, testUserId);
        String retrievedUserId = SessionManager.getUserId();
        
        assertNotNull("User ID should not be null", retrievedUserId);
        assertEquals("User ID should match stored value", testUserId, retrievedUserId);
    }
    
    @Test
    public void testSessionManagerClear() {
        // Rationale: Verify that clear() removes all session data
        SessionManager.setSession(testToken, testUserId);
        assertNotNull("Token should exist before clear", SessionManager.getToken());
        assertNotNull("User ID should exist before clear", SessionManager.getUserId());
        
        SessionManager.clear();
        
        assertNull("Token should be null after clear", SessionManager.getToken());
        assertNull("User ID should be null after clear", SessionManager.getUserId());
    }
    
    @Test
    public void testSessionManagerWithNullToken() {
        // Rationale: Test edge case where token is null
        SessionManager.setSession(null, testUserId);
        assertNull("Token can be null", SessionManager.getToken());
        assertEquals("User ID should still be stored", testUserId, SessionManager.getUserId());
    }
    
    @Test
    public void testSessionManagerWithNullUserId() {
        // Rationale: Test edge case where user ID is null
        SessionManager.setSession(testToken, null);
        assertEquals("Token should still be stored", testToken, SessionManager.getToken());
        assertNull("User ID can be null", SessionManager.getUserId());
    }
    
    @Test
    public void testSessionManagerUpdateSession() {
        // Rationale: Verify that session can be updated by calling setSession again
        SessionManager.setSession("old_token", "old_user");
        assertEquals("Initial token", "old_token", SessionManager.getToken());
        
        SessionManager.setSession("new_token", "new_user");
        assertEquals("Updated token", "new_token", SessionManager.getToken());
        assertEquals("Updated user ID", "new_user", SessionManager.getUserId());
    }
    
    @Test
    public void testSessionManagerInitialState() {
        // Rationale: Verify initial state after clear (or before any setSession call)
        SessionManager.clear();
        assertNull("Token should be null initially", SessionManager.getToken());
        assertNull("User ID should be null initially", SessionManager.getUserId());
    }
    
}

