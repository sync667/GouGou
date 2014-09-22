package com.gmail.sync667.gougou.level.tiles;

import com.gmail.sync667.gougou.gfx.Screen;
import com.gmail.sync667.gougou.level.Level;

public class BasicTile extends Tile {

    protected int tileId;
    protected int tileColour;
    protected int scale;

    public BasicTile(int id, int x, int y, int tileColour, int levelColour, int scale) {
        super(id, false, false, false, levelColour, scale);
        this.tileId = x + y * 32;
        this.tileColour = tileColour;
        this.scale = scale;
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(Screen screen, Level level, int x, int y) {
        screen.render(x, y, tileId, tileColour, 0x00, scale);
    }

}
