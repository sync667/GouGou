package com.gmail.sync667.gougouserver.server.entities;

public abstract class Mob extends Entity {

    protected String name;
    protected static int speed;
    protected int numSteps = 0;
    protected boolean isMoving;
    protected int movingDir = 1;
    protected int scale = 1;

    public Mob(int entityId, String name, int x, int y, int speed) {
        super(entityId);
        this.name = name;
        this.x = x;
        this.y = y;
        Mob.speed = speed;
    }

    public void move(int xa, int ya) {

    }

    public abstract boolean hasCollided(int xa, int ya);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMovingDir() {
        return movingDir;
    }

    @Override
    public void setMovingDir(int movingDir) {
        this.movingDir = movingDir;
    }
}
