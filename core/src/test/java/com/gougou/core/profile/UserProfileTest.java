package com.gougou.core.profile;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    void testDefaultProfile() {
        UserProfile profile = new UserProfile();
        assertEquals("Player", profile.getUsername());
        assertEquals("Player", profile.getDisplayName());
        assertEquals(0, profile.getCharacterClass());
        assertEquals(0, profile.getSkinColor());
        assertEquals(0, profile.getGamesPlayed());
    }

    @Test
    void testProfileWithUsername() {
        UserProfile profile = new UserProfile("Hero");
        assertEquals("Hero", profile.getUsername());
        assertEquals("Hero", profile.getDisplayName());
    }

    @Test
    void testCharacterClassName() {
        UserProfile profile = new UserProfile();
        profile.setCharacterClass(0);
        assertEquals("Warrior", profile.getClassName());
        profile.setCharacterClass(1);
        assertEquals("Mage", profile.getClassName());
        profile.setCharacterClass(2);
        assertEquals("Ranger", profile.getClassName());
    }

    @Test
    void testGamesPlayed() {
        UserProfile profile = new UserProfile();
        assertEquals(0, profile.getGamesPlayed());
        profile.incrementGamesPlayed();
        profile.incrementGamesPlayed();
        assertEquals(2, profile.getGamesPlayed());
    }

    @Test
    void testPlayTime() {
        UserProfile profile = new UserProfile();
        assertEquals(0, profile.getTotalPlayTimeMinutes());
        profile.addPlayTime(30);
        profile.addPlayTime(60);
        assertEquals(90, profile.getTotalPlayTimeMinutes());
    }

    @Test
    void testCreatedAt() {
        long before = System.currentTimeMillis();
        UserProfile profile = new UserProfile();
        long after = System.currentTimeMillis();
        assertTrue(profile.getCreatedAt() >= before && profile.getCreatedAt() <= after);
    }
}
