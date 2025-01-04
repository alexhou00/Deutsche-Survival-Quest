package de.tum.cit.fop.maze;

public class Constants {
    public static final int WORLD_WIDTH = 2000;
    public static final int WORLD_HEIGHT = 2000;

    public static final float ZOOM_SPEED = 0.1f; // Controls how quickly the camera adjusts to the target zoom
    public static final float MIN_ZOOM_LEVEL = 0.6f; // MIN is actually zoom in
    public static final float MAX_ZOOM_LEVEL = 1.3f; // MAX is actually zoom out

    public static final int TILE_SIZE = 16;

    static int horizontalTilesCount = 20; // number of tiles on the width
    public static int TILE_SCREEN_SIZE = WORLD_WIDTH / horizontalTilesCount;
    // public static int TILE_SCREEN_SIZE = TILE_SIZE;

    public static final int MAX_PLAYER_LIVES = 10;
}
