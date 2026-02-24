package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.config.GameSettings;

public class SettingsScreen implements Screen {
    private final GouGouGame game;
    private Stage stage;
    private Skin skin;

    public SettingsScreen(GouGouGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = createSkin();

        GameSettings settings = game.getSettings();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Settings", skin, "title");
        table.add(title).colspan(2).padBottom(30).row();

        // Resolution
        table.add(new Label("Resolution:", skin)).padRight(20);
        SelectBox<String> resSelect = new SelectBox<>(skin);
        resSelect.setItems("800x600", "1024x768", "1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440");
        resSelect.setSelected(settings.getResolutionWidth() + "x" + settings.getResolutionHeight());
        table.add(resSelect).width(200).row();

        // Fullscreen
        table.add(new Label("Fullscreen:", skin)).padRight(20);
        CheckBox fullscreenCheck = new CheckBox("", skin);
        fullscreenCheck.setChecked(settings.isFullscreen());
        table.add(fullscreenCheck).left().row();

        // Music Volume
        table.add(new Label("Music Volume:", skin)).padRight(20);
        Slider musicSlider = new Slider(0, 1, 0.05f, false, skin);
        musicSlider.setValue(settings.getMusicVolume());
        Label musicLabel = new Label(String.format("%.0f%%", settings.getMusicVolume() * 100), skin);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                musicLabel.setText(String.format("%.0f%%", musicSlider.getValue() * 100));
            }
        });
        Table musicRow = new Table();
        musicRow.add(musicSlider).width(150);
        musicRow.add(musicLabel).padLeft(10);
        table.add(musicRow).row();

        // SFX Volume
        table.add(new Label("SFX Volume:", skin)).padRight(20);
        Slider sfxSlider = new Slider(0, 1, 0.05f, false, skin);
        sfxSlider.setValue(settings.getSfxVolume());
        Label sfxLabel = new Label(String.format("%.0f%%", settings.getSfxVolume() * 100), skin);
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sfxLabel.setText(String.format("%.0f%%", sfxSlider.getValue() * 100));
            }
        });
        Table sfxRow = new Table();
        sfxRow.add(sfxSlider).width(150);
        sfxRow.add(sfxLabel).padLeft(10);
        table.add(sfxRow).row();

        // Show FPS
        table.add(new Label("Show FPS:", skin)).padRight(20);
        CheckBox fpsCheck = new CheckBox("", skin);
        fpsCheck.setChecked(settings.isShowFps());
        table.add(fpsCheck).left().row();

        // View Distance
        table.add(new Label("View Distance:", skin)).padRight(20);
        Slider viewSlider = new Slider(5, 20, 1, false, skin);
        viewSlider.setValue(settings.getViewDistance());
        Label viewLabel = new Label(String.valueOf(settings.getViewDistance()), skin);
        viewSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                viewLabel.setText(String.valueOf((int) viewSlider.getValue()));
            }
        });
        Table viewRow = new Table();
        viewRow.add(viewSlider).width(150);
        viewRow.add(viewLabel).padLeft(10);
        table.add(viewRow).row();

        // Server Port
        table.add(new Label("Server Port:", skin)).padRight(20);
        TextField portField = new TextField(String.valueOf(settings.getServerPort()), skin);
        table.add(portField).width(200).row();

        // Buttons
        table.add().padTop(30);
        Table buttonRow = new Table();

        TextButton saveButton = new TextButton("Save & Back", skin);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Apply settings
                String res = resSelect.getSelected();
                String[] parts = res.split("x");
                settings.setResolutionWidth(Integer.parseInt(parts[0]));
                settings.setResolutionHeight(Integer.parseInt(parts[1]));
                settings.setFullscreen(fullscreenCheck.isChecked());
                settings.setMusicVolume(musicSlider.getValue());
                settings.setSfxVolume(sfxSlider.getValue());
                settings.setShowFps(fpsCheck.isChecked());
                settings.setViewDistance((int) viewSlider.getValue());
                try {
                    settings.setServerPort(Integer.parseInt(portField.getText()));
                } catch (NumberFormatException e) {
                    // Keep current port
                }
                settings.save();

                // Apply resolution
                if (settings.isFullscreen()) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                } else {
                    Gdx.graphics.setWindowedMode(settings.getResolutionWidth(), settings.getResolutionHeight());
                }

                game.setScreen(new MainMenuScreen(game));
            }
        });

        TextButton cancelButton = new TextButton("Cancel", skin);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        buttonRow.add(saveButton).width(200).height(45).padRight(20);
        buttonRow.add(cancelButton).width(200).height(45);
        table.add(buttonRow).colspan(2).padTop(30).row();
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

        Label.LabelStyle labelStyle = new Label.LabelStyle(defaultFont, Color.WHITE);
        skin.add("default", labelStyle);
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, new Color(0.2f, 0.8f, 0.4f, 1));
        skin.add("title", titleStyle);

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

        CheckBox.CheckBoxStyle cbStyle = new CheckBox.CheckBoxStyle();
        cbStyle.font = defaultFont;
        cbStyle.fontColor = Color.WHITE;
        Pixmap checkOff = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        checkOff.setColor(new Color(0.3f, 0.3f, 0.4f, 1));
        checkOff.fill();
        checkOff.setColor(Color.WHITE);
        checkOff.drawRectangle(0, 0, 20, 20);
        Pixmap checkOn = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        checkOn.setColor(new Color(0.2f, 0.6f, 0.3f, 1));
        checkOn.fill();
        checkOn.setColor(Color.WHITE);
        checkOn.drawRectangle(0, 0, 20, 20);
        cbStyle.checkboxOff = new TextureRegionDrawable(new TextureRegion(new Texture(checkOff)));
        cbStyle.checkboxOn = new TextureRegionDrawable(new TextureRegion(new Texture(checkOn)));
        checkOff.dispose();
        checkOn.dispose();
        skin.add("default", cbStyle);

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.4f, 1));
        sliderStyle.background.setMinHeight(6);
        Pixmap knobPix = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        knobPix.setColor(Color.GREEN);
        knobPix.fillCircle(8, 8, 7);
        sliderStyle.knob = new TextureRegionDrawable(new TextureRegion(new Texture(knobPix)));
        knobPix.dispose();
        skin.add("default-horizontal", sliderStyle);

        SelectBox.SelectBoxStyle sbStyle = new SelectBox.SelectBoxStyle();
        sbStyle.font = defaultFont;
        sbStyle.fontColor = Color.WHITE;
        sbStyle.background = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 0.9f));
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = defaultFont;
        listStyle.fontColorSelected = Color.GREEN;
        listStyle.fontColorUnselected = Color.WHITE;
        listStyle.selection = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.5f, 0.9f));
        sbStyle.listStyle = listStyle;
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.95f));
        sbStyle.scrollStyle = scrollStyle;
        skin.add("default", sbStyle);

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
