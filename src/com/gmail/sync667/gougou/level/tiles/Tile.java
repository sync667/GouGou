package com.gmail.sync667.gougou.level.tiles;

import com.gmail.sync667.gougou.gfx.Colours;
import com.gmail.sync667.gougou.gfx.Screen;
import com.gmail.sync667.gougou.level.Level;

public abstract class Tile {

    public static final Tile[] tiles = new Tile[256];
    public static final Tile VOID = new BasicSolidTile(0, 0, 0, Colours.get(000, -1, -1, -1), 0xFF000000, 1);
    public static final Tile STONE = new BasicSolidTile(1, 1, 0, Colours.get(-1, 10, 10, -1), 0xFF555555, 1);
    public static final Tile GRASS = new BasicTile(2, 2, 0, Colours.get(-1, 20, 70, 0), 0xFF00FF00, 1);
    public static final Tile WATER = new AnimatedTile(3, new int[][] { { 0, 5 }, { 1, 5 }, { 2, 5 }, { 1, 5 } },
            Colours.get(-1, 004, 115, -1), 0xFF0000FF, 1, 750);

    // public static final Tile DIRT = new BasicTile(4, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile GRAVEL = new BasicTile(4, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile WOOL = new BasicTile(5, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile LOG = new BasicTile(6, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile PLANK = new BasicTile(7, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile GLASS = new BasicTile(8, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile FIRE = new BasicTile(9, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile CHEST = new BasicTile(10, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile SNOW = new BasicTile(11, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile ICE = new BasicTile(12, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile BRICK = new BasicTile(13, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);
    // public static final Tile DOOR = new BasicTile(14, 3, 0, Colours.get(-1, 100, 100, 100),
    // 0xFF666633, 1);

    protected byte id;
    protected boolean solid;
    protected boolean liquid;
    protected boolean emitter;
    private final int levelColour;
    private int scale;

    public Tile(int id, boolean isSolid, boolean isEmitter, boolean isLiquid, int levelColour, int scale) {
        this.id = (byte) id;
        if (tiles[id] != null) {
            throw new RuntimeException("Duplicate tile id on " + id);
        }
        this.solid = isSolid;
        this.liquid = isLiquid;
        this.emitter = isEmitter;
        this.levelColour = levelColour;
        this.scale = scale;
        tiles[id] = this;
    }

    public byte getId() {
        return id;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public boolean isEmitter() {
        return emitter;
    }

    public int getLevelColour() {
        return levelColour;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public abstract void tick();

    public abstract void render(Screen screen, Level level, int x, int y);
}
