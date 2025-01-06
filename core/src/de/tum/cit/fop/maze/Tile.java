package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;

import static de.tum.cit.fop.maze.Constants.TILE_SCREEN_SIZE;

public class Tile extends StaticTiledMapTile{
    private Position tilePosition;
    private Rectangle hitbox;

    public Tile(TextureRegion textureRegion) {
        super(textureRegion);
        this.tilePosition = null;
        this.hitbox = null;
    }

    public Position getTilePosition() {
        if (tilePosition != null)
            return tilePosition;
        else
            throw new IllegalStateException("Tile position has not been initialized");
    }

    public void setTilePosition(Position tilePosition) {
        tilePosition = tilePosition.convertTo(Position.PositionUnit.TILES);
        this.tilePosition = tilePosition;
        //Position tilePosition = this.getTilePosition();
        Gdx.app.log("Tile", "tilePosition: " + tilePosition);
        tilePosition = tilePosition.convertTo(Position.PositionUnit.PIXELS);
        float x = tilePosition.getX() - (float) TILE_SCREEN_SIZE / 2;
        float y = tilePosition.getY() - TILE_SCREEN_SIZE / 2.0f;
        this.setHitbox(new Rectangle(x, y, TILE_SCREEN_SIZE, TILE_SCREEN_SIZE));
    }

    public int getTileX(){
        return this.tilePosition.getTileX();
    }

    public int getTileY(){
        return this.tilePosition.getTileY();
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void setHitbox(Rectangle hitbox) {
        this.hitbox = hitbox;
    }
}
