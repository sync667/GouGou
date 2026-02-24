package com.gougou.core.entities;

import com.gougou.core.world.World;
import com.gougou.core.input.InputManager;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    private int mana;
    private int maxMana;
    private int experience;
    private int level;
    private boolean swimming;
    private int characterClass; // 0=warrior, 1=mage, 2=ranger
    private int skinColor; // index
    private final List<String> inventory;
    private World world;
    private InputManager input;
    private boolean isLocalPlayer;
    private float animTimer;
    private int animFrame;
    private boolean moving;

    public Player(int entityId, String name, float x, float y) {
        super(entityId, name, x, y, 100);
        this.maxMana = 50;
        this.mana = maxMana;
        this.experience = 0;
        this.level = 1;
        this.characterClass = 0;
        this.skinColor = 0;
        this.inventory = new ArrayList<>();
        this.speed = 4.0f;
    }

    public void setWorld(World world) { this.world = world; }
    public void setInput(InputManager input) { this.input = input; }
    public void setLocalPlayer(boolean local) { this.isLocalPlayer = local; }

    @Override
    public void update(float delta) {
        if (!alive || !isLocalPlayer || input == null) return;

        float dx = 0, dy = 0;
        if (input.isMoveUp()) { dy += speed * delta; direction = 1; }
        if (input.isMoveDown()) { dy -= speed * delta; direction = 0; }
        if (input.isMoveLeft()) { dx -= speed * delta; direction = 2; }
        if (input.isMoveRight()) { dx += speed * delta; direction = 3; }

        moving = dx != 0 || dy != 0;

        if (moving) {
            animTimer += delta;
            if (animTimer >= 0.15f) {
                animTimer = 0;
                animFrame = (animFrame + 1) % 4;
            }
        } else {
            animFrame = 0;
            animTimer = 0;
        }

        // Collision with world
        if (world != null) {
            float newX = x + dx;
            float newY = y + dy;
            if (world.isWalkable(Math.round(newX), Math.round(newY)) ||
                world.isLiquid(Math.round(newX), Math.round(newY))) {
                x = newX;
                y = newY;
            } else {
                // Try sliding along walls
                if (world.isWalkable(Math.round(newX), Math.round(y)) ||
                    world.isLiquid(Math.round(newX), Math.round(y))) {
                    x = newX;
                } else if (world.isWalkable(Math.round(x), Math.round(newY)) ||
                           world.isLiquid(Math.round(x), Math.round(newY))) {
                    y = newY;
                }
            }
            swimming = world.isLiquid(Math.round(x), Math.round(y));
        } else {
            x += dx;
            y += dy;
            swimming = false;
        }

        if (swimming) {
            speed = 2.0f;
        } else {
            speed = 4.0f;
        }
    }

    public void addExperience(int amount) {
        experience += amount;
        while (experience >= getExpForNextLevel()) {
            experience -= getExpForNextLevel();
            level++;
            maxHealth += 10;
            health = maxHealth;
            maxMana += 5;
            mana = maxMana;
        }
    }

    public int getExpForNextLevel() {
        return level * 100;
    }

    public void useMana(int amount) {
        mana = Math.max(0, mana - amount);
    }

    public void restoreMana(int amount) {
        mana = Math.min(maxMana, mana + amount);
    }

    // Inventory
    public void addItem(String item) { inventory.add(item); }
    public void removeItem(String item) { inventory.remove(item); }
    public List<String> getInventory() { return inventory; }
    public boolean hasItem(String item) { return inventory.contains(item); }

    // Getters/Setters
    public int getMana() { return mana; }
    public void setMana(int m) { this.mana = m; }
    public int getMaxMana() { return maxMana; }
    public int getExperience() { return experience; }
    public int getLevel() { return level; }
    public boolean isSwimming() { return swimming; }
    public int getCharacterClass() { return characterClass; }
    public void setCharacterClass(int c) { this.characterClass = c; }
    public int getSkinColor() { return skinColor; }
    public void setSkinColor(int c) { this.skinColor = c; }
    public boolean isMoving() { return moving; }
    public int getAnimFrame() { return animFrame; }
    public boolean isLocalPlayer() { return isLocalPlayer; }
}
