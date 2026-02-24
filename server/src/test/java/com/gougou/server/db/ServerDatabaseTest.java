package com.gougou.server.db;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerDatabaseTest {
    private static Path tempDir;
    private ServerDatabase db;

    @BeforeAll
    static void setupDir() throws IOException {
        tempDir = Files.createTempDirectory("gougou-test-db");
    }

    @BeforeEach
    void setUp() throws SQLException {
        db = new ServerDatabase(tempDir.resolve("test-" + System.nanoTime()).toString());
    }

    @AfterEach
    void tearDown() {
        if (db != null) db.close();
    }

    @Test
    void testSaveAndLoadPlayer() {
        List<String> inventory = List.of("Sword", "Shield");
        db.savePlayer("Hero", 1, 2, 5, 250, 90, 110, 40, 55, inventory, 10.5f, 20.3f);

        ServerDatabase.PlayerRecord record = db.loadPlayer("Hero");
        assertTrue(record.found);
        assertEquals("Hero", record.username);
        assertEquals(1, record.characterClass);
        assertEquals(2, record.skinColor);
        assertEquals(5, record.level);
        assertEquals(250, record.experience);
        assertEquals(90, record.health);
        assertEquals(110, record.maxHealth);
        assertEquals(40, record.mana);
        assertEquals(55, record.maxMana);
        assertEquals(10.5f, record.lastX, 0.01f);
        assertEquals(20.3f, record.lastY, 0.01f);
        assertEquals(2, record.inventory.size());
        assertEquals("Sword", record.inventory.get(0));
        assertEquals("Shield", record.inventory.get(1));
    }

    @Test
    void testLoadNonExistentPlayer() {
        ServerDatabase.PlayerRecord record = db.loadPlayer("Ghost");
        assertFalse(record.found);
    }

    @Test
    void testUpdatePlayer() {
        db.savePlayer("Hero", 0, 0, 1, 0, 100, 100, 50, 50, List.of(), 0, 0);
        db.savePlayer("Hero", 0, 0, 2, 50, 95, 110, 45, 55, List.of("Axe"), 5, 5);

        ServerDatabase.PlayerRecord record = db.loadPlayer("Hero");
        assertTrue(record.found);
        assertEquals(2, record.level);
        assertEquals(50, record.experience);
        assertEquals(1, record.inventory.size());
        assertEquals("Axe", record.inventory.get(0));
    }

    @Test
    void testBanAndCheckPlayer() {
        assertFalse(db.isPlayerBanned("Cheater"));
        db.banPlayer("Cheater", "Using hacks");
        assertTrue(db.isPlayerBanned("Cheater"));
        assertFalse(db.isPlayerBanned("GoodPlayer"));
    }

    @Test
    void testSavePlayerEmptyInventory() {
        db.savePlayer("Noob", 0, 0, 1, 0, 100, 100, 50, 50, List.of(), 0, 0);
        ServerDatabase.PlayerRecord record = db.loadPlayer("Noob");
        assertTrue(record.found);
        assertTrue(record.inventory.isEmpty());
    }
}
