package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class ChatSystem {
    private final List<ChatMessage> messages = new ArrayList<>();
    private boolean inputActive = false;
    private StringBuilder currentInput = new StringBuilder();
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private ChatSendListener sendListener;
    private static final int MAX_MESSAGES = 50;
    private static final int VISIBLE_MESSAGES = 8;
    private static final float MESSAGE_FADE_TIME = 10f;

    public record ChatMessage(String sender, String text, float timestamp, Color color) {}

    public interface ChatSendListener {
        void onSendMessage(String message);
    }

    public ChatSystem() {
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
    }

    public void setSendListener(ChatSendListener listener) {
        this.sendListener = listener;
    }

    public void addMessage(String sender, String text) {
        addMessage(sender, text, Color.WHITE);
    }

    public void addMessage(String sender, String text, Color color) {
        float time = (float) (System.currentTimeMillis() / 1000.0);
        messages.add(new ChatMessage(sender, text, time, color));
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }

    public void addSystemMessage(String text) {
        addMessage("[System]", text, Color.YELLOW);
    }

    public void toggleInput() {
        inputActive = !inputActive;
        if (!inputActive && currentInput.length() > 0) {
            String msg = currentInput.toString().trim();
            if (!msg.isEmpty() && sendListener != null) {
                sendListener.onSendMessage(msg);
            }
            currentInput.setLength(0);
        }
    }

    public void handleCharTyped(char character) {
        if (!inputActive) return;
        if (character == '\n' || character == '\r') {
            toggleInput();
        } else if (character == '\b') {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
        } else if (character >= 32) {
            currentInput.append(character);
        }
    }

    public void render(SpriteBatch batch, float screenWidth, float screenHeight) {
        float chatX = 10;
        float chatY = 80;
        float chatWidth = 400;

        batch.end();

        // Chat background when active
        if (inputActive) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.6f);
            shapeRenderer.rect(chatX, chatY, chatWidth, VISIBLE_MESSAGES * 18 + 30);
            shapeRenderer.end();
        }

        batch.begin();

        // Render messages
        float currentTime = (float) (System.currentTimeMillis() / 1000.0);
        int startIdx = Math.max(0, messages.size() - VISIBLE_MESSAGES);
        float yPos = chatY + 25;
        for (int i = startIdx; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            float age = currentTime - msg.timestamp();
            if (!inputActive && age > MESSAGE_FADE_TIME) continue;

            float alpha = inputActive ? 1.0f : Math.max(0, 1.0f - (age - MESSAGE_FADE_TIME + 3) / 3f);
            Color c = new Color(msg.color());
            c.a = alpha;
            font.setColor(c);
            font.draw(batch, msg.sender() + ": " + msg.text(), chatX + 5, yPos);
            yPos += 18;
        }

        // Input box
        if (inputActive) {
            font.setColor(Color.GREEN);
            font.draw(batch, "> " + currentInput.toString() + "_", chatX + 5, chatY + 5);
        }

        font.setColor(Color.WHITE);
    }

    public boolean isInputActive() { return inputActive; }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
