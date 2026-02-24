package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;

public class MainMenuScreen implements Screen {
    private final GouGouGame game;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(GouGouGame game) {
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

        // Title
        Label title = new Label("GouGou", skin, "title");
        table.add(title).padBottom(10).row();

        Label subtitle = new Label("A 2D Multiplayer Adventure", skin);
        table.add(subtitle).padBottom(40).row();

        // Profile info
        String profileInfo = "Welcome, " + game.getProfileManager().getCurrentProfile().getDisplayName()
                + " (Lv.1 " + game.getProfileManager().getCurrentProfile().getClassName() + ")";
        Label profileLabel = new Label(profileInfo, skin);
        table.add(profileLabel).padBottom(30).row();

        // Buttons
        TextButton playButton = new TextButton("Single Player", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, null, 0));
            }
        });
        table.add(playButton).width(300).height(50).padBottom(10).row();

        TextButton serverButton = new TextButton("Multiplayer", skin);
        serverButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ServerBrowserScreen(game));
            }
        });
        table.add(serverButton).width(300).height(50).padBottom(10).row();

        TextButton profileButton = new TextButton("Profile & Character", skin);
        profileButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ProfileScreen(game));
            }
        });
        table.add(profileButton).width(300).height(50).padBottom(10).row();

        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        table.add(settingsButton).width(300).height(50).padBottom(10).row();

        TextButton quitButton = new TextButton("Quit", skin);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(quitButton).width(300).height(50).padBottom(10).row();

        // Version
        Label version = new Label("v1.0.0", skin);
        table.add(version).padTop(20).row();
    }

    private Skin createSkin() {
        Skin skin = new Skin();

        // Default font
        BitmapFont defaultFont = new BitmapFont();
        defaultFont.getData().setScale(1.2f);
        skin.add("default-font", defaultFont);

        // Title font
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(3.0f);
        skin.add("title-font", titleFont);

        // Create pixmap for UI elements
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new com.badlogic.gdx.graphics.Texture(pixmap));
        pixmap.dispose();

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = defaultFont;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // Title label style
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = new Color(0.2f, 0.8f, 0.4f, 1);
        skin.add("title", titleStyle);

        // Button style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = defaultFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GREEN;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;
        buttonStyle.up = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 0.9f));
        buttonStyle.over = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.45f, 0.9f));
        buttonStyle.down = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.9f));
        skin.add("default", buttonStyle);

        // TextField style
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = defaultFont;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.9f));
        textFieldStyle.cursor = skin.newDrawable("white", Color.WHITE);
        textFieldStyle.selection = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.6f, 0.5f));
        skin.add("default", textFieldStyle);

        // SelectBox style
        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = defaultFont;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.background = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 0.9f));
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = defaultFont;
        listStyle.fontColorSelected = Color.GREEN;
        listStyle.fontColorUnselected = Color.WHITE;
        listStyle.selection = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.5f, 0.9f));
        selectBoxStyle.listStyle = listStyle;
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.95f));
        selectBoxStyle.scrollStyle = scrollStyle;
        skin.add("default", selectBoxStyle);

        // CheckBox style
        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.font = defaultFont;
        checkBoxStyle.fontColor = Color.WHITE;
        com.badlogic.gdx.graphics.Pixmap checkOff = new com.badlogic.gdx.graphics.Pixmap(20, 20, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        checkOff.setColor(new Color(0.3f, 0.3f, 0.4f, 1));
        checkOff.fill();
        checkOff.setColor(Color.WHITE);
        checkOff.drawRectangle(0, 0, 20, 20);
        com.badlogic.gdx.graphics.Pixmap checkOn = new com.badlogic.gdx.graphics.Pixmap(20, 20, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        checkOn.setColor(new Color(0.2f, 0.6f, 0.3f, 1));
        checkOn.fill();
        checkOn.setColor(Color.WHITE);
        checkOn.drawRectangle(0, 0, 20, 20);
        checkBoxStyle.checkboxOff = new com.badlogic.gdx.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(new com.badlogic.gdx.graphics.Texture(checkOff)));
        checkBoxStyle.checkboxOn = new com.badlogic.gdx.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(new com.badlogic.gdx.graphics.Texture(checkOn)));
        checkOff.dispose();
        checkOn.dispose();
        skin.add("default", checkBoxStyle);

        // Slider style
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.4f, 1));
        sliderStyle.background.setMinHeight(6);
        com.badlogic.gdx.graphics.Pixmap knobPixmap = new com.badlogic.gdx.graphics.Pixmap(16, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        knobPixmap.setColor(Color.GREEN);
        knobPixmap.fillCircle(8, 8, 7);
        sliderStyle.knob = new com.badlogic.gdx.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(new com.badlogic.gdx.graphics.Texture(knobPixmap)));
        knobPixmap.dispose();
        skin.add("default-horizontal", sliderStyle);

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
