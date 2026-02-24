package com.gougou.core.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TileTypeTest {

    @Test
    void testAllTileTypesHaveUniqueIds() {
        TileType[] types = TileType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotEquals(types[i].getId(), types[j].getId(),
                    types[i].name() + " and " + types[j].name() + " share ID " + types[i].getId());
            }
        }
    }

    @Test
    void testFromId() {
        assertEquals(TileType.GRASS, TileType.fromId(1));
        assertEquals(TileType.WATER, TileType.fromId(4));
        assertEquals(TileType.VOID, TileType.fromId(-1));
        assertEquals(TileType.VOID, TileType.fromId(9999));
    }

    @Test
    void testWaterIsLiquid() {
        assertTrue(TileType.WATER.isLiquid());
        assertTrue(TileType.DEEP_WATER.isLiquid());
        assertTrue(TileType.SWAMP.isLiquid());
    }

    @Test
    void testWalkableTiles() {
        assertTrue(TileType.GRASS.isWalkable());
        assertTrue(TileType.DIRT.isWalkable());
        assertTrue(TileType.SAND.isWalkable());
        assertTrue(TileType.PATH.isWalkable());
        assertTrue(TileType.BRIDGE.isWalkable());
    }

    @Test
    void testSolidTiles() {
        assertFalse(TileType.TREE_OAK.isWalkable());
        assertFalse(TileType.ROCK.isWalkable());
        assertFalse(TileType.WALL_STONE.isWalkable());
        assertFalse(TileType.WALL_WOOD.isWalkable());
        assertFalse(TileType.LAVA.isWalkable());
    }

    @Test
    void testDisplayNames() {
        assertEquals("Grass", TileType.GRASS.getDisplayName());
        assertEquals("Deep Water", TileType.DEEP_WATER.getDisplayName());
        assertEquals("Oak Tree", TileType.TREE_OAK.getDisplayName());
        assertEquals("Stone Wall", TileType.WALL_STONE.getDisplayName());
    }

    @Test
    void testAllTileTypesHaveColor() {
        for (TileType type : TileType.values()) {
            assertNotNull(type.getColor(), type.name() + " has null color");
        }
    }

    @Test
    void testAllTileTypesHaveDisplayName() {
        for (TileType type : TileType.values()) {
            assertNotNull(type.getDisplayName(), type.name() + " has null display name");
            assertFalse(type.getDisplayName().isEmpty(), type.name() + " has empty display name");
        }
    }
}
