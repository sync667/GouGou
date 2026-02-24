package com.gougou.core.world;

import java.util.Random;

public class WorldGenerator {
    private final long seed;
    private final Random random;

    public WorldGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public WorldGenerator() {
        this(System.currentTimeMillis());
    }

    public long getSeed() { return seed; }

    public TileType[][] generate(int width, int height) {
        TileType[][] tiles = new TileType[width][height];
        double[][] elevation = generateNoise(width, height, 4, seed);
        double[][] moisture = generateNoise(width, height, 3, seed + 12345);
        double[][] detail = generateNoise(width, height, 8, seed + 67890);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double e = elevation[x][y];
                double m = moisture[x][y];
                double d = detail[x][y];
                tiles[x][y] = selectTile(e, m, d, x, y);
            }
        }

        placeDecorations(tiles, width, height);
        generatePaths(tiles, width, height);
        return tiles;
    }

    private TileType selectTile(double elevation, double moisture, double detail, int x, int y) {
        if (elevation < 0.22) return TileType.DEEP_WATER;
        if (elevation < 0.32) return TileType.WATER;
        if (elevation < 0.36) return TileType.SAND;

        if (elevation > 0.82) return TileType.SNOW;
        if (elevation > 0.75) {
            return moisture > 0.5 ? TileType.ICE : TileType.STONE;
        }
        if (elevation > 0.68) return TileType.STONE;

        if (moisture < 0.25) {
            return detail > 0.6 ? TileType.GRAVEL : TileType.DIRT;
        }
        if (moisture < 0.40) {
            return detail > 0.7 ? TileType.CLAY : TileType.DIRT;
        }
        if (moisture > 0.75) {
            return elevation < 0.45 ? TileType.SWAMP : TileType.TALL_GRASS;
        }
        return TileType.GRASS;
    }

    private void placeDecorations(TileType[][] tiles, int w, int h) {
        Random decRandom = new Random(seed + 99999);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                TileType base = tiles[x][y];
                if (base == TileType.GRASS) {
                    double r = decRandom.nextDouble();
                    if (r < 0.06) tiles[x][y] = TileType.TREE_OAK;
                    else if (r < 0.09) tiles[x][y] = TileType.TREE_PINE;
                    else if (r < 0.11) tiles[x][y] = TileType.BUSH;
                    else if (r < 0.125) tiles[x][y] = TileType.FLOWER_RED;
                    else if (r < 0.14) tiles[x][y] = TileType.FLOWER_YELLOW;
                    else if (r < 0.15) tiles[x][y] = TileType.MUSHROOM;
                    else if (r < 0.155) tiles[x][y] = TileType.ROCK;
                } else if (base == TileType.SNOW) {
                    if (decRandom.nextDouble() < 0.04) tiles[x][y] = TileType.TREE_PINE;
                    else decRandom.nextDouble(); // advance RNG to keep state consistent
                } else if (base == TileType.DIRT) {
                    double r = decRandom.nextDouble();
                    if (r < 0.02) tiles[x][y] = TileType.TREE_DEAD;
                    else if (r < 0.04) tiles[x][y] = TileType.ROCK;
                } else if (base == TileType.SAND) {
                    if (decRandom.nextDouble() < 0.01) tiles[x][y] = TileType.ROCK;
                    else decRandom.nextDouble(); // advance RNG to keep state consistent
                } else {
                    decRandom.nextDouble();
                }
            }
        }
    }

    private void generatePaths(TileType[][] tiles, int w, int h) {
        Random pathRandom = new Random(seed + 54321);
        int pathCount = 3 + pathRandom.nextInt(5);
        for (int p = 0; p < pathCount; p++) {
            int px = pathRandom.nextInt(w);
            int py = pathRandom.nextInt(h);
            int length = 40 + pathRandom.nextInt(60);
            int dx = pathRandom.nextInt(3) - 1;
            int dy = pathRandom.nextInt(3) - 1;
            if (dx == 0 && dy == 0) dx = 1;

            for (int i = 0; i < length; i++) {
                if (px >= 0 && px < w && py >= 0 && py < h) {
                    TileType current = tiles[px][py];
                    if (current.isWalkable() && !current.isLiquid()) {
                        tiles[px][py] = TileType.PATH;
                    }
                }
                px += dx;
                py += dy;
                if (pathRandom.nextDouble() < 0.2) dx = Math.max(-1, Math.min(1, dx + pathRandom.nextInt(3) - 1));
                if (pathRandom.nextDouble() < 0.2) dy = Math.max(-1, Math.min(1, dy + pathRandom.nextInt(3) - 1));
                if (dx == 0 && dy == 0) dx = 1;
            }
        }
    }

    private double[][] generateNoise(int width, int height, int octaves, long noiseSeed) {
        double[][] noise = new double[width][height];
        Random noiseRandom = new Random(noiseSeed);
        double offsetX = noiseRandom.nextDouble() * 10000;
        double offsetY = noiseRandom.nextDouble() * 10000;
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double value = 0;
                double amplitude = 1;
                double frequency = 0.01;
                double totalAmplitude = 0;

                for (int o = 0; o < octaves; o++) {
                    double nx = (x + offsetX) * frequency;
                    double ny = (y + offsetY) * frequency;
                    value += simplexNoise(nx, ny) * amplitude;
                    totalAmplitude += amplitude;
                    amplitude *= 0.5;
                    frequency *= 2;
                }
                noise[x][y] = value / totalAmplitude;
                if (noise[x][y] < min) min = noise[x][y];
                if (noise[x][y] > max) max = noise[x][y];
            }
        }

        // Normalize to 0-1
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (max - min > 0) {
                    noise[x][y] = (noise[x][y] - min) / (max - min);
                } else {
                    noise[x][y] = 0.5;
                }
            }
        }
        return noise;
    }

    // Simple 2D noise using permutation table
    private static final int[] PERM = new int[512];
    static {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        Random r = new Random(0);
        for (int i = 255; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) PERM[i] = p[i & 255];
    }

    private static double simplexNoise(double x, double y) {
        int ix = (int) Math.floor(x) & 255;
        int iy = (int) Math.floor(y) & 255;
        double fx = x - Math.floor(x);
        double fy = y - Math.floor(y);
        double u = fade(fx);
        double v = fade(fy);
        int a = PERM[ix] + iy;
        int b = PERM[ix + 1] + iy;
        return lerp(v, lerp(u, grad(PERM[a], fx, fy), grad(PERM[b], fx - 1, fy)),
                      lerp(u, grad(PERM[a + 1], fx, fy - 1), grad(PERM[b + 1], fx - 1, fy - 1)));
    }

    private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    private static double grad(int hash, double x, double y) {
        int h = hash & 3;
        double u = h < 2 ? x : y;
        double v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
