package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

public class Entrance extends StaticTiledMapTile {
    private Position tilePosition;

    public Entrance(TextureRegion textureRegion) {
        super(textureRegion);
        this.tilePosition = null;
    }

    public Position getTilePosition() {
        if (tilePosition != null)
            return tilePosition;
        else
            throw new IllegalStateException("Tile position has not been initialized");
    }

    public void setTilePosition(Position tilePosition) {
        this.tilePosition = tilePosition;
    }

    public int getTileX(){
        return this.tilePosition.getTileX();
    }

    public int getTileY(){
        return this.tilePosition.getTileY();
    }
}
