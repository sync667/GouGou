package com.gougou.core.world;

import com.badlogic.gdx.graphics.Color;

public enum TileType {
    VOID(0, false, false, Color.BLACK, "Void"),
    GRASS(1, true, false, new Color(0.2f, 0.7f, 0.2f, 1), "Grass"),
    DIRT(2, true, false, new Color(0.55f, 0.35f, 0.17f, 1), "Dirt"),
    STONE(3, true, false, Color.GRAY, "Stone"),
    WATER(4, false, true, new Color(0.1f, 0.3f, 0.8f, 1), "Water"),
    DEEP_WATER(5, false, true, new Color(0.05f, 0.15f, 0.6f, 1), "Deep Water"),
    SAND(6, true, false, new Color(0.9f, 0.85f, 0.6f, 1), "Sand"),
    SNOW(7, true, false, Color.WHITE, "Snow"),
    ICE(8, true, false, new Color(0.7f, 0.9f, 1.0f, 1), "Ice"),
    LAVA(9, false, false, new Color(1.0f, 0.3f, 0.0f, 1), "Lava"),
    TREE_OAK(10, false, false, new Color(0.1f, 0.5f, 0.1f, 1), "Oak Tree"),
    TREE_PINE(11, false, false, new Color(0.0f, 0.4f, 0.15f, 1), "Pine Tree"),
    TREE_DEAD(12, false, false, new Color(0.4f, 0.3f, 0.2f, 1), "Dead Tree"),
    BUSH(13, true, false, new Color(0.15f, 0.55f, 0.15f, 1), "Bush"),
    FLOWER_RED(14, true, false, new Color(0.9f, 0.2f, 0.2f, 1), "Red Flower"),
    FLOWER_YELLOW(15, true, false, new Color(0.9f, 0.9f, 0.2f, 1), "Yellow Flower"),
    ROCK(16, false, false, new Color(0.5f, 0.5f, 0.5f, 1), "Rock"),
    WALL_STONE(17, false, false, new Color(0.6f, 0.6f, 0.6f, 1), "Stone Wall"),
    WALL_WOOD(18, false, false, new Color(0.6f, 0.4f, 0.2f, 1), "Wood Wall"),
    PATH(19, true, false, new Color(0.7f, 0.6f, 0.4f, 1), "Path"),
    BRIDGE(20, true, false, new Color(0.5f, 0.35f, 0.15f, 1), "Bridge"),
    CHEST(21, false, false, new Color(0.7f, 0.55f, 0.1f, 1), "Chest"),
    DOOR(22, true, false, new Color(0.55f, 0.35f, 0.1f, 1), "Door"),
    CAMPFIRE(23, false, false, new Color(1.0f, 0.5f, 0.0f, 1), "Campfire"),
    MUSHROOM(24, true, false, new Color(0.8f, 0.2f, 0.2f, 1), "Mushroom"),
    TALL_GRASS(25, true, false, new Color(0.25f, 0.65f, 0.2f, 1), "Tall Grass"),
    SWAMP(26, true, true, new Color(0.25f, 0.4f, 0.2f, 1), "Swamp"),
    CLAY(27, true, false, new Color(0.7f, 0.5f, 0.3f, 1), "Clay"),
    GRAVEL(28, true, false, new Color(0.65f, 0.65f, 0.65f, 1), "Gravel");

    private final int id;
    private final boolean walkable;
    private final boolean liquid;
    private final Color color;
    private final String displayName;

    TileType(int id, boolean walkable, boolean liquid, Color color, String displayName) {
        this.id = id;
        this.walkable = walkable;
        this.liquid = liquid;
        this.color = color;
        this.displayName = displayName;
    }

    public int getId() { return id; }
    public boolean isWalkable() { return walkable; }
    public boolean isLiquid() { return liquid; }
    public Color getColor() { return color; }
    public String getDisplayName() { return displayName; }

    public static TileType fromId(int id) {
        for (TileType t : values()) {
            if (t.id == id) return t;
        }
        return VOID;
    }
}
