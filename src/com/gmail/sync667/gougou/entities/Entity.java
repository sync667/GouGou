package com.gmail.sync667.gougou.entities;

import com.gmail.sync667.gougou.gfx.Screen;
import com.gmail.sync667.gougou.level.Level;

public abstract class Entity {

    public int entityId;
    public int x, y;
    protected Level level;

    public Entity(int entityId, Level level) {
        init(entityId, level);
    }

    public final void init(int entityId, Level level) {
        this.entityId = entityId;
        this.level = level;
    }

    public abstract void tick();

    public abstract void render(Screen screen);

    public int getEntityId() {
        return entityId;
    }
}
