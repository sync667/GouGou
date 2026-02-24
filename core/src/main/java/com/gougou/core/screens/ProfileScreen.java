package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.profile.UserProfile;
import com.gougou.core.ui.OldSchoolSkin;
import com.gougou.core.ui.ScreenBackground;

public class ProfileScreen implements Screen {

    private final GouGouGame game;
    private Stage stage;
    private OldSchoolSkin skin;
    private ScreenBackground background;

    public ProfileScreen(GouGouGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage      = new Stage(new ScreenViewport());
        skin       = new OldSchoolSkin();
        background = new ScreenBackground();
        Gdx.input.setInputProcessor(stage);

        UserProfile profile = game.getProfileManager().getCurrentProfile();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table panel = new Table();
        panel.setBackground(skin.panelDrawable());
        panel.pad(30, 50, 30, 50);

        panel.add(new Label("Profile & Character", skin, "subtitle")).colspan(2).padBottom(6).row();
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(2).padBottom(20).row();

        // Username
        panel.add(new Label("Username:", skin)).right().padRight(20);
        TextField usernameField = new TextField(profile.getUsername(), skin);
        panel.add(usernameField).width(260).row();

        // Display name
        panel.add(new Label("Display Name:", skin)).right().padRight(20);
        TextField displayField = new TextField(profile.getDisplayName(), skin);
        panel.add(displayField).width(260).padTop(8).row();

        // Class
        panel.add(new Label("Class:", skin)).right().padRight(20);
        SelectBox<String> classBox = new SelectBox<>(skin);
        classBox.setItems("Warrior", "Mage", "Ranger");
        classBox.setSelectedIndex(profile.getCharacterClass());
        panel.add(classBox).width(260).padTop(8).row();

        // Skin colour
        panel.add(new Label("Skin Colour:", skin)).right().padRight(20);
        SelectBox<String> skinBox = new SelectBox<>(skin);
        skinBox.setItems("Light", "Fair", "Medium", "Tan", "Brown", "Dark");
        skinBox.setSelectedIndex(profile.getSkinColor());
        panel.add(skinBox).width(260).padTop(8).row();

        // Stats section
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(1).padTop(20).padBottom(14).row();
        panel.add(new Label("Games Played:", skin, "small")).right().padRight(20);
        panel.add(new Label(String.valueOf(profile.getGamesPlayed()), skin, "small")).left().row();
        int mins = profile.getTotalPlayTimeMinutes();
        panel.add(new Label("Total Play Time:", skin, "small")).right().padRight(20);
        panel.add(new Label(mins / 60 + "h " + mins % 60 + "m", skin, "small")).left().row();

        // Buttons
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(1).padTop(20).padBottom(16).row();
        Table btns = new Table();
        TextButton save = new TextButton("Save", skin);
        save.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                profile.setUsername(usernameField.getText().trim());
                profile.setDisplayName(displayField.getText().trim());
                profile.setCharacterClass(classBox.getSelectedIndex());
                profile.setSkinColor(skinBox.getSelectedIndex());
                game.getProfileManager().save();
                game.getSettings().setPlayerName(profile.getDisplayName());
                game.getSettings().save();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        TextButton cancel = new TextButton("Cancel", skin);
        cancel.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        btns.add(save).width(200).height(46).padRight(16);
        btns.add(cancel).width(200).height(46);
        panel.add(btns).colspan(2).row();

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
