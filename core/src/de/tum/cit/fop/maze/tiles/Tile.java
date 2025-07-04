package de.tum.cit.fop.maze.tiles;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.util.Position;

import java.util.HashMap;
import java.util.Map;

import static de.tum.cit.fop.maze.util.Constants.TILE_SCREEN_SIZE;
import static de.tum.cit.fop.maze.util.Constants.TILE_SIZE;

/**
 * Represents a generic tile in the game, with hitbox and position functionalities.
 */
public class Tile extends StaticTiledMapTile{
    private float worldX, worldY;
    private Position tilePosition;

    protected boolean[][] hitPixmap; // stores the precomputed alpha map

    private static final Map<String, Pixmap> tilePixmapCache = new HashMap<>(); // saving the pixmap first

    /**
     * Constructs a Tile with the specified texture region.
     *
     * @param textureRegion The texture region for the tile.
     */
    public Tile(TextureRegion textureRegion) {
        super(textureRegion);
        this.tilePosition = null;
        this.hitPixmap = null;
    }

    /**
     * Returns the tile position in tile units.
     *
     * @return The position of the tile.
     * @throws IllegalStateException If the tile position is not initialized.
     */
    public Position getTilePosition() {
        if (tilePosition != null)
            return tilePosition;
        else
            throw new IllegalStateException("Tile position has not been initialized");
    }

    /**
     * Sets the position of the tile in tile units and updates the hitbox.
     *
     * @param tilePosition The new position of the tile.
     */
    public void setTilePosition(Position tilePosition) {
        this.tilePosition = tilePosition.convertTo(Position.PositionUnit.TILES);

        // Set Hitbox upon updating its position
        tilePosition = tilePosition.convertTo(Position.PositionUnit.PIXELS);
        worldX = tilePosition.getX() - (float) TILE_SCREEN_SIZE / 2;
        worldY = tilePosition.getY() - TILE_SCREEN_SIZE / 2.0f;

        setHitPixmap();
    }

    /**
     * Prints the specified hit pixel map to the console for debugging purposes.
     *
     * @param hitPixmap The hit pixel map to print.
     */
    public static void printHitPixmap(boolean[][] hitPixmap) {
        int width = hitPixmap[0].length;
        int height = hitPixmap.length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(hitPixmap[x][y] ? "１" : "０");
            }
            System.out.println();
        }
    }


    /**
     * Returns the x-coordinate of the tile in tile units.
     * It simplifies the length of the method getting tileX
     *
     * @return The x-coordinate of the tile.
     */
    public int getTileX(){
        return this.tilePosition.getTileX();
    }


    /**
     * Returns the y-coordinate of the tile in tile units.
     * It simplifies the length of the method getting tileY
     *
     * @return The y-coordinate of the tile.
     */
    public int getTileY(){
        return this.tilePosition.getTileY();
    }

    /**
     * Sets the hit pixel map based on the texture region.
     */
    protected void setHitPixmap() {
        Pixmap pixmap = getTilePixmap(this.getTextureRegion());
        hitPixmap = createHitPixmap(this.getTextureRegion(), pixmap);
    }

    /**
     * Creates a hit pixel map based on the texture region and pixmap.
     * The alpha calculation is performed here
     *
     * @param textureRegion The texture region of the tile.
     * @param tilePixmap    The pixmap of the tile.
     * @return A 2D boolean array representing the hit pixel map.
     */
    public static boolean[][] createHitPixmap(TextureRegion textureRegion, Pixmap tilePixmap) {
        int startX = textureRegion.getRegionX();
        int startY = textureRegion.getRegionY();
        int width = textureRegion.getRegionWidth();
        int height = textureRegion.getRegionHeight();

        boolean[][] hitPixmap = new boolean[width][height];

        // Iterate over the pixels in the texture region
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = tilePixmap.getPixel(startX + x, startY + y); // pixel is in RGBA8888 Format, so that alpha is the LSB
                int alpha = (pixel & 0x000000FF); // int alpha = (pixel & 0xFF000000) >>> 24; // Extract the alpha channel

                // Collision detected if (alpha > 150) (max. 255)
                hitPixmap[x][y] = (alpha > 150);
            }
        }

        return hitPixmap;
    }

    /**
     * Precomputes and caches the Pixmap for the given tile region.
     *
     * @param tileRegion The {@link TextureRegion} of the tile.
     * @return The Pixmap of the tile.
     */
    protected Pixmap getTilePixmap(TextureRegion tileRegion) {
        return getPixmap(tileRegion);
    }

    public static Pixmap getPixmap(TextureRegion tileRegion) {
        String key = tileRegion.getTexture().toString() + tileRegion.getRegionX() + tileRegion.getRegionY();
        if (!tilePixmapCache.containsKey(key)) {
            Texture tileTexture = tileRegion.getTexture();
            TextureData textureData = tileTexture.getTextureData();
            if (!textureData.isPrepared()) {
                textureData.prepare();
            }

            Pixmap pixmap = textureData.consumePixmap();
            tilePixmapCache.put(key, pixmap);
        }
        return tilePixmapCache.get(key);
    }

    /**
     * Checks if the specified point collides with the non-transparent part of a tile.
     *
     * @param pointX The world X coordinate of the point.
     * @param pointY The world Y coordinate of the point.
     * @return True if there is a collision, false otherwise.
     */
    public boolean isCollidingPoint(float pointX, float pointY) {
        /*Precomputed Alpha Maps:
        Instead of accessing Pixmap directly during runtime,
        preprocess the tileset and store alpha masks as 2D arrays of booleans
        (true for non-transparent or alpha > 150, false for transparent).
        This avoids expensive Pixmap operations.
         */
        TextureRegion tileRegion = this.getTextureRegion();

        float scale = (float) TILE_SCREEN_SIZE / TILE_SIZE;
        int height = this.getTextureRegion().getRegionHeight(); // in pixels, which is 16 (or TILE_SIZE) in this case

        // Calculate the local tile coordinates (in pixels) relative to the tile's position
        int localX = (int) ((pointX - worldX) / scale);
        int localY = height - (int) ((pointY - worldY) / scale) - 1; // The direction of the y-axis needs to be reversed.
        // In the array, the y-axis is facing down. However, in LibGDX coordinate system, it is facing upward from the bottom-left corner.

        // Check if the point is within the bounds of the tile
        if (localX < 0 || localX >= tileRegion.getRegionWidth() || localY < 0 || localY >= tileRegion.getRegionHeight()) {
            return false; // Point is outside the tile's bounds
        }

        // get that specific bit (boolean)
        // printHitPixmap();
        return hitPixmap[localX][localY]; // true if this pixel is true
    }

    /**
     * Checks if the specified point is in the space of this tile.
     * Compare {@code isCollidingPoint} which only detects the non-transparent part of the tile.
     * <br>
     * Note that this is specifically used to detect the SpeedBoost tile,
     * since it is lacking a hitbox.
     *
     * @param pointX The world X coordinate of the point.
     * @param pointY The world Y coordinate of the point.
     * @return True if there is a collision, false otherwise.
     */
    public boolean isPointInTile(float pointX, float pointY) {
        Position tilePosition = this.getTilePosition();
        tilePosition = tilePosition.convertTo(Position.PositionUnit.PIXELS);
        float x = tilePosition.getX() - (float) TILE_SCREEN_SIZE / 2;
        float y = tilePosition.getY() - TILE_SCREEN_SIZE / 2.0f;
        Rectangle hitbox = new Rectangle(x, y, TILE_SCREEN_SIZE, TILE_SCREEN_SIZE);
        return hitbox.contains(pointX, pointY);
    }
}
