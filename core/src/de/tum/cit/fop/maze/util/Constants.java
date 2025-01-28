package de.tum.cit.fop.maze.util;

import com.badlogic.gdx.utils.Array;

public class Constants {
    public static final float ZOOM_SPEED = 0.1f; // Controls how quickly the camera adjusts to the target zoom
    public static final int MIN_ZOOM_TILES_COUNT = 10; // MIN is actually zoom in, this is the minimum tiles count on the window's width of the window to clamp zooming
    public static final int MAX_ZOOM_TILES_COUNT = 18; // MAX is actually zoom out, this is the maximum tiles count on the window's width of the window to clamp zooming

    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;

    public static final int TILE_SIZE = 16; // in pixels
    public static final int TRAP_SIZE = 32;
    public static final int ENEMY_SIZE = 16;

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

    public static <T> Array.ArrayIterator<T> iterate(Array<T> array){
        return new Array.ArrayIterator<>(array);
    }

    public static String capitalize(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
