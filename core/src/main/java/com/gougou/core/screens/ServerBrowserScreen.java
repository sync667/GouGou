package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.net.GameClient;
import com.gougou.core.net.GameServer;
import com.gougou.core.net.Protocol;
import com.gougou.core.ui.OldSchoolSkin;
import com.gougou.core.ui.ScreenBackground;

public class ServerBrowserScreen implements Screen {

    private final GouGouGame game;
    private Stage stage;
    private OldSchoolSkin skin;
    private ScreenBackground background;
    private Label statusLabel;

    public ServerBrowserScreen(GouGouGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage      = new Stage(new ScreenViewport());
        skin       = new OldSchoolSkin();
        background = new ScreenBackground();
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table panel = new Table();
        panel.setBackground(skin.panelDrawable());
        panel.pad(30, 50, 30, 50);

        panel.add(new Label("Multiplayer", skin, "subtitle")).colspan(2).padBottom(4).row();
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(2).padBottom(20).row();

        // ── Direct Connect ────────────────────────────────────────────────
        panel.add(new Label("Direct Connect", skin, "gold")).colspan(2).padBottom(10).row();

        panel.add(new Label("Server Address:", skin)).right().padRight(20);
        TextField addrField = new TextField("localhost", skin);
        panel.add(addrField).width(240).row();

        panel.add(new Label("Port:", skin)).right().padRight(20);
        TextField portField = new TextField(String.valueOf(Protocol.DEFAULT_PORT), skin);
        panel.add(portField).width(240).padTop(6).row();

        TextButton connectBtn = new TextButton("Connect", skin);
        connectBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                String host = addrField.getText().trim();
                int port;
                try { port = Integer.parseInt(portField.getText().trim()); }
                catch (NumberFormatException ex) { statusLabel.setText("Invalid port"); return; }
                statusLabel.setText("Connecting to " + host + ":" + port + "...");
                game.setScreen(new GameScreen(game, host, port));
            }
        });
        panel.add(connectBtn).colspan(2).width(240).height(46).padTop(12).row();

        // ── Host Server ───────────────────────────────────────────────────
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(1).padTop(18).padBottom(12).row();
        panel.add(new Label("Host LAN Server", skin, "gold")).colspan(2).padBottom(10).row();

        panel.add(new Label("Server Name:", skin)).right().padRight(20);
        TextField nameField = new TextField("My Server", skin);
        panel.add(nameField).width(240).row();

        panel.add(new Label("Max Players:", skin)).right().padRight(20);
        TextField maxField = new TextField("10", skin);
        panel.add(maxField).width(240).padTop(6).row();

        TextButton hostBtn = new TextButton("Host & Play", skin);
        hostBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                int port = game.getSettings().getServerPort();
                int max;
                try { max = Integer.parseInt(maxField.getText().trim()); } catch (Exception ex) { max = 10; }
                statusLabel.setText("Starting server...");
                try {
                    GameServer srv = new GameServer(port, nameField.getText().trim());
                    srv.setMaxPlayers(max);
                    srv.start(System.currentTimeMillis(), 256, 256);
                    game.setScreen(new GameScreen(game, "localhost", port));
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                }
            }
        });
        panel.add(hostBtn).colspan(2).width(240).height(46).padTop(12).row();

        // ── LAN Scan ──────────────────────────────────────────────────────
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(1).padTop(18).padBottom(12).row();
        TextButton scanBtn = new TextButton("Scan LAN", skin);
        Label lanLabel = new Label("Press Scan to find servers", skin, "small");
        scanBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                lanLabel.setText("Scanning...");
                new Thread(() -> {
                    var servers = GameClient.discoverLANServers(3000);
                    Gdx.app.postRunnable(() -> {
                        if (servers.isEmpty()) { lanLabel.setText("No servers found"); }
                        else {
                            StringBuilder sb = new StringBuilder();
                            servers.forEach(s -> sb.append(s).append('\n'));
                            lanLabel.setText(sb.toString().trim());
                        }
                    });
                }).start();
            }
        });
        panel.add(scanBtn).colspan(2).width(240).height(40).row();
        panel.add(lanLabel).colspan(2).padTop(6).row();

        // ── Status + Back ─────────────────────────────────────────────────
        statusLabel = new Label("", skin, "small");
        statusLabel.setColor(Color.YELLOW);
        panel.add(statusLabel).colspan(2).padTop(12).row();

        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(1).padTop(14).padBottom(12).row();
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        panel.add(back).colspan(2).width(240).height(46).row();

        root.add(panel).center();
    }

    @Override
    public void render(float delta) {
        background.render();
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { dispose(); }

    @Override
    public void dispose() {
        if (stage      != null) stage.dispose();
        if (skin       != null) skin.dispose();
        if (background != null) background.dispose();
    }
}
