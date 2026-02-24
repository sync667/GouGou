package com.gougou.core.entities;

public abstract class Entity {
    protected float x, y;
    protected int direction; // 0=down, 1=up, 2=left, 3=right
    protected int entityId;
    protected String name;
    protected int health;
    protected int maxHealth;
    protected boolean alive = true;
    protected float speed = 2.0f;

    public Entity(int entityId, String name, float x, float y, int maxHealth) {
        this.entityId = entityId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public abstract void update(float delta);

    public void damage(int amount) {
        health = Math.max(0, health - amount);
        if (health <= 0) alive = false;
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public int getDirection() { return direction; }
    public void setDirection(int d) { this.direction = d; }
    public int getEntityId() { return entityId; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public void setHealth(int h) { this.health = h; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean a) { this.alive = a; }
    public float getSpeed() { return speed; }
    public void setSpeed(float s) { this.speed = s; }
}
