package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet04Disconnect extends Packet {

    private int entityId;
    private String msg;

    public Packet04Disconnect(byte[] data) {
        super(04);

        String[] dataArray = readData(data).split("/");
        try {
            this.entityId = Integer.valueOf(dataArray[0]);
            this.msg = dataArray[1];
        } catch (NumberFormatException e) {
            this.entityId = 0;
            this.msg = null;
        }
    }

    /**
     * @param senderIp
     * @param port
     * @param entityId
     * @param msg
     */
    public Packet04Disconnect(int entityId, String msg) {
        super(04);

        this.entityId = entityId;
        this.msg = msg;
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

    public String getMsg() {
        return msg;
    }

}
