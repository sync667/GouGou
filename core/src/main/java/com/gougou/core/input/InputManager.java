package com.gougou.core.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputManager {
    public boolean isMoveUp() {
        return Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
    }
    public boolean isMoveDown() {
        return Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
    }
    public boolean isMoveLeft() {
        return Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
    }
    public boolean isMoveRight() {
        return Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }
    public boolean isInventoryToggle() {
        return Gdx.input.isKeyJustPressed(Input.Keys.I);
    }
    public boolean isChatToggle() {
        return Gdx.input.isKeyJustPressed(Input.Keys.T);
    }
    public boolean isEscape() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }
    public boolean isInteract() {
        return Gdx.input.isKeyJustPressed(Input.Keys.E);
    }
    public boolean isAttack() {
        return Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }
}
