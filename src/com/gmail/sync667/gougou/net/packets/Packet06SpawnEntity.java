package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet06SpawnEntity extends Packet {

    private int entityId;
    private int x;
    private int y;
    private final String name;
    private final String username;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet06SpawnEntity(byte[] data) {
        super(06);

        String[] dataArray = readData(data).split("/");

        try {
            this.entityId = Integer.valueOf(dataArray[0]);
            this.x = Integer.valueOf(dataArray[1]);
            this.y = Integer.valueOf(dataArray[2]);
        } catch (NumberFormatException e) {
            this.entityId = 0;
            this.x = 0;
            this.y = 0;
        }
        this.name = dataArray[3];
        this.username = dataArray[4];

    }

    /*
     * Client can not send server side packet!
     */
    @Override
    public void writeData(GouGouClient client) {
        return;
    }

    /*
     * Client can not parse packet data!
     */
    @Override
    public byte[] getData() {
        return null;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
}
