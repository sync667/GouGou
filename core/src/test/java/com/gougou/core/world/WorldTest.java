package com.gougou.core.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldTest {
    private World world;

    @BeforeEach
    void setUp() {
        world = new World(64, 64, 42);
    }

    @Test
    void testWorldDimensions() {
        assertEquals(64, world.getWidth());
        assertEquals(64, world.getHeight());
    }

    @Test
    void testGetTileInBounds() {
        TileType tile = world.getTile(0, 0);
        assertNotNull(tile);
    }

    @Test
    void testGetTileOutOfBoundsReturnsVoid() {
        assertEquals(TileType.VOID, world.getTile(-1, 0));
        assertEquals(TileType.VOID, world.getTile(0, -1));
        assertEquals(TileType.VOID, world.getTile(64, 0));
        assertEquals(TileType.VOID, world.getTile(0, 64));
    }

    @Test
    void testSetTile() {
        world.setTile(10, 10, TileType.LAVA);
        assertEquals(TileType.LAVA, world.getTile(10, 10));
    }

    @Test
    void testSetTileOutOfBoundsDoesNotThrow() {
        assertDoesNotThrow(() -> world.setTile(-1, -1, TileType.GRASS));
    }

    @Test
    void testSpawnPointIsWalkable() {
        int sx = world.getSpawnX();
        int sy = world.getSpawnY();
        TileType spawnTile = world.getTile(sx, sy);
        assertTrue(spawnTile.isWalkable() || spawnTile.isLiquid(),
            "Spawn point should be walkable, but is " + spawnTile);
    }

    @Test
    void testIsWalkable() {
        world.setTile(5, 5, TileType.GRASS);
        assertTrue(world.isWalkable(5, 5));
        world.setTile(5, 5, TileType.WALL_STONE);
        assertFalse(world.isWalkable(5, 5));
    }

    @Test
    void testIsLiquid() {
        world.setTile(5, 5, TileType.WATER);
        assertTrue(world.isLiquid(5, 5));
        world.setTile(5, 5, TileType.GRASS);
        assertFalse(world.isLiquid(5, 5));
    }

    @Test
    void testWorldFromTiles() {
        TileType[][] tiles = new TileType[10][10];
        for (int x = 0; x < 10; x++)
            for (int y = 0; y < 10; y++)
                tiles[x][y] = TileType.GRASS;
        tiles[5][5] = TileType.WATER;

        World w = new World(tiles, 99);
        assertEquals(10, w.getWidth());
        assertEquals(10, w.getHeight());
        assertEquals(99, w.getSeed());
        assertEquals(TileType.WATER, w.getTile(5, 5));
    }
}
