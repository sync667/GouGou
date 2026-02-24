package com.gougou.core.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testPlayerCreation() {
        Player player = new Player(1, "TestPlayer", 10, 20);
        assertEquals(1, player.getEntityId());
        assertEquals("TestPlayer", player.getName());
        assertEquals(10f, player.getX());
        assertEquals(20f, player.getY());
        assertEquals(100, player.getMaxHealth());
        assertEquals(100, player.getHealth());
        assertTrue(player.isAlive());
    }

    @Test
    void testDamageAndHeal() {
        Player player = new Player(1, "Test", 0, 0);
        player.damage(30);
        assertEquals(70, player.getHealth());
        assertTrue(player.isAlive());

        player.heal(20);
        assertEquals(90, player.getHealth());

        player.heal(100);
        assertEquals(100, player.getHealth()); // Should not exceed max
    }

    @Test
    void testLethalDamage() {
        Player player = new Player(1, "Test", 0, 0);
        player.damage(150);
        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    @Test
    void testPlayerManaSystem() {
        Player player = new Player(1, "Test", 0, 0);
        assertEquals(50, player.getMana());
        assertEquals(50, player.getMaxMana());

        player.useMana(20);
        assertEquals(30, player.getMana());

        player.restoreMana(10);
        assertEquals(40, player.getMana());

        player.restoreMana(100);
        assertEquals(50, player.getMana()); // Should not exceed max
    }

    @Test
    void testPlayerLevelUp() {
        Player player = new Player(1, "Test", 0, 0);
        assertEquals(1, player.getLevel());
        assertEquals(100, player.getExpForNextLevel());

        player.addExperience(100);
        assertEquals(2, player.getLevel());
        assertEquals(0, player.getExperience());
        assertEquals(110, player.getMaxHealth()); // +10 per level
    }

    @Test
    void testPlayerInventory() {
        Player player = new Player(1, "Test", 0, 0);
        assertTrue(player.getInventory().isEmpty());

        player.addItem("Sword");
        player.addItem("Shield");
        assertEquals(2, player.getInventory().size());
        assertTrue(player.hasItem("Sword"));
        assertFalse(player.hasItem("Axe"));

        player.removeItem("Sword");
        assertFalse(player.hasItem("Sword"));
    }

    @Test
    void testPlayerCharacterClass() {
        Player player = new Player(1, "Test", 0, 0);
        assertEquals(0, player.getCharacterClass()); // Default warrior
        player.setCharacterClass(1);
        assertEquals(1, player.getCharacterClass());
    }

    @Test
    void testMobCreation() {
        Mob mob = new Mob(1, Mob.MobType.SLIME, 5, 10);
        assertEquals("SLIME", mob.getName());
        assertEquals(20, mob.getMaxHealth());
        assertEquals(5f, mob.getX());
        assertEquals(10f, mob.getY());
    }

    @Test
    void testMobTypes() {
        for (Mob.MobType type : Mob.MobType.values()) {
            Mob mob = new Mob(1, type, 0, 0);
            assertTrue(mob.getMaxHealth() > 0, type + " has no health");
            assertTrue(mob.getSpeed() > 0, type + " has no speed");
        }
    }
}
