package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;

import static de.tum.cit.fop.maze.Constants.TILE_SCREEN_SIZE;
import static de.tum.cit.fop.maze.Constants.TILE_SIZE;

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
        this.tilePosition = tilePosition.convertTo(Position.PositionUnit.TILES);

        // Set Hitbox upon updating its position
        tilePosition = tilePosition.convertTo(Position.PositionUnit.PIXELS);
        float x = tilePosition.getX() - (float) TILE_SCREEN_SIZE / 2;
        float y = tilePosition.getY() - TILE_SCREEN_SIZE / 2.0f;

        Rectangle hitbox = calculateHitbox(x, y, this.getTextureRegion());
        this.setHitbox(hitbox);
        //this.setHitbox(new Rectangle(x, y, TILE_SCREEN_SIZE, TILE_SCREEN_SIZE));
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

    /**
     * Calculates the hitbox (non-transparent area) for a tile based on its texture region.
     *
     * @param textureRegion The texture region of the tile.
     * @return A {@link Rectangle} representing the hitbox in pixel coordinates.
     */
    private Rectangle calculateHitbox(float baseX, float baseY, TextureRegion textureRegion) {
        float scale = (float) TILE_SCREEN_SIZE / TILE_SIZE;

        // Extract the texture and ensure it's prepared
        Texture texture = textureRegion.getTexture();
        TextureData textureData = texture.getTextureData();

        // Ensure the TextureData is prepared for pixel access
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }

        // Access the Pixmap
        Pixmap pixmap = textureData.consumePixmap();
        int startX = textureRegion.getRegionX();
        int startY = textureRegion.getRegionY();
        int width = textureRegion.getRegionWidth();
        int height = textureRegion.getRegionHeight();

        // Variables to store the bounding box
        int minX = width, minY = height, maxX = 0, maxY = 0;

        // Iterate over the pixels in the texture region
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixmap.getPixel(startX + x, startY + y);
                int alpha = (pixel & 0xFF000000) >>> 24; // Extract the alpha channel

                if (alpha > 0) { // Non-transparent pixel
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // Dispose the Pixmap after processing
        pixmap.dispose();

        // Handle the case where the entire tile is transparent
        if (minX > maxX || minY > maxY) {
            return new Rectangle(baseX, baseY, 0, 0); // No hitbox
        }

        // Create a rectangle using the bounding box
        // Gdx.app.log("Rectangle", rect +  "");
        return new Rectangle(baseX + minX, baseY + minY, (maxX - minX + 1) * scale, (maxY - minY + 1) * scale);
    }
}
