package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class ChatSystem {

    private final List<ChatMessage> messages = new ArrayList<>();
    private boolean inputActive = false;
    private final StringBuilder currentInput = new StringBuilder();
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final FreeTypeFontGenerator gen;
    private final Texture bgTex;
    private ChatSendListener sendListener;

    private static final int MAX_MESSAGES      = 50;
    private static final int VISIBLE_MESSAGES  = 8;
    private static final float MESSAGE_FADE    = 10f;
    private static final float LINE_HEIGHT     = 19f;
    private static final float CHAT_X          = 12f;
    private static final float CHAT_WIDTH      = 420f;

    public record ChatMessage(String sender, String text, float timestamp, Color color) {}

    public interface ChatSendListener {
        void onSendMessage(String message);
    }

    public ChatSystem() {
        shapeRenderer = new ShapeRenderer();

        gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Metamorphous-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size      = 16;
        p.color     = Color.WHITE;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        font = gen.generateFont(p);

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(0f, 0f, 0f, 0.65f);
        px.fill();
        bgTex = new Texture(px);
        px.dispose();
    }

    public void setSendListener(ChatSendListener l)  { sendListener = l; }

    public void addMessage(String sender, String text) {
        addMessage(sender, text, OldSchoolSkin.PARCHMENT);
    }

    public void addMessage(String sender, String text, Color color) {
        float now = System.currentTimeMillis() / 1000f;
        messages.add(new ChatMessage(sender, text, now, color));
        if (messages.size() > MAX_MESSAGES) messages.remove(0);
    }

    public void addSystemMessage(String text) {
        addMessage("[System]", text, OldSchoolSkin.GOLD_TEXT);
    }

    public void toggleInput() {
        inputActive = !inputActive;
        if (!inputActive && currentInput.length() > 0) {
            String msg = currentInput.toString().trim();
            if (!msg.isEmpty() && sendListener != null) sendListener.onSendMessage(msg);
            currentInput.setLength(0);
        }
    }

    public void handleCharTyped(char ch) {
        if (!inputActive) return;
        if (ch == '\n' || ch == '\r') { toggleInput(); }
        else if (ch == '\b')          { if (currentInput.length() > 0) currentInput.deleteCharAt(currentInput.length() - 1); }
        else if (ch >= 32)            { currentInput.append(ch); }
    }

    public void render(SpriteBatch batch, float screenW, float screenH) {
        float chatY   = 88f;
        float chatBgH = VISIBLE_MESSAGES * LINE_HEIGHT + 32f;

        // ── Background panel when active ──────────────────────────────────
        if (inputActive) {
            batch.setColor(Color.WHITE);
            batch.draw(bgTex, CHAT_X - 4, chatY - 4, CHAT_WIDTH + 8, chatBgH);
            batch.setColor(Color.WHITE);

            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(OldSchoolSkin.GOLD_DARK);
            shapeRenderer.rect(CHAT_X - 4, chatY - 4, CHAT_WIDTH + 8, chatBgH);
            shapeRenderer.end();
            batch.begin();
        }

        // ── Messages ──────────────────────────────────────────────────────
        float now      = System.currentTimeMillis() / 1000f;
        int   startIdx = Math.max(0, messages.size() - VISIBLE_MESSAGES);
        float yPos     = chatY + LINE_HEIGHT * 0.5f;

        for (int i = startIdx; i < messages.size(); i++) {
            ChatMessage msg   = messages.get(i);
            float       age   = now - msg.timestamp();
            if (!inputActive && age > MESSAGE_FADE) continue;

            float alpha = inputActive ? 1f : Math.max(0f, 1f - (age - MESSAGE_FADE + 3f) / 3f);
            Color c = new Color(msg.color());
            c.a = alpha;
            font.setColor(c);
            font.draw(batch, msg.sender() + ": " + msg.text(), CHAT_X + 4, yPos);
            yPos += LINE_HEIGHT;
        }

        // ── Input line ────────────────────────────────────────────────────
        if (inputActive) {
            font.setColor(OldSchoolSkin.GOLD_BRIGHT);
            font.draw(batch, "> " + currentInput + "_", CHAT_X + 4, chatY + 6);
        }
        font.setColor(Color.WHITE);
    }

    public boolean isInputActive() { return inputActive; }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        gen.dispose();
        bgTex.dispose();
    }
}
