package com.gougou.core.entities;

import com.gougou.core.world.World;
import java.util.Random;

public class Mob extends Entity {
    public enum MobType { SLIME, SKELETON, WOLF, SPIDER, GOBLIN }

    private MobType mobType;
    private float wanderTimer;
    private float wanderInterval;
    private int wanderDirX, wanderDirY;
    private World world;
    private float aggroRange;
    private boolean hostile;
    private final Random random;

    public Mob(int entityId, MobType type, float x, float y) {
        super(entityId, type.name(), x, y, getMaxHealthForType(type));
        this.mobType = type;
        this.hostile = type != MobType.WOLF;
        this.aggroRange = type == MobType.GOBLIN ? 8 : 5;
        this.speed = getSpeedForType(type);
        this.random = new Random(entityId);
        this.wanderInterval = 2 + random.nextFloat() * 3;
    }

    private static int getMaxHealthForType(MobType type) {
        return switch (type) {
            case SLIME -> 20;
            case SKELETON -> 40;
            case WOLF -> 30;
            case SPIDER -> 25;
            case GOBLIN -> 35;
        };
    }

    private static float getSpeedForType(MobType type) {
        return switch (type) {
            case SLIME -> 1.0f;
            case SKELETON -> 1.5f;
            case WOLF -> 3.0f;
            case SPIDER -> 2.5f;
            case GOBLIN -> 2.0f;
        };
    }

    public void setWorld(World world) { this.world = world; }

    @Override
    public void update(float delta) {
        if (!alive) return;

        wanderTimer += delta;
        if (wanderTimer >= wanderInterval) {
            wanderTimer = 0;
            wanderDirX = random.nextInt(3) - 1;
            wanderDirY = random.nextInt(3) - 1;
            wanderInterval = 2 + random.nextFloat() * 3;
        }

        float dx = wanderDirX * speed * delta * 0.3f;
        float dy = wanderDirY * speed * delta * 0.3f;

        if (world != null) {
            float newX = x + dx;
            float newY = y + dy;
            if (world.isWalkable(Math.round(newX), Math.round(newY))) {
                x = newX;
                y = newY;
            }
        } else {
            x += dx;
            y += dy;
        }

        if (dx > 0) direction = 3;
        else if (dx < 0) direction = 2;
        else if (dy > 0) direction = 1;
        else if (dy < 0) direction = 0;
    }

    public MobType getMobType() { return mobType; }
    public boolean isHostile() { return hostile; }
    public float getAggroRange() { return aggroRange; }
}
