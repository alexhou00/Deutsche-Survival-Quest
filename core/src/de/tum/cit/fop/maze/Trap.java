package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.Map;

import static de.tum.cit.fop.maze.Constants.TILE_SCREEN_SIZE;
import static de.tum.cit.fop.maze.Constants.TILE_SIZE;
import static de.tum.cit.fop.maze.Tile.createHitPixmap;
import static de.tum.cit.fop.maze.Tile.getPixmap;

public class Trap extends GameObject {
    private float damage;
    private TextureRegion trapTexture;

    private boolean[][] hitPixmap; // stores the precomputed alpha map

    public Trap(TextureRegion textureRegion, float x, float y, int width, int height, int hitboxWidth, int hitboxHeight,
                float widthOnScreen, float heightOnScreen, float damage) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.damage = 0.5f * damage; // Optional multiplier
        this.trapTexture = textureRegion; // new TextureRegion(new Texture(Gdx.files.internal("objects.png")),1,165,31,26); // Path to the trap image texture
        setHitPixmap();
    }

    // Render the trap using SpriteBatch
    public void draw(SpriteBatch batch) {
        batch.draw(trapTexture, x - widthOnScreen / 2, y - heightOnScreen / 2, widthOnScreen, heightOnScreen);
        // printHitPixmap();
    }
/*
    // Dispose the texture to avoid memory leaks
    public void dispose() {
        if (trapTexture != null) {

        }
    }*/

    public float getDamage() {
        return damage;
    }

    private void printHitPixmap(){
        Tile.printHitPixmap(hitPixmap);
    }

    private void setHitPixmap() {
        Pixmap pixmap = getTilePixmap(this.getTextureRegion());
        hitPixmap = createHitPixmap(this.getTextureRegion(), pixmap);
    }

    private Pixmap getTilePixmap(TextureRegion tileRegion) {
        return getPixmap(tileRegion);
    }

    @Override
    public boolean isTouching(GameObject object) {
        /*Precomputed Alpha Maps:
        Instead of accessing Pixmap directly during runtime,
        preprocess the tileset and store alpha masks as 2D arrays of booleans
        (true for non-transparent or alpha > 20, false for transparent).
        This avoids expensive Pixmap operations.
         */ // access it throuch hitPixmap
        /*TextureRegion tileRegion = this.getTextureRegion();

        float scale = (float) TILE_SCREEN_SIZE * 0.8f / 32f;

        for (int localX)

            int height = this.getTextureRegion().getRegionHeight(); // in pixels, which is 16 (or TILE_SIZE) in this case

        // Calculate the local tile coordinates (in pixels) relative to the tile's position
        int localX = (int) ((pointX - x) / scale);
        int localY = height - (int) ((pointY - y) / scale) - 1; // The direction of the y-axis needs to be reversed.
        // In the array, the y-axis is facing down. However, in LibGDX coordinate system, it is facing upward from the bottom-left corner.

        // Check if the point is within the bounds of the tile
        if (localX < 0 || localX >= tileRegion.getRegionWidth() || localY < 0 || localY >= tileRegion.getRegionHeight()) {
            return false; // Point is outside the tile's bounds
        }

        // get that specific bit (boolean)
        // printHitPixmap();
        return hitPixmap[localX][localY]; // true if this pixel is true*/

        // Get the hitboxes of both objects
        Rectangle thisHitbox = this.getHitbox();
        Rectangle otherHitbox = object.getHitbox();

        // Check if their hitboxes overlap
        if (!thisHitbox.overlaps(otherHitbox)) {
            return false; // No collision if hitboxes don't intersect
        }

        float scale = (float) TILE_SCREEN_SIZE * 0.8f / 32f;
        int height = this.getTextureRegion().getRegionHeight(); // The direction of the y-axis needs to be reversed.
        //Gdx.app.log("TrapTouch", "height: " + height);

        // Calculate the overlapping region of the two hitboxes
        int startX = Math.max((int) thisHitbox.x, (int) otherHitbox.x);
        int startY = Math.max((int) thisHitbox.y, (int) otherHitbox.y - 12); // IDK why -12, it'd just offset adjust
        int endX = Math.min((int) (thisHitbox.x + thisHitbox.width), (int) (otherHitbox.x + otherHitbox.width));
        int endY = Math.min((int) (thisHitbox.y + thisHitbox.height), (int) (otherHitbox.y - 12 + otherHitbox.height));
        //Gdx.app.log("TrapTouch", startX + " " + startY + " " + endX + " " + endY);

        // Iterate through each pixel in the overlapping region
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                // Convert world coordinates to local coordinates of "this"
                int localXThis = (int) ((x - thisHitbox.x) / scale);
                int localYThis = height - (int) ((y - thisHitbox.y) / scale) - 1;

                // Check the alpha map for "this"
                if (this.hitPixmap[localXThis][localYThis]) {
                    Gdx.app.log("TrapTouch", "localYThis: " + localYThis);
                    printHitPixmap();
                    return true; // Collision detected because the non-transparent pixel of "this" overlaps with "object"'s hitbox
                }
            }
        }

        // No collision detected
        return false;
        //return true;

    }

    public TextureRegion getTextureRegion () {
        return trapTexture;
    }



}
