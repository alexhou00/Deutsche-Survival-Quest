package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.base.GameObject;

public class Collectibles extends GameObject {

    public enum Type{
        HEART,
        SPEED_BOOST,
        SHIELD
    }

    private final Type type; // The type of collectible
    private boolean collected; // Whether the collectible has been picked up
    private final TextureRegion textureRegion; // Texture to render the collectible

    /**
     * Constructs a new GameObject instance with specified parameters.
     *
     * @param x              World x-coordinate of the object's initial position (origin is the center of the sprite)
     * @param y              World y-coordinate of the object's initial position. (origin is the center of the sprite)
     * @param width          The width of the object.
     * @param height         The height of the object.
     * @param hitboxWidth    The width of the object's hitbox.
     * @param hitboxHeight   The height of the object's hitbox.
     * @param widthOnScreen  The width of the object as displayed on screen.
     * @param heightOnScreen The height of the object as displayed on screen.
     */
    public Collectibles(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, Type type) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.type = type;
        this.textureRegion = null; //textureRegion;
        this.collected = false;
    }

    /**
     * Checks if the collectible has been picked up.
     *
     * @return True if collected, false otherwise.
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Marks the collectible as collected.
     */
    public void collect() {
        this.collected = true;
    }

    /**
     * Renders the collectible on the screen.
     *
     * @param batch The SpriteBatch used for rendering.
     */
    public void render(SpriteBatch batch, TextureRegion frame) {
        if (!collected) {
            batch.draw(frame, getX() - getWidthOnScreen() / 2, getY() - getHeightOnScreen() / 2, getWidthOnScreen(), getHeightOnScreen());
        }
    }

    /**
     * Gets the type of this collectible.
     *
     * @return The type of collectible.
     */
    public Type getType() {
        return type;
    }

    /**
     * Updates the collectible's state.
     * Could include logic like animations or interactions in the future.
     */
    public void update() {
        // Placeholder for future logic (e.g., animations)
    }
}
