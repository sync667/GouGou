package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.ui.OldSchoolSkin;
import com.gougou.core.ui.ScreenBackground;

public class MainMenuScreen implements Screen {

    private final GouGouGame game;
    private Stage stage;
    private OldSchoolSkin skin;
    private ScreenBackground background;

    public MainMenuScreen(GouGouGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage      = new Stage(new ScreenViewport());
        skin       = new OldSchoolSkin();
        background = new ScreenBackground();
        Gdx.input.setInputProcessor(stage);

        // ── Root layout ──────────────────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // ── Centered panel ───────────────────────────────────────────────
        Table panel = new Table();
        panel.setBackground(skin.panelDrawable());
        panel.pad(36, 50, 36, 50);

        // Title
        Label title = new Label("GouGou", skin, "title");
        panel.add(title).padBottom(6).row();

        // Subtitle
        Label sub = new Label("Old-School 2D MMORPG", skin, "subtitle");
        panel.add(sub).padBottom(4).row();

        // Gold separator
        Image sep1 = new Image(skin.goldSeparator());
        panel.add(sep1).width(300).height(2).padBottom(18).row();

        // Profile info
        String name  = game.getProfileManager().getCurrentProfile().getDisplayName();
        String klass = game.getProfileManager().getCurrentProfile().getClassName();
        Label profile = new Label("Welcome, " + name + "  \u2014  Lv.1 " + klass, skin, "small");
        panel.add(profile).padBottom(22).row();

        // Buttons
        addButton(panel, "Single Player",      () -> game.setScreen(new GameScreen(game, null, 0)));
        addButton(panel, "Multiplayer",        () -> game.setScreen(new ServerBrowserScreen(game)));
        addButton(panel, "Profile & Character",() -> game.setScreen(new ProfileScreen(game)));
        addButton(panel, "Settings",           () -> game.setScreen(new SettingsScreen(game)));
        addButton(panel, "Quit",               Gdx.app::exit);

        // Bottom separator + version
        Image sep2 = new Image(skin.goldSeparator());
        panel.add(sep2).width(300).height(1).padTop(14).padBottom(6).row();
        panel.add(new Label("v1.0.0", skin, "small")).row();

        root.add(panel).center();
    }

    private void addButton(Table panel, String text, Runnable action) {
        TextButton btn = new TextButton(text, skin);
        btn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { action.run(); }
        });
        panel.add(btn).width(300).height(50).padBottom(10).row();
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
