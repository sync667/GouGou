package com.gougou.core.world;

public class World {
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private final long seed;
    private int spawnX;
    private int spawnY;

    public World(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        WorldGenerator generator = new WorldGenerator(seed);
        this.tiles = generator.generate(width, height);
        findSpawnPoint();
    }

    public World(int width, int height) {
        this(width, height, System.currentTimeMillis());
    }

    public World(TileType[][] tiles, long seed) {
        this.width = tiles.length;
        this.height = tiles[0].length;
        this.tiles = tiles;
        this.seed = seed;
        findSpawnPoint();
    }

    private void findSpawnPoint() {
        // Find a walkable tile near center
        int cx = width / 2;
        int cy = height / 2;
        for (int r = 0; r < Math.max(width, height); r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int x = cx + dx;
                    int y = cy + dy;
                    if (x >= 0 && x < width && y >= 0 && y < height && tiles[x][y].isWalkable()) {
                        spawnX = x;
                        spawnY = y;
                        return;
                    }
                }
            }
        }
        spawnX = cx;
        spawnY = cy;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return TileType.VOID;
        return tiles[x][y];
    }

    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = type;
        }
    }

    public boolean isWalkable(int x, int y) {
        return getTile(x, y).isWalkable();
    }

    public boolean isLiquid(int x, int y) {
        return getTile(x, y).isLiquid();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getSeed() { return seed; }
    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }
    public TileType[][] getTiles() { return tiles; }
}
