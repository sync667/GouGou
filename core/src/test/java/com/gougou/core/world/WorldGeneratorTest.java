package com.gougou.core.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldGeneratorTest {

    @Test
    void testGenerateProducesCorrectSize() {
        WorldGenerator gen = new WorldGenerator(42);
        TileType[][] tiles = gen.generate(64, 64);
        assertEquals(64, tiles.length);
        assertEquals(64, tiles[0].length);
    }

    @Test
    void testGenerateIsDeterministic() {
        WorldGenerator gen1 = new WorldGenerator(12345);
        WorldGenerator gen2 = new WorldGenerator(12345);
        TileType[][] tiles1 = gen1.generate(32, 32);
        TileType[][] tiles2 = gen2.generate(32, 32);

        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                assertEquals(tiles1[x][y], tiles2[x][y],
                    "Mismatch at (" + x + "," + y + ")");
            }
        }
    }

    @Test
    void testDifferentSeedsProduceDifferentWorlds() {
        WorldGenerator gen1 = new WorldGenerator(1);
        WorldGenerator gen2 = new WorldGenerator(999);
        TileType[][] tiles1 = gen1.generate(64, 64);
        TileType[][] tiles2 = gen2.generate(64, 64);

        boolean anyDifferent = false;
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                if (tiles1[x][y] != tiles2[x][y]) {
                    anyDifferent = true;
                    break;
                }
            }
            if (anyDifferent) break;
        }
        assertTrue(anyDifferent, "Different seeds should produce different worlds");
    }

    @Test
    void testGenerateContainsVariety() {
        WorldGenerator gen = new WorldGenerator(42);
        TileType[][] tiles = gen.generate(128, 128);

        java.util.Set<TileType> found = new java.util.HashSet<>();
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                found.add(tiles[x][y]);
            }
        }
        assertTrue(found.size() >= 5, "World should contain at least 5 different tile types, found: " + found.size());
    }

    @Test
    void testNoNullTiles() {
        WorldGenerator gen = new WorldGenerator(42);
        TileType[][] tiles = gen.generate(64, 64);

        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                assertNotNull(tiles[x][y], "Tile at (" + x + "," + y + ") is null");
            }
        }
    }

    @Test
    void testSeedIsPreserved() {
        WorldGenerator gen = new WorldGenerator(42);
        assertEquals(42, gen.getSeed());
    }
}
