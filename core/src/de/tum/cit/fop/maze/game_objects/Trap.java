package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.base.GameObject;
import de.tum.cit.fop.maze.tiles.Tile;

import static de.tum.cit.fop.maze.util.Constants.TILE_SCREEN_SIZE;
import static de.tum.cit.fop.maze.tiles.Tile.createHitPixmap;
import static de.tum.cit.fop.maze.tiles.Tile.getPixmap;

public class Trap extends GameObject {
    private final float damage;
    private final TextureRegion trapTexture;

    private boolean[][] hitPixmap; // stores the precomputed alpha map

    /**
     * Constructs a Trap object with the specified parameters.
     *
     * @param textureRegion the texture region representing the appearance of the trap.
     * @param x the x-coordinate of the trap.
     * @param y the y-coordinate of the trap.
     * @param width the width of the trap in world units.
     * @param height the height of the trap in world units.
     * @param hitboxWidth the width of the trap's hitbox in world units.
     * @param hitboxHeight the height of the trap's hitbox in world units.
     * @param widthOnScreen the width of the trap when rendered on the screen.
     * @param heightOnScreen the height of the trap when rendered on the screen.
     * @param damage the amount of damage this trap inflicts on a player.
     */
    public Trap(TextureRegion textureRegion, float x, float y, int width, int height, int hitboxWidth, int hitboxHeight,
                float widthOnScreen, float heightOnScreen, float damage) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.damage = damage; // Optional multiplier
        this.trapTexture = textureRegion; // new TextureRegion(new Texture(Gdx.files.internal("objects.png")),1,165,31,26); // Path to the trap image texture
        setHitPixmap();
    }

    /**
     * Renders the trap using the specified SpriteBatch.
     *
     * @param batch the SpriteBatch used for rendering.
     */
    public void draw(SpriteBatch batch) {
        batch.draw(trapTexture, x - widthOnScreen / 2, y - heightOnScreen / 2, widthOnScreen, heightOnScreen);
    }

    public float getDamage() {
        return damage;
    }

    /**
     * Sets the hit detection pixmap for the trap.
     * This method retrieves the texture region of the trap,
     * creates a pixmap from it, and then processes the pixmap
     * to generate a hit detection mask.
     */
    private void setHitPixmap() {
        Pixmap pixmap = getTilePixmap(this.getTextureRegion());
        hitPixmap = createHitPixmap(this.getTextureRegion(), pixmap);
    }

    private Pixmap getTilePixmap(TextureRegion tileRegion) {
        return getPixmap(tileRegion);
    }

    /**
     * Determines if this object is touching another GameObject using pixel-perfect collision detection.
     *
     * <p>This method overrides the default rectangle overlap detection by incorporating
     * precomputed alpha maps for pixel-perfect collision detection. The method calculates the
     * overlapping region of the hitboxes and checks the alpha values to determine collision.</p>
     *
     * @param object the other GameObject to check collision with.
     * @return {@code true} if this object is touching the specified GameObject, {@code false} otherwise.
     */
    @Override
    public boolean isTouching(GameObject object) {
        /*Precomputed Alpha Maps:
        Instead of accessing Pixmap directly during runtime,
        preprocess the tileset and store alpha masks as 2D arrays of booleans
        (true for non-transparent or alpha > 20, false for transparent).
        This avoids expensive Pixmap operations.
         */
        // Get the hitboxes of both objects
        Rectangle thisHitbox = this.getHitbox();
        Rectangle otherHitbox = object.getHitbox();

        // Check if their hitboxes overlap at first
        if (!thisHitbox.overlaps(otherHitbox)) {
            return false; // No collision if hitboxes don't even intersect
        }

        // start pixel-perfect collision detection
        float scale = (float) TILE_SCREEN_SIZE * 0.8f / 32f; // 32 is the size of the original image
        int height = this.getTextureRegion().getRegionHeight(); // The direction of the y-axis needs to be reversed.

        // Calculate the overlapping region of the two hitboxes
        int startX = Math.max((int) thisHitbox.x, (int) otherHitbox.x);
        int startY = Math.max((int) thisHitbox.y, (int) otherHitbox.y/* - 12*/); // IDK why -12, it'd just offset adjust
        int endX = Math.min((int) (thisHitbox.x + thisHitbox.width), (int) (otherHitbox.x + otherHitbox.width));
        int endY = Math.min((int) (thisHitbox.y + thisHitbox.height), (int) (otherHitbox.y/* - 12*/ + otherHitbox.height));

        // Iterate through each pixel in the overlapping region
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                // Convert world coordinates to local coordinates of "this" (the trap)
                int localXThis = (int) ((x - thisHitbox.x) / scale);
                int localYThis = height - (int) ((y - thisHitbox.y) / scale) - 1; // reverse the y position

                // Check the alpha map for "this" (the trap)
                if (this.hitPixmap[localXThis][localYThis]) {
                    // printHitPixmap();
                    return true; // Collision detected because the non-transparent pixel of "this" overlaps with "object"'s hitbox
                }
            }
        }

        // No collision detected
        return false;

    }

    public TextureRegion getTextureRegion () {
        return trapTexture;
    }

}
