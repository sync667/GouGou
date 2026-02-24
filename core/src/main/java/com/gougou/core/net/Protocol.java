package com.gougou.core.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;

public final class Protocol {
    public static final int VERSION = 4;
    public static final int DEFAULT_PORT = 7777;
    public static final int MAX_PACKET_SIZE = 4096;
    public static final int DISCOVERY_PORT = 7778;
    public static final byte[] DISCOVERY_REQUEST = "GOUGOU_DISCOVER".getBytes(StandardCharsets.UTF_8);
    public static final byte[] DISCOVERY_RESPONSE_PREFIX = "GOUGOU_SERVER:".getBytes(StandardCharsets.UTF_8);

    // Packet types
    public static final byte HANDSHAKE = 0x01;
    public static final byte HANDSHAKE_ACK = 0x02;
    public static final byte LOGIN = 0x03;
    public static final byte LOGIN_ACK = 0x04;
    public static final byte DISCONNECT = 0x05;
    public static final byte SPAWN = 0x06;
    public static final byte DESPAWN = 0x07;
    public static final byte MOVE = 0x08;
    public static final byte CHAT = 0x09;
    public static final byte PING = 0x0A;
    public static final byte PONG = 0x0B;
    public static final byte WORLD_DATA = 0x0C;
    public static final byte ENTITY_UPDATE = 0x0D;
    public static final byte PLAYER_ACTION = 0x0E;
    public static final byte PLAYER_STATE = 0x0F;
    public static final byte MOVE_INPUT = 0x10;
    public static final byte INVENTORY_UPDATE = 0x11;

    public static final int MAX_USERNAME_LENGTH = 32;
    public static final int MAX_CHAT_LENGTH = 512;
    public static final int MAX_NAME_LENGTH = 64;
    public static final int MAX_ITEM_NAME_LENGTH = 128;

    private Protocol() {}

    public static int clampStringLength(byte[] bytes, int maxLen) {
        return Math.min(bytes.length, maxLen);
    }

    public static int validateReadLength(int len, int remaining, int maxLen) {
        if (len < 0 || len > remaining || len > maxLen) return -1;
        return len;
    }

    // --- Packet creation methods ---

    public static byte[] createHandshake() {
        ByteBuf buf = Unpooled.buffer(5);
        buf.writeByte(HANDSHAKE);
        buf.writeInt(VERSION);
        return toBytes(buf);
    }

    public static byte[] createHandshakeAck(boolean accepted, String message) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.buffer(2 + msgBytes.length);
        buf.writeByte(HANDSHAKE_ACK);
        buf.writeBoolean(accepted);
        buf.writeBytes(msgBytes);
        return toBytes(buf);
    }

    public static byte[] createLogin(String username, int characterClass, int skinColor) {
        byte[] nameBytes = username.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(nameBytes.length, MAX_USERNAME_LENGTH);
        ByteBuf buf = Unpooled.buffer(1 + 4 + len + 4 + 4);
        buf.writeByte(LOGIN);
        buf.writeInt(len);
        buf.writeBytes(nameBytes, 0, len);
        buf.writeInt(characterClass);
        buf.writeInt(skinColor);
        return toBytes(buf);
    }

    public static byte[] createLoginAck(int entityId, float spawnX, float spawnY, long worldSeed) {
        ByteBuf buf = Unpooled.buffer(1 + 4 + 4 + 4 + 8);
        buf.writeByte(LOGIN_ACK);
        buf.writeInt(entityId);
        buf.writeFloat(spawnX);
        buf.writeFloat(spawnY);
        buf.writeLong(worldSeed);
        return toBytes(buf);
    }

    public static byte[] createDisconnect(int entityId) {
        ByteBuf buf = Unpooled.buffer(5);
        buf.writeByte(DISCONNECT);
        buf.writeInt(entityId);
        return toBytes(buf);
    }

    public static byte[] createSpawn(int entityId, String name, float x, float y, int type) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(nameBytes.length, MAX_NAME_LENGTH);
        ByteBuf buf = Unpooled.buffer(1 + 4 + 4 + len + 4 + 4 + 4);
        buf.writeByte(SPAWN);
        buf.writeInt(entityId);
        buf.writeInt(len);
        buf.writeBytes(nameBytes, 0, len);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeInt(type);
        return toBytes(buf);
    }

    public static byte[] createDespawn(int entityId) {
        ByteBuf buf = Unpooled.buffer(5);
        buf.writeByte(DESPAWN);
        buf.writeInt(entityId);
        return toBytes(buf);
    }

    public static byte[] createMove(int entityId, float x, float y, int direction) {
        ByteBuf buf = Unpooled.buffer(1 + 4 + 4 + 4 + 4);
        buf.writeByte(MOVE);
        buf.writeInt(entityId);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeInt(direction);
        return toBytes(buf);
    }

    public static byte[] createMoveInput(boolean up, boolean down, boolean left, boolean right) {
        ByteBuf buf = Unpooled.buffer(2);
        buf.writeByte(MOVE_INPUT);
        int flags = 0;
        if (up) flags |= 1;
        if (down) flags |= 2;
        if (left) flags |= 4;
        if (right) flags |= 8;
        buf.writeByte(flags);
        return toBytes(buf);
    }

    public static byte[] createChat(int entityId, String message) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(msgBytes.length, MAX_CHAT_LENGTH);
        ByteBuf buf = Unpooled.buffer(1 + 4 + 4 + len);
        buf.writeByte(CHAT);
        buf.writeInt(entityId);
        buf.writeInt(len);
        buf.writeBytes(msgBytes, 0, len);
        return toBytes(buf);
    }

    public static byte[] createPing(long timestamp) {
        ByteBuf buf = Unpooled.buffer(9);
        buf.writeByte(PING);
        buf.writeLong(timestamp);
        return toBytes(buf);
    }

    public static byte[] createPong(long timestamp) {
        ByteBuf buf = Unpooled.buffer(9);
        buf.writeByte(PONG);
        buf.writeLong(timestamp);
        return toBytes(buf);
    }

    public static byte[] createPlayerState(int entityId, int health, int maxHealth,
                                            int mana, int maxMana, int level,
                                            float x, float y, int direction,
                                            boolean swimming) {
        ByteBuf buf = Unpooled.buffer(1 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 1);
        buf.writeByte(PLAYER_STATE);
        buf.writeInt(entityId);
        buf.writeInt(health);
        buf.writeInt(maxHealth);
        buf.writeInt(mana);
        buf.writeInt(maxMana);
        buf.writeInt(level);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeInt(direction);
        buf.writeBoolean(swimming);
        return toBytes(buf);
    }

    public static byte[] createInventoryUpdate(int entityId, String[] items) {
        if (items == null) {
            items = new String[0];
        }
        ByteBuf buf = Unpooled.buffer(1 + 4 + 4 + items.length * 8);
        buf.writeByte(INVENTORY_UPDATE);
        buf.writeInt(entityId);
        buf.writeInt(items.length);
        for (String item : items) {
            byte[] itemBytes = (item != null ? item : "").getBytes(StandardCharsets.UTF_8);
            int len = Math.min(itemBytes.length, MAX_ITEM_NAME_LENGTH);
            buf.writeInt(len);
            buf.writeBytes(itemBytes, 0, len);
        }
        return toBytes(buf);
    }

    private static byte[] toBytes(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        buf.release();
        return data;
    }
}
