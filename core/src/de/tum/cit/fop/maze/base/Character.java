package de.tum.cit.fop.maze.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.level.Tiles;
import de.tum.cit.fop.maze.rendering.SpeechBubble;
import de.tum.cit.fop.maze.screens.GameScreen;
import de.tum.cit.fop.maze.tiles.Tile;
import de.tum.cit.fop.maze.tiles.Wall;

import static de.tum.cit.fop.maze.util.Constants.TILE_SCREEN_SIZE;
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

    private final SpeechBubble speechBubble;

    //protected GameScreen gameScreen;

    protected boolean paused;

    protected final Tiles tiles;

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
    public Character(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives, Tiles tiles) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.lives = lives;
        this.velX = 0;
        this.velY = 0;
        this.speed = 0;
        this.speechBubble = new SpeechBubble();
        this.tiles = tiles;
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
        for (int i = (int) (-hitboxWidthOnScreen / 2); i <= hitboxWidthOnScreen / 2; i += (int) (hitboxWidthOnScreen / numPointsToCheck))
            if (isPointWithinInstanceOf(x, y, i, -hitboxHeightOnScreen / 2, Wall.class))
                return false;
        // Check bottom edge
        for (int i = (int) (-hitboxWidthOnScreen / 2); i <= hitboxWidthOnScreen / 2; i += (int) (hitboxWidthOnScreen / numPointsToCheck)) {
            if (isPointWithinInstanceOf(x, y, i, hitboxHeightOnScreen / 2, Wall.class))
                return false;
        }
        // Check left edge
        for (int j = (int) (-hitboxHeightOnScreen / 2); j <= hitboxHeightOnScreen / 2; j += (int) (hitboxHeightOnScreen / numPointsToCheck)) {
            if (isPointWithinInstanceOf(x, y, -hitboxWidthOnScreen / 2, j, Wall.class))
                return false;
        }
        // Check right edge
        for (int j = (int) (-hitboxHeightOnScreen / 2); j <= hitboxHeightOnScreen / 2; j += (int) (hitboxHeightOnScreen / numPointsToCheck)) {
            if (isPointWithinInstanceOf(x, y, hitboxWidthOnScreen / 2, j, Wall.class))
                return false;
        }
        return true;
    }

    /**
     * Checks if a specific point of the player's hitbox is touching a tile that is of that class.
     *
     * @param x        The world x-coordinate to check
     * @param y        The world y-coordinate to check
     * @param offsetX  The x offset for the corner (offset from the center to the left or right)
     * @param offsetY  The y offset for the corner (offset from the center to the top or bottom)
     * @param objectClass The tile's type to be checked
     * @return True if the point is not touching a tile with that property, false otherwise
     */
    protected boolean isPointWithinInstanceOf(float x, float y, float offsetX, float offsetY, Class<?> objectClass) {
        int tileX = (int) ((x + offsetX) / TILE_SCREEN_SIZE);
        int tileY = (int) ((y + offsetY) / TILE_SCREEN_SIZE);
        // if the tile at position (tileX, tileY) is an instance of the objectClass (e.g., Wall) AND
        // if the point (x+offsetX, y+offsetY) is inside this tile
        //if (isTileInstanceOf(tileX, tileY, SpeedBoost.class) && tiles.getTileOnMap(tileX, tileY).)
        // && tiles.getTileOnMap(tileX, tileY).getHitbox().contains(x+offsetX, y+offsetY)
        /*Gdx.app.log("Player",
                    "Player's " +
                            ((offsetX > 0) ? "right" : "left") + "-" + ((offsetY > 0) ? "upper" : "lower") +
                            " corner collided with tile at position " + tileX + ", " + tileY);*/
        return isTileInstanceOf(tileX, tileY, objectClass) && tiles.getTileOnMap(tileX, tileY).isCollidingPoint(x + offsetX, y + offsetY);
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
            Tile tile = tiles.getTileOnMap(tileX, tileY);
            return objectClass.isInstance(tile);
        }
        catch (ArrayIndexOutOfBoundsException e){
            Gdx.app.error("Player", e.getMessage());
            return false;
        }
    }


    public void update(float delta){
        //speechBubble.update(delta);
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

    // Normal Speech Bubble
    public void say(String text, SpriteBatch batch) {
        speechBubble.render(batch, text, x, y, getHitboxHeightOnScreen() / 2, SpeechBubble.BubbleType.NORMAL);
    }

    // Normal Speech Bubble
    public void say(String text, SpriteBatch batch, boolean typewriterEffect, float timer, float interval) {
        if (typewriterEffect){
            say(text.substring(0, Math.min((int) ((timer/interval)), text.length())), batch);
        }
        else say(text, batch);
    }

    // Multi-edged Speech Bubble for a message out loud
    public void scream(String text, SpriteBatch batch) {
        speechBubble.render(batch, text, x, y, getHitboxHeightOnScreen() / 2, SpeechBubble.BubbleType.SCREAM);
    }

    // Cloud-shaped Speech Bubble for thoughts
    public void think(String text, SpriteBatch batch) {
        speechBubble.render(batch, text, x, y, getHitboxHeightOnScreen() / 2, SpeechBubble.BubbleType.THOUGHT);
    }

    public static float bounceVelocity(float v1){
        // assuming m2 is Infinite and the collision is elastic
        return (abs(v1) > 50) ? -v1 : -v1*5; // if not fast enough, times 5
    }

    public static float bounceVelocity(float v1, float v2) {
        // v1 is the velocity of this, v2 is the velocity of the other
        // assuming m1 and m2 are the same mass, and the collision is elastic
        System.out.println(v1 + " " + v2);
        float vc = (v1+v2)/2f;
        System.out.println("Vc: " + vc);
        float vf = 2 * vc - v1;
        return (abs(vf) > 50) ? vf : vf*5;
    }

    public void bounceBack(GameObject source){
        if (source instanceof Character){
            velX = bounceVelocity(velX, ((Character) source).getVelX());
            velY = bounceVelocity(velY, ((Character) source).getVelY());
        }
        else {
            //isInvulnerable = true;
            //targetVelX = (abs(targetVelX) > 50) ? -targetVelX : -targetVelX*5/*(((targetVelX>0) ? 1 : -1) * -500)*/;
            velX = bounceVelocity(velX)/*(((velX>0) ? 1 : -1) * -500)*/;
            //targetVelY = (abs(targetVelY) > 50) ? -targetVelY : -targetVelY*5/*(((targetVelY>0) ? 1 : -1) * -500)*/;
            velY = bounceVelocity(velY)/*(((velY>0) ? 1 : -1) * -500)*/;
        }
    }
}
