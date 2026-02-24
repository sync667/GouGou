package com.gougou.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gougou.core.GouGouGame;
import com.gougou.core.profile.UserProfile;

public class ProfileScreen implements Screen {
    private final GouGouGame game;
    private Stage stage;
    private Skin skin;

    public ProfileScreen(GouGouGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = createSkin();

        UserProfile profile = game.getProfileManager().getCurrentProfile();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Profile & Character", skin, "title");
        table.add(title).colspan(2).padBottom(30).row();

        // Username
        table.add(new Label("Username:", skin)).padRight(20);
        TextField usernameField = new TextField(profile.getUsername(), skin);
        table.add(usernameField).width(250).row();

        // Display Name
        table.add(new Label("Display Name:", skin)).padRight(20);
        TextField displayField = new TextField(profile.getDisplayName(), skin);
        table.add(displayField).width(250).row();

        // Character Class
        table.add(new Label("Class:", skin)).padRight(20);
        SelectBox<String> classSelect = new SelectBox<>(skin);
        classSelect.setItems("Warrior", "Mage", "Ranger");
        classSelect.setSelectedIndex(profile.getCharacterClass());
        table.add(classSelect).width(250).row();

        // Skin Color
        table.add(new Label("Skin Color:", skin)).padRight(20);
        SelectBox<String> skinSelect = new SelectBox<>(skin);
        skinSelect.setItems("Light", "Fair", "Medium", "Tan", "Brown", "Dark");
        skinSelect.setSelectedIndex(profile.getSkinColor());
        table.add(skinSelect).width(250).row();

        // Stats
        table.add(new Label("", skin)).padTop(20).row();
        table.add(new Label("--- Stats ---", skin, "subtitle")).colspan(2).padBottom(10).row();
        table.add(new Label("Games Played:", skin)).padRight(20);
        table.add(new Label(String.valueOf(profile.getGamesPlayed()), skin)).row();
        table.add(new Label("Total Play Time:", skin)).padRight(20);
        int mins = profile.getTotalPlayTimeMinutes();
        table.add(new Label(mins / 60 + "h " + mins % 60 + "m", skin)).row();

        // Buttons
        Table buttonRow = new Table();

        TextButton saveButton = new TextButton("Save", skin);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                profile.setUsername(usernameField.getText().trim());
                profile.setDisplayName(displayField.getText().trim());
                profile.setCharacterClass(classSelect.getSelectedIndex());
                profile.setSkinColor(skinSelect.getSelectedIndex());
                game.getProfileManager().save();
                game.getSettings().setPlayerName(profile.getDisplayName());
                game.getSettings().save();
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
        BitmapFont subtitleFont = new BitmapFont();
        subtitleFont.getData().setScale(1.5f);
        skin.add("subtitle-font", subtitleFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        skin.add("default", new Label.LabelStyle(defaultFont, Color.WHITE));
        skin.add("title", new Label.LabelStyle(titleFont, new Color(0.2f, 0.8f, 0.4f, 1)));
        skin.add("subtitle", new Label.LabelStyle(subtitleFont, Color.GOLD));

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
