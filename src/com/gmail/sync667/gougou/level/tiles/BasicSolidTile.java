package com.gmail.sync667.gougou.level.tiles;

public class BasicSolidTile extends BasicTile {

    public BasicSolidTile(int id, int x, int y, int tileColour, int levelColour, int scale) {
        super(id, x, y, tileColour, levelColour, scale);
        this.solid = true;
    }

}
