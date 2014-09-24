package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet00HandShake extends Packet {

    private final String version;
    private int players = 0;
    private int slots = 0;
    private final String motd;

    public Packet00HandShake(byte[] data) {
        super(00);

        String[] dataArray = readData(data).split("/");
        this.version = dataArray[0];
        try {
            this.players = Integer.valueOf(dataArray[1]);
            this.slots = Integer.valueOf(dataArray[2]);
        } catch (NumberFormatException e) {
            this.players = 0;
            this.slots = 0;
        }
        this.motd = dataArray[3];
    }

    public Packet00HandShake(String version, int players, int slots, String motd) {
        super(00);

        this.version = version;
        this.players = players;
        this.slots = slots;
        this.motd = motd;
    }

    @Override
    public void writeData(GouGouClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("00" + this.version + "/" + this.players + "/" + this.slots + "/" + this.motd).getBytes();
    }

}
