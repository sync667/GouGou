package com.gmail.sync667.gougouserver.server.entities.player;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.server.entities.Mob;

public class Player extends Mob {

    protected boolean isSwimming = false;
    private final int tickCount = 0;
    public int scale = 1;
    private final String username;
    private final InetAddress ipAddress;
    private final int port;

    public Player(int entityId, int x, int y, String username, InetAddress ipAddress, int port) {
        super(entityId, "Gracz", x, y, speed);
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean hasCollided(int xa, int ya) {
        return false;

    }

    @Override
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(int speed) {
        Player.speed = speed;
    }

    @Override
    public String getPlayerName() {
        return username;
    }

}
