package com.gmail.sync667.gougouserver.server.entities;

import java.net.InetAddress;

public abstract class Entity {

    public int entityId;
    public int x, y;

    public Entity(int entityId) {
        init(entityId);
    }

    public final void init(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityId() {
        return entityId;
    }

    public abstract void tick();

    public abstract InetAddress getIpAddress();

    public abstract int getPort();

    public abstract int getSpeed();

    public abstract void setSpeed(int speed);

    public abstract String getName();

    public abstract int getMovingDir();

    public abstract void setMovingDir(int movingDir);

    public abstract String getPlayerName();
}
