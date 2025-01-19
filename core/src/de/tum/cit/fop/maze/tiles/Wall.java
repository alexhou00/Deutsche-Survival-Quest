package de.tum.cit.fop.maze.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents a wall tile in the maze.
 * Walls are static tiles that serve as non-through regions in the game.
 */
public class Wall extends Tile {
    public Wall(TextureRegion textureRegion) {
        super(textureRegion);
    }
}