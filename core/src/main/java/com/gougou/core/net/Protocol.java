package com.gougou.core.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class Protocol {
    public static final int VERSION = 3;
    public static final int DEFAULT_PORT = 7777;
    public static final int MAX_PACKET_SIZE = 1024;
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

    public static final int MAX_USERNAME_LENGTH = 32;
    public static final int MAX_CHAT_LENGTH = 512;
    public static final int MAX_NAME_LENGTH = 64;

    private Protocol() {}

    public static int clampStringLength(byte[] bytes, int maxLen) {
        return Math.min(bytes.length, maxLen);
    }

    public static int validateReadLength(int len, int remaining, int maxLen) {
        if (len < 0 || len > remaining || len > maxLen) return -1;
        return len;
    }

    public static byte[] createHandshake() {
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.put(HANDSHAKE);
        buf.putInt(VERSION);
        return buf.array();
    }

    public static byte[] createHandshakeAck(boolean accepted, String message) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(2 + msgBytes.length);
        buf.put(HANDSHAKE_ACK);
        buf.put((byte) (accepted ? 1 : 0));
        buf.put(msgBytes);
        return buf.array();
    }

    public static byte[] createLogin(String username, int characterClass, int skinColor) {
        byte[] nameBytes = username.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(1 + 4 + nameBytes.length + 4 + 4);
        buf.put(LOGIN);
        buf.putInt(nameBytes.length);
        buf.put(nameBytes);
        buf.putInt(characterClass);
        buf.putInt(skinColor);
        return buf.array();
    }

    public static byte[] createLoginAck(int entityId, float spawnX, float spawnY, long worldSeed) {
        ByteBuffer buf = ByteBuffer.allocate(1 + 4 + 4 + 4 + 8);
        buf.put(LOGIN_ACK);
        buf.putInt(entityId);
        buf.putFloat(spawnX);
        buf.putFloat(spawnY);
        buf.putLong(worldSeed);
        return buf.array();
    }

    public static byte[] createDisconnect(int entityId) {
        ByteBuffer buf = ByteBuffer.allocate(5);
        buf.put(DISCONNECT);
        buf.putInt(entityId);
        return buf.array();
    }

    public static byte[] createSpawn(int entityId, String name, float x, float y, int type) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(1 + 4 + 4 + nameBytes.length + 4 + 4 + 4);
        buf.put(SPAWN);
        buf.putInt(entityId);
        buf.putInt(nameBytes.length);
        buf.put(nameBytes);
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putInt(type);
        return buf.array();
    }

    public static byte[] createMove(int entityId, float x, float y, int direction) {
        ByteBuffer buf = ByteBuffer.allocate(1 + 4 + 4 + 4 + 4);
        buf.put(MOVE);
        buf.putInt(entityId);
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putInt(direction);
        return buf.array();
    }

    public static byte[] createChat(int entityId, String message) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(1 + 4 + 4 + msgBytes.length);
        buf.put(CHAT);
        buf.putInt(entityId);
        buf.putInt(msgBytes.length);
        buf.put(msgBytes);
        return buf.array();
    }

    public static byte[] createPing(long timestamp) {
        ByteBuffer buf = ByteBuffer.allocate(9);
        buf.put(PING);
        buf.putLong(timestamp);
        return buf.array();
    }

    public static byte[] createPong(long timestamp) {
        ByteBuffer buf = ByteBuffer.allocate(9);
        buf.put(PONG);
        buf.putLong(timestamp);
        return buf.array();
    }
}
