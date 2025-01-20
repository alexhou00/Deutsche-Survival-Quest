package de.tum.cit.fop.maze.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents an entrance tile in the maze.
 * Entrances are where the player starts a level.
 */
public class Entrance extends Tile {
    public Entrance(TextureRegion textureRegion) {
        super(textureRegion);
    }
}
