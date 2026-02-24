package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.net.GameClient;
import com.gougou.core.net.GameServer;
import com.gougou.core.net.Protocol;

public class ServerBrowserScreen implements Screen {
    private final GouGouGame game;
    private Stage stage;
    private Skin skin;
    private Label statusLabel;

    public ServerBrowserScreen(GouGouGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = createSkin();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Multiplayer", skin, "title");
        table.add(title).colspan(2).padBottom(20).row();

        // Direct connect section
        table.add(new Label("--- Direct Connect ---", skin)).colspan(2).padBottom(10).row();

        table.add(new Label("Server Address:", skin)).padRight(20);
        TextField addressField = new TextField("localhost", skin);
        table.add(addressField).width(250).row();

        table.add(new Label("Port:", skin)).padRight(20);
        TextField portField = new TextField(String.valueOf(Protocol.DEFAULT_PORT), skin);
        table.add(portField).width(250).row();

        TextButton connectButton = new TextButton("Connect", skin);
        connectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String host = addressField.getText().trim();
                int port;
                try {
                    port = Integer.parseInt(portField.getText().trim());
                } catch (NumberFormatException e) {
                    statusLabel.setText("Invalid port number");
                    return;
                }
                statusLabel.setText("Connecting to " + host + ":" + port + "...");
                game.setScreen(new GameScreen(game, host, port));
            }
        });
        table.add(connectButton).colspan(2).width(250).height(45).padTop(10).row();

        // Host server section
        table.add(new Label("", skin)).padTop(20).row();
        table.add(new Label("--- Host LAN Server ---", skin)).colspan(2).padBottom(10).row();

        table.add(new Label("Server Name:", skin)).padRight(20);
        TextField serverNameField = new TextField("My Server", skin);
        table.add(serverNameField).width(250).row();

        table.add(new Label("Max Players:", skin)).padRight(20);
        TextField maxPlayersField = new TextField("10", skin);
        table.add(maxPlayersField).width(250).row();

        TextButton hostButton = new TextButton("Host & Play", skin);
        hostButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int port = game.getSettings().getServerPort();
                String name = serverNameField.getText().trim();
                int maxPlayers;
                try {
                    maxPlayers = Integer.parseInt(maxPlayersField.getText().trim());
                } catch (NumberFormatException e) {
                    maxPlayers = 10;
                }

                statusLabel.setText("Starting server...");
                try {
                    GameServer server = new GameServer(port, name);
                    server.setMaxPlayers(maxPlayers);
                    server.start(System.currentTimeMillis(), 256, 256);
                    statusLabel.setText("Server started! Connecting...");
                    game.setScreen(new GameScreen(game, "localhost", port));
                } catch (Exception e) {
                    statusLabel.setText("Failed to start server: " + e.getMessage());
                }
            }
        });
        table.add(hostButton).colspan(2).width(250).height(45).padTop(10).row();

        // LAN Discovery
        table.add(new Label("", skin)).padTop(20).row();
        table.add(new Label("--- LAN Servers ---", skin)).colspan(2).padBottom(10).row();

        TextButton scanButton = new TextButton("Scan LAN", skin);
        Label lanResultsLabel = new Label("Press Scan to find servers", skin);
        scanButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                lanResultsLabel.setText("Scanning...");
                new Thread(() -> {
                    var servers = GameClient.discoverLANServers(3000);
                    Gdx.app.postRunnable(() -> {
                        if (servers.isEmpty()) {
                            lanResultsLabel.setText("No servers found");
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (String s : servers) sb.append(s).append("\n");
                            lanResultsLabel.setText(sb.toString());
                        }
                    });
                }).start();
            }
        });
        table.add(scanButton).colspan(2).width(250).height(40).row();
        table.add(lanResultsLabel).colspan(2).padTop(5).row();

        // Status
        statusLabel = new Label("", skin);
        statusLabel.setColor(Color.YELLOW);
        table.add(statusLabel).colspan(2).padTop(15).row();

        // Back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        table.add(backButton).colspan(2).width(250).height(45).padTop(20).row();
    }

    private Skin createSkin() {
        Skin skin = new Skin();
        BitmapFont defaultFont = new BitmapFont();
        defaultFont.getData().setScale(1.2f);
        skin.add("default-font", defaultFont);
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(2.5f);
        skin.add("title-font", titleFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        skin.add("default", new Label.LabelStyle(defaultFont, Color.WHITE));
        skin.add("title", new Label.LabelStyle(titleFont, new Color(0.2f, 0.8f, 0.4f, 1)));

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = defaultFont;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.GREEN;
        btnStyle.up = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 0.9f));
        btnStyle.over = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.45f, 0.9f));
        btnStyle.down = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.9f));
        skin.add("default", btnStyle);

        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle();
        tfStyle.font = defaultFont;
        tfStyle.fontColor = Color.WHITE;
        tfStyle.background = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.9f));
        tfStyle.cursor = skin.newDrawable("white", Color.WHITE);
        tfStyle.selection = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.6f, 0.5f));
        skin.add("default", tfStyle);

        return skin;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
