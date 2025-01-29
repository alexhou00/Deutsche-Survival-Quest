package de.tum.cit.fop.maze.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.level.LevelManager;
import de.tum.cit.fop.maze.rendering.SpeechBubble;
import de.tum.cit.fop.maze.tiles.Tile;
import de.tum.cit.fop.maze.tiles.Wall;

import static de.tum.cit.fop.maze.util.Constants.*;
import static java.lang.Math.abs;

/**
 * Represents a DYNAMIC character in the maze game, such as our player or any moving enemy. <br>
 * This class provides basic properties and behaviors for characters,
 * such as position, velocity, dimensions, and lifecycle methods.
 * It is intended to be extended by specific character.
 */
public abstract class Character extends GameObject {
    protected float lives;
    /** velX and velY stand for velocity X and velocity Y, resp.
     * velocities are horizontal/vertical components of the speed vector
     * speed is the overall magnitude of speed
     * */
    protected float velX, velY, speed;

    protected final SpeechBubble speechBubble;

    //protected GameScreen gameScreen;

    protected boolean paused;

    protected final LevelManager levels;

    protected float SPEECH_COOLDOWN_TIME = 5;
    protected float speechCooldown = SPEECH_COOLDOWN_TIME;
    public boolean canSpeak = false;

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
    public Character(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives, LevelManager levels) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.lives = lives;
        this.velX = 0;
        this.velY = 0;
        this.speed = 0;
        this.speechBubble = new SpeechBubble();
        this.levels = levels;
    }

    public enum Direction{
        left,
        right,
        up,
        down
    }

    /**
     * Checks if the player can move to a given position because of the wall blocks.
     * <p>
     * This method checks a grid of points (total of five on each side) within the object's hitbox to ensure that
     * none of these points overlap with an instance of the Wall class. If any of the points within the hitbox
     * intersects a wall, the object cannot move to the specified position.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @return True if the position is valid, false otherwise.
     */
    protected boolean canMoveTo(float x, float y){
        // Points to check along each edge (more points = more accurate but slower)
        int numPointsToCheck = 20;
        // Check top edge
        for (int i = (int) (-getHitboxWidthOnScreen() / 2); i <= getHitboxWidthOnScreen() / 2; i += (int) (getHitboxWidthOnScreen() / numPointsToCheck))
            if (isPointWithinInstanceOf(x, y, i, -getHitboxHeightOnScreen() / 2, Wall.class))
                return false;
        // Check bottom edge
        for (int i = (int) (-getHitboxWidthOnScreen() / 2); i <= getHitboxWidthOnScreen() / 2; i += (int) (getHitboxWidthOnScreen() / numPointsToCheck)) {
            if (isPointWithinInstanceOf(x, y, i, getHitboxHeightOnScreen() / 2, Wall.class))
                return false;
        }
        // Check left edge
        for (int j = (int) (-getHitboxHeightOnScreen() / 2); j <= getHitboxHeightOnScreen() / 2; j += (int) (getHitboxHeightOnScreen() / numPointsToCheck)) {
            if (isPointWithinInstanceOf(x, y, -getHitboxWidthOnScreen() / 2, j, Wall.class))
                return false;
        }
        // Check right edge
        for (int j = (int) (-getHitboxHeightOnScreen() / 2); j <= getHitboxHeightOnScreen() / 2; j += (int) (getHitboxHeightOnScreen() / numPointsToCheck)) {
            if (isPointWithinInstanceOf(x, y, getHitboxWidthOnScreen() / 2, j, Wall.class))
                return false;
        }
        return true;
    }


    /**
     * Checks whether a given point, with an applied offset, is within an instance of a specific class.
     *
     * @param x          The x-coordinate of the point in world space.
     * @param y          The y-coordinate of the point in world space.
     * @param offsetX    The horizontal offset applied to the point.
     * @param offsetY    The vertical offset applied to the point.
     * @param objectClass The class type to check the tile instance against.
     * @return {@code true} if the point is within an instance of the specified class and collides with the tile, {@code false} otherwise.
     */
    protected boolean isPointWithinInstanceOf(float x, float y, float offsetX, float offsetY, Class<?> objectClass) {
        int tileX = (int) ((x + offsetX) / TILE_SCREEN_SIZE);
        int tileY = (int) ((y + offsetY) / TILE_SCREEN_SIZE);
        return isTileInstanceOf(tileX, tileY, objectClass) && levels.getTileOnMap(tileX, tileY).isCollidingPoint(x + offsetX, y + offsetY);
    }

    /**
     * Checks if a tile at a specific position is an instance of a class (e.g., Wall) or not
     *
     * @param tileX  The x-coordinate (in tiles) of the tile.
     * @param tileY  The y-coordinate (in tiles) of the tile.
     * @return True if the tile is that class (e.g., Wall, Exit, etc.), false otherwise.
     */
    protected boolean isTileInstanceOf(int tileX, int tileY, Class<?> objectClass) {
        try {
            if (tileX < horizontalTilesCount && tileY < verticalTilesCount){
                Tile tile = levels.getTileOnMap(tileX, tileY);
                return objectClass.isInstance(tile);
            }
            else
                return false;
        }
        catch (ArrayIndexOutOfBoundsException e){
            Gdx.app.error("Character", e.getMessage());
            return false;
        }
    }


    public void update(float delta){
        speechBubble.update(delta);
        speechCooldown -= delta;
    }

    public void pause(){
        paused = true;
    }

    public void resume(){
        paused = false;
    }

    public abstract void hide();

    public abstract void dispose();

    public float getLives() {
        return lives;
    }

    public void setLives(float lives) {
        this.lives = lives;
    }

    public float getVelX() {
        return velX;
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

    public SpeechBubble getSpeechBubble() {
        return speechBubble;
    }


    /**
     * Displays a normal speech bubble with the given text at the entity's position.
     *
     * @param text  The text to be displayed in the speech bubble.
     * @param batch The {@link SpriteBatch} used for rendering the speech bubble. Must not be {@code null}.
     */
    // Normal Speech Bubble
    public void say(String text, SpriteBatch batch) {
        if ( (batch != null)/*speechCooldown > 0*/){
            speechBubble.render(batch, text, x, y, getHeightOnScreen() / 2, SpeechBubble.BubbleType.NORMAL);
        }

    }
    /**
     * Displays a normal speech bubble with the given text at the entity's position.
     * Supports an optional typewriter effect that gradually reveals the text over time.
     *
     * @param text            The full text to be displayed in the speech bubble.
     * @param batch           The {@link SpriteBatch} used for rendering the speech bubble. Must not be {@code null}.
     * @param typewriterEffect If {@code true}, the text is revealed progressively based on the timer and interval.
     * @param timer           The elapsed time used to determine how much of the text should be revealed.
     * @param interval        The time interval controlling the speed of the typewriter effect.
     */
    // Normal Speech Bubble
    public void say(String text, SpriteBatch batch, boolean typewriterEffect, float timer, float interval) {
        if (typewriterEffect){
            say(text.substring(0, Math.min((int) ((timer/interval)), text.length())), batch);
        }
        else say(text, batch);
    }

    /**
     * Displays a multi-edged speech bubble to represent a loud message (scream) at the entity's position.
     *
     * @param text  The text to be displayed in the speech bubble.
     * @param batch The {@link SpriteBatch} used for rendering the speech bubble. Must not be {@code null}.
     */
    // Multi-edged Speech Bubble for a message out loud
    public void scream(String text, SpriteBatch batch) {
        speechBubble.render(batch, text, x, y, getHeightOnScreen() / 2, SpeechBubble.BubbleType.SCREAM);
    }

    /**
     * Displays a cloud-shaped speech bubble to represent a thought at the entity's position.
     *
     * @param text  The text to be displayed in the thought bubble.
     * @param batch The {@link SpriteBatch} used for rendering the thought bubble. Must not be {@code null}.
     */
    // Cloud-shaped Speech Bubble for thoughts
    public void think(String text, SpriteBatch batch) {
        speechBubble.render(batch, text, x, y, getHeightOnScreen() / 2, SpeechBubble.BubbleType.THOUGHT);
    }

    /**
     * Calculates the bounce velocity after a collision, assuming an elastic collision with an infinitely massive object.
     *
     * @param v1 The initial velocity before the bounce.
     * @return The velocity after the bounce. If the initial velocity is greater than 50 in magnitude,
     *         it is simply negated; otherwise, it is negated and multiplied by 5 to enhance the bounce effect.
     */
    public static float bounceVelocity(float v1){
        // assuming m2 is Infinite and the collision is elastic
        return (abs(v1) > 50) ? -v1 : -v1*5; // if not fast enough, times 5
    }

    /**
     * Calculates the bounce velocity after an elastic collision between two objects of equal mass.
     *
     * @param v1 The velocity of the current object before the collision.
     * @param v2 The velocity of the other object before the collision.
     * @return The velocity after the bounce. If the absolute value of the computed velocity is greater than 50,
     *         it is returned as is; otherwise, it is amplified by a factor of 5 to enhance the bounce effect.
     */
    public static float bounceVelocity(float v1, float v2) {
        // v1 is the velocity of this, v2 is the velocity of the other
        // assuming m1 and m2 are the same mass, and the collision is elastic
        float vc = (v1+v2)/2f;//vc is the mass center
        float vf = 2 * vc - v1;//final velocity
        return (abs(vf) > 50) ? vf : vf*5;//abs = absolute velocity
    }

    /**
     * Applies a bounce effect when this object collides with another {@link GameObject}.
     * If the source object is a {@link Character}, an elastic collision calculation is used
     * considering both objects' velocities. Otherwise, a simple bounce is applied.
     *
     * @param source The {@link GameObject} that this object is bouncing off.
     */
    public void bounceBack(GameObject source){
        if (source instanceof Character){
            velX = bounceVelocity(velX, ((Character) source).getVelX());
            velY = bounceVelocity(velY, ((Character) source).getVelY());
        }
        else {
            velX = bounceVelocity(velX);
            velY = bounceVelocity(velY);
        }
    }
}
