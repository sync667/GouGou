package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet00HandShakeClient extends Packet {

    private final String version;
    private short nextState;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet00HandShakeClient(byte[] data) {
        super(00);

        String[] dataArray = readData(data).split("/");
        this.version = dataArray[0];
        try {
            this.nextState = Short.valueOf(dataArray[1]);
        } catch (NumberFormatException e) {
            this.nextState = 0;
        }
    }

    /**
     * @param version
     *            of client
     */
    public Packet00HandShakeClient(String version, short nextState) {
        super(00);

        this.version = version;
        this.nextState = nextState;
    }

    @Override
    public void writeData(GouGouClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("00" + this.version + "/" + nextState).getBytes();
    }

}
