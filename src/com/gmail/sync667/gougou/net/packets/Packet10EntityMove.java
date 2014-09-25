package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet10EntityMove extends Packet {

    private int entityId;
    private int x;
    private int y;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet10EntityMove(byte[] data) {
        super(10);

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
    }

    /**
     * @param entityId
     * @param x
     * @param y
     */
    public Packet10EntityMove(int entityId, int x, int y) {
        super(10);

        this.entityId = entityId;
        this.x = x;
        this.y = y;

    }

    @Override
    public void writeData(GouGouClient client) {
        return;
    }

    @Override
    public byte[] getData() {
        return ("10" + this.entityId + "/" + x + "/" + y).getBytes();
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

}
