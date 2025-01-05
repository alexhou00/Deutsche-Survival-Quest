package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import java.util.ArrayList;
import java.util.List;

public class Exit extends StaticTiledMapTile {
    private List<Position> tilePositions;

    public Exit(TextureRegion textureRegion) {
        super(textureRegion);
        this.tilePositions = new ArrayList<>();
    }

    public Position getTilePosition(int index) {
        if (tilePositions.get(index) != null)
            return tilePositions.get(index);
        else
            throw new IllegalStateException("Tile position has not been initialized");
    }

    public void setTilePosition(int index, Position tilePosition) {
        this.tilePositions.set(index, tilePosition);
    }

    public void addTilePosition(Position tilePosition) {
        this.tilePositions.add(tilePosition);
    }


    public List<Position> getTilePositions() {
        return tilePositions;
    }

    public void setTilePositions(List<Position> tilePositions) {
        this.tilePositions = tilePositions;
    }


    public int getTileX(int index){
        return this.tilePositions.get(index).getTileX();
    }

    public int getTileY(int index){
        return this.tilePositions.get(index).getTileY();
    }
}
