package com.gougou.core.entities;

import com.gougou.core.world.TileType;
import com.gougou.core.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {
    private EntityManager manager;
    private World world;

    @BeforeEach
    void setUp() {
        TileType[][] tiles = new TileType[32][32];
        for (int x = 0; x < 32; x++)
            for (int y = 0; y < 32; y++)
                tiles[x][y] = TileType.GRASS;
        world = new World(tiles, 42);

        manager = new EntityManager();
        manager.setWorld(world);
    }

    @Test
    void testSpawnPlayer() {
        Player player = manager.spawnPlayer("Test", 5, 5);
        assertNotNull(player);
        assertEquals("Test", player.getName());
        assertEquals(1, manager.getEntityCount());
    }

    @Test
    void testSpawnMob() {
        Mob mob = manager.spawnMob(Mob.MobType.SLIME, 10, 10);
        assertNotNull(mob);
        assertEquals(1, manager.getEntityCount());
    }

    @Test
    void testRemoveEntity() {
        Player player = manager.spawnPlayer("Test", 5, 5);
        assertEquals(1, manager.getEntityCount());
        manager.removeEntity(player.getEntityId());
        assertEquals(0, manager.getEntityCount());
    }

    @Test
    void testGetEntity() {
        Player player = manager.spawnPlayer("Test", 5, 5);
        Entity found = manager.getEntity(player.getEntityId());
        assertSame(player, found);
    }

    @Test
    void testGetEntitiesOfType() {
        manager.spawnPlayer("Player1", 5, 5);
        manager.spawnMob(Mob.MobType.SLIME, 10, 10);
        manager.spawnMob(Mob.MobType.WOLF, 15, 15);

        assertEquals(1, manager.getEntitiesOfType(Player.class).size());
        assertEquals(2, manager.getEntitiesOfType(Mob.class).size());
    }

    @Test
    void testUpdateRemovesDeadEntities() {
        Mob mob = manager.spawnMob(Mob.MobType.SLIME, 10, 10);
        assertEquals(1, manager.getEntityCount());
        mob.damage(999);
        manager.update(0.016f);
        assertEquals(0, manager.getEntityCount());
    }

    @Test
    void testUniqueEntityIds() {
        Player p1 = manager.spawnPlayer("P1", 5, 5);
        Player p2 = manager.spawnPlayer("P2", 10, 10);
        Mob m1 = manager.spawnMob(Mob.MobType.SLIME, 15, 15);

        assertNotEquals(p1.getEntityId(), p2.getEntityId());
        assertNotEquals(p1.getEntityId(), m1.getEntityId());
        assertNotEquals(p2.getEntityId(), m1.getEntityId());
    }
}
