package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {

    protected float x, y;
    protected float width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen;
    protected float hitboxWidthOnScreen;
    protected float hitboxHeightOnScreen;
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
        // Actual size of the non-transparent part shown on the screen
        this.hitboxWidthOnScreen = (float) widthOnScreen * hitboxWidth / width;
        this.hitboxHeightOnScreen = (float) heightOnScreen * hitboxHeight / height;

        this.hitbox = null;
    }

    public float getX() {
        return x;
    }

    /**
     * Calculates the x-coordinate of the object's origin (the center of the object).
     *
     * @return The x-coordinate of the origin.
     */
    public float getOriginX(){
        return x - widthOnScreen / 2;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    /**
     * Calculates the y-coordinate of the object's origin (the center of the object).
     *
     * @return The y-coordinate of the origin.
     */
    public float getOriginY(){
        return y - heightOnScreen / 2;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setHitbox(Rectangle hitbox) {
        this.hitbox = hitbox;
    }

    public boolean isCollision(GameObject other) {
        return hitbox.overlaps(other.hitbox);
    }

    public boolean isTouching(GameObject other) {
        return this.getHitbox().overlaps(other.getHitbox());
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
        return width; // Or you can add a scaling factor if needed
    }

    public float getHeightOnScreen() {
        return height; // Or a scaling factor if needed
    }

    public void setWidthOnScreen(float setWidthOnScreen) {
        this.widthOnScreen = setWidthOnScreen;
    }

    // if me make the object move
    public void hitboxPosition(){
        hitbox.setPosition(x, y);
    }

    public Rectangle getHitbox() {
        hitbox = new Rectangle(x - hitboxWidthOnScreen / 2, y - hitboxWidthOnScreen / 2, hitboxWidthOnScreen, hitboxHeightOnScreen);
        return hitbox;
    }
}


