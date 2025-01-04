package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a generic character in the maze game, such as our player or any moving enemy. <br>
 * This class provides basic properties and behaviors for characters,
 * such as position, velocity, dimensions, and lifecycle methods.
 * It is intended to be extended by specific character.
 */
public abstract class Character {
    protected float lives;
    /** velX and velY stand for velocity X and velocity Y, resp.
     * velocities are horizontal/vertical components of the speed vector
     * speed is the overall magnitude of speed
     * */
    protected float x, y, velX, velY, speed;
    protected float width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen;
    /** The width/height of the visible hitbox on the screen. */
    protected float hitboxWidthOnScreen;
    protected float hitboxHeightOnScreen;
    protected Rectangle hitbox;


    /**
     * Constructs a new Character instance with specified parameters.
     *
     * @param x World x-coordinate of the character's initial position (origin is the center of the sprite)
     * @param y World y-coordinate of the character's initial position. (origin is the center of the sprite)
     * @param width The width of the character.
     * @param height The height of the character.
     * @param hitboxWidth The width of the character's hitbox.
     * @param hitboxHeight The height of the character's hitbox.
     * @param widthOnScreen The width of the character as displayed on screen.
     * @param heightOnScreen The height of the character as displayed on screen.
     * @param lives The number of lives the character starts with.
     */
    public Character(int x, int y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives) {
        this.lives = lives;
        this.velX = 0;
        this.velY = 0;
        this.speed = 0;
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

    abstract void update(float delta);

    abstract void pause();

    abstract void resume();

    abstract void hide();

    abstract void dispose();

    public float getLives() {
        return lives;
    }
    public void setLives(float lives) {
        this.lives = lives;
    }

    public float getX() {
        return x;
    }

    /**
     * Calculates the x-coordinate of the character's origin (the center of the character).
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
     * Calculates the y-coordinate of the character's origin (the center of the character).
     *
     * @return The y-coordinate of the origin.
     */
    public float getOriginY(){
        return y - heightOnScreen / 2;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
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

    public void setWidthOnScreen(float setWidthOnScreen) {
        this.widthOnScreen = setWidthOnScreen;
    }

    public float getHeightOnScreen() {
        return heightOnScreen;
    }

    public void setHeightOnScreen(float heightOnScreen) {
        this.heightOnScreen = heightOnScreen;
    }

    public Rectangle getHitbox() {
        hitbox = new Rectangle(x - hitboxWidthOnScreen / 2, y - hitboxWidthOnScreen / 2, hitboxWidthOnScreen, hitboxHeightOnScreen);
        return hitbox;
    }

    public boolean isTouching(Character other) {
        return this.getHitbox().overlaps(other.getHitbox());
    }
}
