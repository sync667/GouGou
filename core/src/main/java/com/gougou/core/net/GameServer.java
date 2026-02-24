package com.gougou.core.net;

import com.gougou.core.world.World;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private DatagramSocket discoverySocket;
    private int port;
    private String serverName;
    private volatile boolean running;
    private World world;
    private final Map<Channel, ServerPlayerData> players = new ConcurrentHashMap<>();
    private int nextEntityId = 1;
    private int maxPlayers = 10;
    private Timer tickTimer;

    public static class ServerPlayerData {
        public final Channel channel;
        public final int entityId;
        public String username;
        public int characterClass;
        public int skinColor;
        public float x, y;
        public int direction;
        public int health, maxHealth;
        public int mana, maxMana;
        public int level;
        public int experience;
        public boolean swimming;
        public boolean moveUp, moveDown, moveLeft, moveRight;
        public final List<String> inventory = new ArrayList<>();
        public long lastActivity;

        public ServerPlayerData(Channel channel, int entityId, String username) {
            this.channel = channel;
            this.entityId = entityId;
            this.username = username;
            this.health = 100;
            this.maxHealth = 100;
            this.mana = 50;
            this.maxMana = 50;
            this.level = 1;
            this.experience = 0;
            this.lastActivity = System.currentTimeMillis();
            this.inventory.add("Sword");
            this.inventory.add("Shield");
            this.inventory.add("Potion");
        }
    }

    public GameServer(int port, String serverName) {
        this.port = port;
        this.serverName = serverName;
    }

    public void start(long worldSeed, int worldWidth, int worldHeight) throws IOException {
        world = new World(worldWidth, worldHeight, worldSeed);
        running = true;

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                            new LengthFieldBasedFrameDecoder(Protocol.MAX_PACKET_SIZE, 0, 4, 0, 4),
                            new LengthFieldPrepender(4),
                            new IdleStateHandler(60, 0, 0),
                            new ServerHandler()
                        );
                    }
                });

            serverChannel = bootstrap.bind(port).sync().channel();
            System.out.println("Game server started on port " + port);

            // Start LAN discovery responder
            Thread discoveryThread = new Thread(this::discoveryLoop, "GouGou-Server-Discovery");
            discoveryThread.setDaemon(true);
            discoveryThread.start();

            // Start game tick (20 ticks/sec)
            tickTimer = new Timer("GouGou-Server-Tick", true);
            tickTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (running) tick();
                }
            }, 50, 50);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Server start interrupted", e);
        }
    }

    private void tick() {
        float delta = 0.05f; // 50ms per tick
        float speed = 4.0f;

        for (ServerPlayerData player : players.values()) {
            float dx = 0, dy = 0;
            if (player.moveUp) { dy += speed * delta; player.direction = 1; }
            if (player.moveDown) { dy -= speed * delta; player.direction = 0; }
            if (player.moveLeft) { dx -= speed * delta; player.direction = 2; }
            if (player.moveRight) { dx += speed * delta; player.direction = 3; }

            if (dx != 0 || dy != 0) {
                float newX = player.x + dx;
                float newY = player.y + dy;

                // Swimming speed reduction
                if (player.swimming) {
                    newX = player.x + dx * 0.5f;
                    newY = player.y + dy * 0.5f;
                }

                // Server-side collision detection
                if (world.isWalkable(Math.round(newX), Math.round(newY)) ||
                    world.isLiquid(Math.round(newX), Math.round(newY))) {
                    player.x = newX;
                    player.y = newY;
                } else {
                    // Wall sliding
                    if (world.isWalkable(Math.round(newX), Math.round(player.y)) ||
                        world.isLiquid(Math.round(newX), Math.round(player.y))) {
                        player.x = newX;
                    } else if (world.isWalkable(Math.round(player.x), Math.round(newY)) ||
                               world.isLiquid(Math.round(player.x), Math.round(newY))) {
                        player.y = newY;
                    }
                }

                player.swimming = world.isLiquid(Math.round(player.x), Math.round(player.y));

                // Broadcast position to other players
                byte[] movePacket = Protocol.createMove(player.entityId, player.x, player.y, player.direction);
                broadcastExcept(player.channel, movePacket);
            }

            // Send authoritative state to the player
            sendTo(player.channel, Protocol.createPlayerState(
                player.entityId, player.health, player.maxHealth,
                player.mana, player.maxMana, player.level,
                player.x, player.y, player.direction, player.swimming
            ));
        }
    }

    private void discoveryLoop() {
        try {
            discoverySocket = new DatagramSocket(Protocol.DISCOVERY_PORT);
            byte[] buf = new byte[256];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    discoverySocket.receive(packet);
                    String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (request.equals("GOUGOU_DISCOVER")) {
                        String response = "GOUGOU_SERVER:" + serverName + "|" + players.size() + "/" + maxPlayers + "|" + port;
                        byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                            packet.getAddress(), packet.getPort());
                        discoverySocket.send(responsePacket);
                    }
                } catch (IOException e) {
                    if (running) System.err.println("Discovery error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start discovery listener: " + e.getMessage());
        }
    }

    private class ServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            try {
                if (in.readableBytes() < 1) return;
                byte type = in.readByte();
                Channel ch = ctx.channel();

                switch (type) {
                    case Protocol.HANDSHAKE -> {
                        int version = in.readInt();
                        boolean accepted = version == Protocol.VERSION;
                        String message = accepted ? "Welcome to " + serverName : "Version mismatch (expected v" + Protocol.VERSION + ")";
                        sendTo(ch, Protocol.createHandshakeAck(accepted, message));
                        if (!accepted) {
                            ch.close();
                        }
                    }
                    case Protocol.LOGIN -> {
                        if (players.size() >= maxPlayers) {
                            sendTo(ch, Protocol.createHandshakeAck(false, "Server full"));
                            ch.close();
                            return;
                        }

                        int nameLen = Protocol.validateReadLength(in.readInt(), in.readableBytes(), Protocol.MAX_USERNAME_LENGTH);
                        if (nameLen < 0) return;
                        byte[] nameBytes = new byte[nameLen];
                        in.readBytes(nameBytes);
                        String username = new String(nameBytes, StandardCharsets.UTF_8);
                        int charClass = in.readInt();
                        int skinColor = in.readInt();

                        int eid = nextEntityId++;
                        ServerPlayerData player = new ServerPlayerData(ch, eid, username);
                        player.characterClass = charClass;
                        player.skinColor = skinColor;
                        player.x = world.getSpawnX();
                        player.y = world.getSpawnY();
                        players.put(ch, player);

                        // Send login ack
                        sendTo(ch, Protocol.createLoginAck(eid, player.x, player.y, world.getSeed()));

                        // Send existing players to the new player
                        for (ServerPlayerData existing : players.values()) {
                            if (existing.channel != ch) {
                                sendTo(ch, Protocol.createSpawn(existing.entityId, existing.username, existing.x, existing.y, 0));
                            }
                        }

                        // Notify existing players about new player
                        byte[] spawnPacket = Protocol.createSpawn(eid, username, player.x, player.y, 0);
                        broadcastExcept(ch, spawnPacket);

                        // Send initial inventory
                        sendTo(ch, Protocol.createInventoryUpdate(eid, player.inventory.toArray(new String[0])));

                        System.out.println(username + " joined (ID: " + eid + ")");
                    }
                    case Protocol.MOVE_INPUT -> {
                        ServerPlayerData player = players.get(ch);
                        if (player != null) {
                            player.lastActivity = System.currentTimeMillis();
                            int flags = in.readByte();
                            player.moveUp = (flags & 1) != 0;
                            player.moveDown = (flags & 2) != 0;
                            player.moveLeft = (flags & 4) != 0;
                            player.moveRight = (flags & 8) != 0;
                        }
                    }
                    case Protocol.CHAT -> {
                        ServerPlayerData player = players.get(ch);
                        if (player != null) {
                            in.readInt(); // skip entityId from client (use server's)
                            int msgLen = Protocol.validateReadLength(in.readInt(), in.readableBytes(), Protocol.MAX_CHAT_LENGTH);
                            if (msgLen < 0) return;
                            byte[] chatBytes = new byte[msgLen];
                            in.readBytes(chatBytes);
                            String chatMsg = new String(chatBytes, StandardCharsets.UTF_8);
                            // Broadcast with server-verified entity ID
                            broadcast(Protocol.createChat(player.entityId, chatMsg));
                            System.out.println("[Chat] " + player.username + ": " + chatMsg);
                        }
                    }
                    case Protocol.PING -> {
                        long timestamp = in.readLong();
                        sendTo(ch, Protocol.createPong(timestamp));
                    }
                    case Protocol.DISCONNECT -> {
                        handleDisconnect(ch);
                    }
                }
            } finally {
                in.release();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            handleDisconnect(ctx.channel());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                System.out.println("Closing idle connection: " + ctx.channel().remoteAddress());
                ctx.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("Server handler error: " + cause.getMessage());
            ctx.close();
        }
    }

    private void handleDisconnect(Channel ch) {
        ServerPlayerData player = players.remove(ch);
        if (player != null) {
            broadcastExcept(ch, Protocol.createDespawn(player.entityId));
            System.out.println(player.username + " left the server");
        }
    }

    private void sendTo(Channel channel, byte[] data) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.wrappedBuffer(data));
        }
    }

    private void broadcast(byte[] data) {
        for (ServerPlayerData player : players.values()) {
            sendTo(player.channel, data);
        }
    }

    private void broadcastExcept(Channel exclude, byte[] data) {
        for (ServerPlayerData player : players.values()) {
            if (player.channel != exclude) {
                sendTo(player.channel, data);
            }
        }
    }

    public void stop() {
        running = false;
        if (tickTimer != null) tickTimer.cancel();
        broadcast(Protocol.createDisconnect(0));
        if (serverChannel != null) serverChannel.close();
        if (discoverySocket != null) discoverySocket.close();
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        System.out.println("Server stopped");
    }

    public boolean isRunning() { return running; }
    public int getPlayerCount() { return players.size(); }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int max) { this.maxPlayers = max; }
    public World getWorld() { return world; }
    public int getPort() { return port; }
    public Collection<ServerPlayerData> getPlayers() { return players.values(); }
}
