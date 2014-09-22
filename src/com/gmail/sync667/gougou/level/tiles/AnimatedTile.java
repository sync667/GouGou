package com.gmail.sync667.gougou.level.tiles;

public class AnimatedTile extends BasicLiquidTile {

    private final int[][] animationTileCoords;
    private int currentAnimationIndex;
    private long lastInterationTime;
    private final int animationSwitchDelay;

    public AnimatedTile(int id, int[][] animationCoords, int tileColour, int levelColour, int scale,
            int animationSwitchDelay) {
        super(id, animationCoords[0][0], animationCoords[0][1], tileColour, levelColour, scale);
        this.animationTileCoords = animationCoords;
        this.currentAnimationIndex = 0;
        this.lastInterationTime = System.currentTimeMillis();
        this.animationSwitchDelay = animationSwitchDelay;
    }

    @Override
    public void tick() {
        if ((System.currentTimeMillis() - lastInterationTime) >= (animationSwitchDelay)) {
            lastInterationTime = System.currentTimeMillis();
            currentAnimationIndex = (currentAnimationIndex + 1) % animationTileCoords.length;

            this.tileId = (animationTileCoords[currentAnimationIndex][0] + (animationTileCoords[currentAnimationIndex][1] * 32));
        }
    }
}
