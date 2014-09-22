package com.gmail.sync667.gougou.level.tiles;

public class BasicLiquidTile extends BasicTile {

    public BasicLiquidTile(int id, int x, int y, int tileColour, int levelColour, int scale) {
        super(id, x, y, tileColour, levelColour, scale);
        solid = false;
        liquid = true;
    }

}
