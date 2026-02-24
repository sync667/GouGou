package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.entities.*;
import com.gougou.core.input.InputManager;
import com.gougou.core.net.GameClient;
import com.gougou.core.net.GameServer;
import com.gougou.core.ui.*;
import com.gougou.core.world.*;

public class GameScreen implements Screen {
    private final GouGouGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private World world;
    private EntityManager entityManager;
    private Player localPlayer;
    private InputManager inputManager;

    private HUD hud;
    private ChatSystem chatSystem;
    private InventoryUI inventoryUI;
    private MiniMap miniMap;

    private GameClient client;
    private GameServer server;
    private boolean isMultiplayer;
    private float pingTimer = 0;
    private float moveUpdateTimer = 0;

    private static final float TILE_SIZE = 1.0f;
    private static final int WORLD_SIZE = 256;
    private static final float CAMERA_ZOOM = 0.5f;

    public GameScreen(GouGouGame game, String serverHost, int serverPort) {
        this.game = game;
        this.isMultiplayer = serverHost != null;

        if (isMultiplayer) {
            connectToServer(serverHost, serverPort);
        }
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.zoom = CAMERA_ZOOM;
        viewport = new FitViewport(40, 22.5f, camera);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        inputManager = new InputManager();
        entityManager = new EntityManager();

        // Generate world
        long seed = isMultiplayer ? 0 : System.currentTimeMillis();
        world = new World(WORLD_SIZE, WORLD_SIZE, seed);
        entityManager.setWorld(world);

        // Create local player
        String playerName = game.getProfileManager().getCurrentProfile().getDisplayName();
        localPlayer = entityManager.spawnPlayer(playerName, world.getSpawnX(), world.getSpawnY());
        localPlayer.setInput(inputManager);
        localPlayer.setLocalPlayer(true);
        localPlayer.setCharacterClass(game.getProfileManager().getCurrentProfile().getCharacterClass());
        localPlayer.setSkinColor(game.getProfileManager().getCurrentProfile().getSkinColor());

        // Give starting items
        localPlayer.addItem("Sword");
        localPlayer.addItem("Shield");
        localPlayer.addItem("Potion");

        // Spawn some mobs
        spawnMobs();

        // Initialize UI
        hud = new HUD();
        hud.setShowFps(game.getSettings().isShowFps());
        chatSystem = new ChatSystem();
        chatSystem.setSendListener(msg -> {
            chatSystem.addMessage(localPlayer.getName(), msg);
            if (client != null && client.isConnected()) {
                client.sendChat(msg);
            }
        });
        chatSystem.addSystemMessage("Welcome to GouGou! Use WASD to move.");
        chatSystem.addSystemMessage("Press T to chat, I for inventory, M for map.");

        inventoryUI = new InventoryUI();
        miniMap = new MiniMap();
        miniMap.generateTexture(world);

        // Input handling
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (chatSystem.isInputActive()) return false;
                if (inputManager.isChatToggle()) {
                    chatSystem.toggleInput();
                    return true;
                }
                if (inputManager.isInventoryToggle()) {
                    inventoryUI.toggle();
                    return true;
                }
                if (keycode == com.badlogic.gdx.Input.Keys.M) {
                    miniMap.toggle();
                    return true;
                }
                if (inputManager.isEscape()) {
                    if (inventoryUI.isVisible()) {
                        inventoryUI.toggle();
                    } else {
                        returnToMenu();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (chatSystem.isInputActive()) {
                    chatSystem.handleCharTyped(character);
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        if (!isMultiplayer) {
            hud.setConnectionStatus("Single Player");
        }
    }

    private void spawnMobs() {
        java.util.Random rand = new java.util.Random(world.getSeed());
        int mobCount = 30;
        for (int i = 0; i < mobCount; i++) {
            int mx = rand.nextInt(WORLD_SIZE);
            int my = rand.nextInt(WORLD_SIZE);
            if (world.isWalkable(mx, my)) {
                Mob.MobType type = Mob.MobType.values()[rand.nextInt(Mob.MobType.values().length)];
                entityManager.spawnMob(type, mx, my);
            }
        }
    }

    private void connectToServer(String host, int port) {
        client = new GameClient();
        client.addListener(new GameClient.ClientListener() {
            @Override
            public void onConnected(int entityId, float spawnX, float spawnY, long worldSeed) {
                hud.setConnectionStatus("Connected");
                chatSystem.addSystemMessage("Connected to server!");
            }

            @Override
            public void onDisconnected(String reason) {
                hud.setConnectionStatus("Disconnected");
                chatSystem.addSystemMessage("Disconnected: " + reason);
            }

            @Override
            public void onEntitySpawn(int entityId, String name, float x, float y, int type) {
                Player p = new Player(entityId, name, x, y);
                p.setWorld(world);
                entityManager.addEntity(p);
                chatSystem.addSystemMessage(name + " joined the game");
            }

            @Override
            public void onEntityDespawn(int entityId) {
                Entity e = entityManager.getEntity(entityId);
                if (e != null) {
                    chatSystem.addSystemMessage(e.getName() + " left the game");
                    entityManager.removeEntity(entityId);
                }
            }

            @Override
            public void onEntityMove(int entityId, float x, float y, int direction) {
                Entity e = entityManager.getEntity(entityId);
                if (e != null) {
                    e.setX(x);
                    e.setY(y);
                    e.setDirection(direction);
                }
            }

            @Override
            public void onChatMessage(int entityId, String message) {
                Entity e = entityManager.getEntity(entityId);
                String name = e != null ? e.getName() : "Unknown";
                chatSystem.addMessage(name, message);
            }

            @Override
            public void onPong(long latency) {
                hud.setLatency(latency);
            }
        });

        try {
            var profile = game.getProfileManager().getCurrentProfile();
            client.connect(host, port, profile.getDisplayName(),
                    profile.getCharacterClass(), profile.getSkinColor());
        } catch (Exception e) {
            chatSystem.addSystemMessage("Failed to connect: " + e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Camera follows player
        camera.position.set(localPlayer.getX(), localPlayer.getY(), 0);
        camera.update();

        // Render world tiles
        shapeRenderer.setProjectionMatrix(camera.combined);
        renderWorld();

        // Render entities
        renderEntities();

        // Render UI (screen space)
        batch.begin();
        hud.render(batch, localPlayer);
        chatSystem.render(batch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        inventoryUI.render(batch, localPlayer.getInventory());
        miniMap.render(batch, localPlayer, entityManager.getAllEntities());
        batch.end();
    }

    private void update(float delta) {
        if (!chatSystem.isInputActive() && !inventoryUI.isVisible()) {
            entityManager.update(delta);
        }

        // Network updates
        if (client != null && client.isConnected()) {
            moveUpdateTimer += delta;
            if (moveUpdateTimer >= 0.05f) { // 20 updates/sec
                moveUpdateTimer = 0;
                client.sendMoveInput(inputManager.isMoveUp(), inputManager.isMoveDown(),
                    inputManager.isMoveLeft(), inputManager.isMoveRight());
            }

            pingTimer += delta;
            if (pingTimer >= 2.0f) {
                pingTimer = 0;
                client.sendPing();
            }
        }
    }

    private void renderWorld() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        int viewW = (int) (viewport.getWorldWidth() / TILE_SIZE / camera.zoom) + 2;
        int viewH = (int) (viewport.getWorldHeight() / TILE_SIZE / camera.zoom) + 2;

        int startX = Math.max(0, (int) (camX - viewW / 2));
        int startY = Math.max(0, (int) (camY - viewH / 2));
        int endX = Math.min(world.getWidth(), startX + viewW);
        int endY = Math.min(world.getHeight(), startY + viewH);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                TileType tile = world.getTile(x, y);
                shapeRenderer.setColor(tile.getColor());
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        shapeRenderer.end();
    }

    private void renderEntities() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Entity entity : entityManager.getAllEntities()) {
            if (entity instanceof Player p) {
                // Player rendering
                Color playerColor = getPlayerColor(p);
                shapeRenderer.setColor(playerColor);

                // Body
                float bx = p.getX() * TILE_SIZE;
                float by = p.getY() * TILE_SIZE;
                shapeRenderer.rect(bx - 0.3f, by - 0.3f, 0.6f, 0.8f);

                // Head
                shapeRenderer.setColor(getSkinColor(p.getSkinColor()));
                shapeRenderer.circle(bx, by + 0.6f, 0.25f, 12);

                // Swimming effect
                if (p.isSwimming()) {
                    shapeRenderer.setColor(new Color(0.3f, 0.5f, 0.9f, 0.5f));
                    shapeRenderer.rect(bx - 0.4f, by - 0.3f, 0.8f, 0.3f);
                }

                // Name tag
            } else if (entity instanceof Mob mob) {
                Color mobColor = getMobColor(mob.getMobType());
                shapeRenderer.setColor(mobColor);
                float mx = mob.getX() * TILE_SIZE;
                float my = mob.getY() * TILE_SIZE;
                shapeRenderer.rect(mx - 0.25f, my - 0.25f, 0.5f, 0.5f);

                // Health bar for damaged mobs
                if (mob.getHealth() < mob.getMaxHealth()) {
                    shapeRenderer.setColor(Color.RED);
                    float hpRatio = (float) mob.getHealth() / mob.getMaxHealth();
                    shapeRenderer.rect(mx - 0.3f, my + 0.35f, 0.6f * hpRatio, 0.08f);
                }
            }
        }
        shapeRenderer.end();

        // Entity name tags
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Entity entity : entityManager.getAllEntities()) {
            if (entity instanceof Player p) {
                game.font.getData().setScale(0.02f);
                game.font.draw(batch, p.getName(),
                        p.getX() * TILE_SIZE - 0.3f, p.getY() * TILE_SIZE + 1.0f);
            }
        }
        game.font.getData().setScale(1.0f);
        batch.end();
        batch.setProjectionMatrix(batch.getProjectionMatrix().idt());
    }

    private Color getPlayerColor(Player p) {
        return switch (p.getCharacterClass()) {
            case 1 -> new Color(0.3f, 0.3f, 0.9f, 1); // Mage = blue
            case 2 -> new Color(0.2f, 0.7f, 0.2f, 1); // Ranger = green
            default -> new Color(0.8f, 0.2f, 0.2f, 1); // Warrior = red
        };
    }

    private Color getSkinColor(int index) {
        return switch (index) {
            case 1 -> new Color(0.87f, 0.72f, 0.53f, 1);
            case 2 -> new Color(0.76f, 0.57f, 0.38f, 1);
            case 3 -> new Color(0.55f, 0.38f, 0.26f, 1);
            case 4 -> new Color(0.4f, 0.26f, 0.18f, 1);
            case 5 -> new Color(0.3f, 0.2f, 0.15f, 1);
            default -> new Color(1.0f, 0.87f, 0.75f, 1);
        };
    }

    private Color getMobColor(Mob.MobType type) {
        return switch (type) {
            case SLIME -> new Color(0.2f, 0.9f, 0.3f, 0.8f);
            case SKELETON -> Color.LIGHT_GRAY;
            case WOLF -> new Color(0.5f, 0.4f, 0.3f, 1);
            case SPIDER -> new Color(0.3f, 0.1f, 0.1f, 1);
            case GOBLIN -> new Color(0.4f, 0.6f, 0.2f, 1);
        };
    }

    private void returnToMenu() {
        if (client != null) client.disconnect();
        if (server != null) server.stop();
        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        hud.dispose();
        chatSystem.dispose();
        inventoryUI.dispose();
        miniMap.dispose();
    }
}
