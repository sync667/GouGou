package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.config.GameSettings;
import com.gougou.core.ui.OldSchoolSkin;
import com.gougou.core.ui.ScreenBackground;

public class SettingsScreen implements Screen {

    private final GouGouGame game;
    private Stage stage;
    private OldSchoolSkin skin;
    private ScreenBackground background;

    public SettingsScreen(GouGouGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage      = new Stage(new ScreenViewport());
        skin       = new OldSchoolSkin();
        background = new ScreenBackground();
        Gdx.input.setInputProcessor(stage);

        GameSettings s = game.getSettings();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table panel = new Table();
        panel.setBackground(skin.panelDrawable());
        panel.pad(30, 50, 30, 50);

        panel.add(new Label("Settings", skin, "subtitle")).colspan(2).padBottom(4).row();
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(2).padBottom(20).row();

        // Resolution
        panel.add(new Label("Resolution:", skin)).right().padRight(20);
        SelectBox<String> resBox = new SelectBox<>(skin);
        resBox.setItems("800x600","1024x768","1280x720","1366x768","1600x900","1920x1080","2560x1440");
        resBox.setSelected(s.getResolutionWidth() + "x" + s.getResolutionHeight());
        panel.add(resBox).width(220).row();

        // Fullscreen
        panel.add(new Label("Fullscreen:", skin)).right().padRight(20);
        CheckBox fullCheck = new CheckBox("", skin);
        fullCheck.setChecked(s.isFullscreen());
        panel.add(fullCheck).left().padTop(8).row();

        // Music Volume
        panel.add(new Label("Music Volume:", skin)).right().padRight(20);
        Label musicLbl = new Label(fmt(s.getMusicVolume()), skin, "small");
        Slider musicSlider = new Slider(0, 1, 0.05f, false, skin);
        musicSlider.setValue(s.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                musicLbl.setText(fmt(musicSlider.getValue()));
            }
        });
        Table mr = new Table(); mr.add(musicSlider).width(160); mr.add(musicLbl).padLeft(10);
        panel.add(mr).padTop(8).row();

        // SFX Volume
        panel.add(new Label("SFX Volume:", skin)).right().padRight(20);
        Label sfxLbl = new Label(fmt(s.getSfxVolume()), skin, "small");
        Slider sfxSlider = new Slider(0, 1, 0.05f, false, skin);
        sfxSlider.setValue(s.getSfxVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                sfxLbl.setText(fmt(sfxSlider.getValue()));
            }
        });
        Table sr = new Table(); sr.add(sfxSlider).width(160); sr.add(sfxLbl).padLeft(10);
        panel.add(sr).padTop(8).row();

        // Show FPS
        panel.add(new Label("Show FPS:", skin)).right().padRight(20);
        CheckBox fpsCheck = new CheckBox("", skin);
        fpsCheck.setChecked(s.isShowFps());
        panel.add(fpsCheck).left().padTop(8).row();

        // View Distance
        panel.add(new Label("View Distance:", skin)).right().padRight(20);
        Label viewLbl = new Label(String.valueOf(s.getViewDistance()), skin, "small");
        Slider viewSlider = new Slider(5, 20, 1, false, skin);
        viewSlider.setValue(s.getViewDistance());
        viewSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                viewLbl.setText(String.valueOf((int) viewSlider.getValue()));
            }
        });
        Table vr = new Table(); vr.add(viewSlider).width(160); vr.add(viewLbl).padLeft(10);
        panel.add(vr).padTop(8).row();

        // Server Port
        panel.add(new Label("Server Port:", skin)).right().padRight(20);
        TextField portField = new TextField(String.valueOf(s.getServerPort()), skin);
        panel.add(portField).width(220).padTop(8).row();

        // Buttons
        panel.add(new Image(skin.goldSeparator())).colspan(2).fillX().height(1).padTop(20).padBottom(16).row();
        Table btns = new Table();
        TextButton save = new TextButton("Save & Back", skin);
        save.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                String[] parts = resBox.getSelected().split("x");
                s.setResolutionWidth(Integer.parseInt(parts[0]));
                s.setResolutionHeight(Integer.parseInt(parts[1]));
                s.setFullscreen(fullCheck.isChecked());
                s.setMusicVolume(musicSlider.getValue());
                s.setSfxVolume(sfxSlider.getValue());
                s.setShowFps(fpsCheck.isChecked());
                s.setViewDistance((int) viewSlider.getValue());
                try { s.setServerPort(Integer.parseInt(portField.getText())); } catch (Exception ignored) {}
                s.save();
                if (s.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                else Gdx.graphics.setWindowedMode(s.getResolutionWidth(), s.getResolutionHeight());
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

    private String fmt(float v) { return String.format("%.0f%%", v * 100); }

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
