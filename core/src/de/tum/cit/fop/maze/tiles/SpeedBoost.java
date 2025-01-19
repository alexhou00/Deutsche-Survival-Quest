package de.tum.cit.fop.maze.tiles;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents a speed boost tile, which is a specialized type of wall tile.
 * It may contain non-through regions, so it is a child class of Wall
 * Speed boost tiles allow the player to move faster when touching them.
 */
public class SpeedBoost extends Wall {
    public SpeedBoost(TextureRegion textureRegion) {
        super(textureRegion);
    }

    /**
     * Overrides the hit map generation for speed boost tiles.
     * The hit map is customized to include specific regions of the tile that should be treated as collidable.
     */
    @Override
    protected void setHitPixmap() {
        Pixmap pixmap = getTilePixmap(this.getTextureRegion());
        hitPixmap = createHitPixmapForSpeedBoost(this.getTextureRegion(), pixmap);
    }

    /**
     * Compare the static method, {@link Tile#createHitPixmap(TextureRegion, Pixmap)}
     * Creates a hit map for the speed boost tile based on the texture's pixel data.
     * A pixel is marked as collidable if it is black enough and has sufficient opacity.
     *
     * @param textureRegion The {@link TextureRegion} of the tile.
     * @param tilePixmap    The {@link Pixmap} representing the pixel data of the tile.
     * @return A 2D boolean array indicating collidable pixels.
     */
    public static boolean[][] createHitPixmapForSpeedBoost(TextureRegion textureRegion, Pixmap tilePixmap) {
        int startX = textureRegion.getRegionX();
        int startY = textureRegion.getRegionY();
        int width = textureRegion.getRegionWidth();
        int height = textureRegion.getRegionHeight();

        boolean[][] hitPixmap = new boolean[width][height];

        // Iterate over the pixels in the texture region
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = tilePixmap.getPixel(startX + x, startY + y);

                // getPixel() returns the pixel color in "RGBA8888" format
                int red = (pixel & 0xFF000000) >>> 24; // mask the MSB, and shift 3 bytes to get R
                int green = (pixel & 0x00FF0000) >>> 16; // mask the 2nd Byte, and shift 3 bytes to get R
                int blue = (pixel & 0x0000FF00) >>> 8; // mask the 3rd Byte, and shift 3 bytes to get R
                int alpha = (pixel & 0x000000FF); // mask the LSB, and  it does not need shifts. alphas are instead extracted by (pixel & 0xFF000000) >>> 24 in ARGB8888 Format

                // Collision detected if (alpha > 150) (max. 255) and is black enough
                // which is the handrail part of the moving walkway
                hitPixmap[x][y] = (red <= 10 && green <= 10 && blue <= 20) && alpha > 150;
            }
        }
        return hitPixmap;
    }
}
