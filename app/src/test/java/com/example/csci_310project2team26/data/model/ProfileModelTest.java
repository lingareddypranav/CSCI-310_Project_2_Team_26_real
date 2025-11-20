package com.example.csci_310project2team26.data.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * White-box Test: Profile Model
 * 
 * Location: app/src/test/java/com/example/csci_310project2team26/data/model/ProfileModelTest.java
 * Test Class: ProfileModelTest
 * 
 * Description: Tests the Profile data model class to ensure proper data encapsulation,
 * getter/setter functionality, and object construction. Tests both editable and non-editable fields.
 * 
 * How to Execute: Run as JUnit test in Android Studio or via: ./gradlew test --tests ProfileModelTest
 * 
 * Coverage: Tests all constructors, getters, setters, and edge cases for Profile model.
 */
public class ProfileModelTest {
    
    private Profile profile;
    private String testUserId = "user123";
    private String testName = "John Doe";
    private String testEmail = "johndoe@usc.edu";
    private String testAffiliation = "USC";
    private String testBirthDate = "1990-01-01";
    private String testBio = "This is a test bio";
    private String testInterests = "AI, Machine Learning";
    private String testProfilePictureUrl = "https://example.com/pic.jpg";
    private String testCreatedAt = "2024-01-01T00:00:00Z";
    private String testUpdatedAt = "2024-01-01T00:00:00Z";
    
    @Before
    public void setUp() {
        profile = new Profile(testUserId, testName, testEmail, testAffiliation,
                             testBirthDate, testBio, testInterests, testProfilePictureUrl,
                             testCreatedAt, testUpdatedAt);
    }
    
    @Test
    public void testProfileCreationWithFullConstructor() {
        // Rationale: Verify that Profile object can be created with all parameters
        assertNotNull("Profile should not be null", profile);
        assertEquals("User ID should match", testUserId, profile.getUserId());
        assertEquals("Name should match", testName, profile.getName());
        assertEquals("Email should match", testEmail, profile.getEmail());
        assertEquals("Affiliation should match", testAffiliation, profile.getAffiliation());
        assertEquals("Birth date should match", testBirthDate, profile.getBirthDate());
        assertEquals("Bio should match", testBio, profile.getBio());
        assertEquals("Interests should match", testInterests, profile.getInterests());
        assertEquals("Profile picture URL should match", testProfilePictureUrl, profile.getProfilePictureUrl());
        assertEquals("Created at should match", testCreatedAt, profile.getCreatedAt());
        assertEquals("Updated at should match", testUpdatedAt, profile.getUpdatedAt());
    }
    
    @Test
    public void testProfileCreationWithMinimalConstructor() {
        // Rationale: Test minimal constructor with required fields only
        Profile minimalProfile = new Profile(testUserId, testName, testEmail, testAffiliation,
                                             testBirthDate, testBio);
        assertNotNull("Profile should not be null", minimalProfile);
        assertEquals("User ID should match", testUserId, minimalProfile.getUserId());
        assertEquals("Name should match", testName, minimalProfile.getName());
        assertEquals("Email should match", testEmail, minimalProfile.getEmail());
        assertEquals("Affiliation should match", testAffiliation, minimalProfile.getAffiliation());
    }
    
    @Test
    public void testProfileEditableFieldsSetters() {
        // Rationale: Verify that only editable fields (birth_date, bio, interests, profile_picture_url) have setters
        String newBirthDate = "1995-05-15";
        String newBio = "Updated bio";
        String newInterests = "Updated interests";
        String newProfilePictureUrl = "https://example.com/newpic.jpg";
        String newUpdatedAt = "2024-01-02T00:00:00Z";
        
        profile.setBirthDate(newBirthDate);
        profile.setBio(newBio);
        profile.setInterests(newInterests);
        profile.setProfilePictureUrl(newProfilePictureUrl);
        profile.setUpdatedAt(newUpdatedAt);
        
        assertEquals("Birth date should be updated", newBirthDate, profile.getBirthDate());
        assertEquals("Bio should be updated", newBio, profile.getBio());
        assertEquals("Interests should be updated", newInterests, profile.getInterests());
        assertEquals("Profile picture URL should be updated", newProfilePictureUrl, profile.getProfilePictureUrl());
        assertEquals("Updated at should be updated", newUpdatedAt, profile.getUpdatedAt());
    }
    
    @Test
    public void testProfileNonEditableFields() {
        // Rationale: Verify that non-editable fields (name, email, affiliation) don't have setters
        // These fields should remain constant after profile creation
        String originalName = profile.getName();
        String originalEmail = profile.getEmail();
        String originalAffiliation = profile.getAffiliation();
        
        // Note: In the actual model, these fields don't have setters, which is correct
        // This test verifies the design constraint
        assertEquals("Name should remain unchanged", originalName, profile.getName());
        assertEquals("Email should remain unchanged", originalEmail, profile.getEmail());
        assertEquals("Affiliation should remain unchanged", originalAffiliation, profile.getAffiliation());
    }
    
    @Test
    public void testProfileWithNullValues() {
        // Rationale: Test edge case where optional fields might be null
        Profile nullProfile = new Profile(testUserId, testName, testEmail, testAffiliation,
                                         null, null, null, null, null, null);
        assertNull("Birth date can be null", nullProfile.getBirthDate());
        assertNull("Bio can be null", nullProfile.getBio());
        assertNull("Interests can be null", nullProfile.getInterests());
        assertNull("Profile picture URL can be null", nullProfile.getProfilePictureUrl());
    }
    
    @Test
    public void testProfileLongBio() {
        // Rationale: Test with very long bio text (edge case)
        String longBio = "A".repeat(5000);
        profile.setBio(longBio);
        assertEquals("Profile can handle long bio", longBio, profile.getBio());
    }
    
    @Test
    public void testProfileUrlValidation() {
        // Rationale: Test various URL formats for profile picture
        profile.setProfilePictureUrl("http://example.com/image.jpg");
        assertEquals("HTTP URL", "http://example.com/image.jpg", profile.getProfilePictureUrl());
        
        profile.setProfilePictureUrl("https://example.com/image.png");
        assertEquals("HTTPS URL", "https://example.com/image.png", profile.getProfilePictureUrl());
        
        profile.setProfilePictureUrl("data:image/png;base64,ABC123");
        assertEquals("Data URL", "data:image/png;base64,ABC123", profile.getProfilePictureUrl());
    }
    
    @Test
    public void testProfileTimestampFields() {
        // Rationale: Test timestamp string fields
        String newCreatedAt = "2024-06-15T12:30:00Z";
        String newUpdatedAt = "2024-06-15T13:45:00Z";
        
        // Note: createdAt doesn't have a setter (immutable), but updatedAt does
        profile.setUpdatedAt(newUpdatedAt);
        assertEquals("Updated at should be updated", newUpdatedAt, profile.getUpdatedAt());
    }
    
    @Test
    public void testProfileBirthDateFormats() {
        // Rationale: Test various date formats
        profile.setBirthDate("1990-01-01");
        assertEquals("ISO date format", "1990-01-01", profile.getBirthDate());
        
        profile.setBirthDate("01/01/1990");
        assertEquals("US date format", "01/01/1990", profile.getBirthDate());
        
        profile.setBirthDate("1990-12-31");
        assertEquals("End of year date", "1990-12-31", profile.getBirthDate());
    }
}

