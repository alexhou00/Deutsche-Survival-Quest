package de.tum.cit.fop.maze.base;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a generic game object with common properties and behaviors.
 */
public abstract class GameObject {

    protected float x, y;
    protected float width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen;
    protected Rectangle hitbox;

    /**
     * Constructs a new GameObject instance with specified parameters.
     *
     * @param x World x-coordinate of the object's initial position (origin is the center of the sprite)
     * @param y World y-coordinate of the object's initial position. (origin is the center of the sprite)
     * @param width The width of the object.
     * @param height The height of the object.
     * @param hitboxWidth The width of the object's hitbox.
     * @param hitboxHeight The height of the object's hitbox.
     * @param widthOnScreen The width of the object as displayed on screen.
     * @param heightOnScreen The height of the object as displayed on screen.
     */
    public GameObject(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.widthOnScreen = widthOnScreen;
        this.heightOnScreen = heightOnScreen;
        this.hitbox = new Rectangle(x - getHitboxWidthOnScreen() / 2, y - getHitboxHeightOnScreen() / 2, getHitboxWidthOnScreen(), getHitboxHeightOnScreen());
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getHitboxWidth() {
        return hitboxWidth;
    }

    public void setHitboxWidth(float hitboxWidth) {
        this.hitboxWidth = hitboxWidth;
    }

    public float getHitboxHeight() {
        return hitboxHeight;
    }

    public void setHitboxHeight(float hitboxHeight) {
        this.hitboxHeight = hitboxHeight;
    }

    public float getWidthOnScreen() {
        return widthOnScreen;
    }

    public float getHeightOnScreen() {
        return heightOnScreen;
    }

    public float getHitboxWidthOnScreen() {
        return widthOnScreen * hitboxWidth / width;
    }

    public float getHitboxHeightOnScreen() {
        return heightOnScreen * hitboxHeight / height;
    }

    public Rectangle getHitbox() {
        hitbox.set(x - getHitboxWidthOnScreen() / 2, y - getHitboxHeightOnScreen() / 2, getHitboxWidthOnScreen(), getHitboxHeightOnScreen());
        return hitbox;
    }

    /**
     * Calculates the x-coordinate of the character's origin (the center of the character).
     *
     * @return The x-coordinate of the origin.
     */
    public float getOriginX() {
        return x - widthOnScreen / 2;
    }

    /**
     * Calculates the y-coordinate of the character's origin (the center of the character).
     *
     * @return The y-coordinate of the origin.
     */
    public float getOriginY() {
        return y - heightOnScreen / 2;
    }

    /**
     * Checks if this object is touching another {@link GameObject} by determining if their hitboxes overlap.
     *
     * @param other The {@link GameObject} to check for collision with.
     * @return {@code true} if the hitboxes of both objects overlap, indicating a collision; {@code false} otherwise.
     */
    public boolean isTouching(GameObject other) {
        return this.getHitbox().overlaps(other.getHitbox());
    }
}
