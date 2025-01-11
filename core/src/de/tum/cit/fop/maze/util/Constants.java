package de.tum.cit.fop.maze.util;

public class Constants {
    public static final float ZOOM_SPEED = 0.1f; // Controls how quickly the camera adjusts to the target zoom
    public static final float MIN_ZOOM_LEVEL = 0.6f; // MIN is actually zoom in
    public static final float MAX_ZOOM_LEVEL = 1.3f; // MAX is actually zoom out

    public static final int TILE_SIZE = 16; // in pixels

    public static int horizontalTilesCount = 1; // number of tiles on the width
    public static int verticalTilesCount = 1; // number of tiles on the height
    public static final int TILE_SCREEN_SIZE = 100;

    public static int getWorldWidth() {
        return horizontalTilesCount * TILE_SCREEN_SIZE;
    }

    public static int getWorldHeight() {
        return verticalTilesCount * TILE_SCREEN_SIZE;
    }

    public static final int MAX_PLAYER_LIVES = 10;
}
