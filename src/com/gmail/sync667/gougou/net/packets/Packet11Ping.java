package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet11Ping extends Packet {

    private int entityId;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet11Ping(byte[] data) {
        super(11);

        try {
            this.entityId = Integer.valueOf(readData(data));
        } catch (NumberFormatException e) {
            this.entityId = 0;
        }
    }

    public Packet11Ping(int entityId) {
        super(11);

        this.entityId = entityId;

    }

    @Override
    public void writeData(GouGouClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("11" + this.entityId).getBytes();
    }

    public int getEntityId() {
        return entityId;
    }

}
