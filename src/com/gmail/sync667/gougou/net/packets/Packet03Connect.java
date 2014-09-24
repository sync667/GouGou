package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet03Connect extends Packet {

    private int entityId;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet03Connect(byte[] data) {
        super(03);

        String[] dataArray = readData(data).split("/");

        try {
            this.entityId = Integer.valueOf(dataArray[0]);
        } catch (NumberFormatException e) {
            this.entityId = 0;
        }
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

}
