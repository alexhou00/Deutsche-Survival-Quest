package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;

import static de.tum.cit.fop.maze.Constants.*;
import static java.lang.Math.abs;

/**
 * Represents the main player character in the maze game, handling movement, collision, and state.
 */
public class Player extends Character {
    private boolean hasKey;
    private boolean isMoving;
    private TiledMapTileLayer collisionLayer;
    float targetVelX, targetVelY;
    int lastHorizontalDirection = 0, lastVerticalDirection = 0;

    private static final float BASE_SPEED = 240f; // normal speed when moving either vertically or horizontally
    private static final float BOOST_MULTIPLIER = 2f; // the speed will be multiplied by this number when the SHIFT key is pressed
    private static final float SMOOTH_FACTOR = 5f; // the lower the value, the smoother it gets (and needs more time to stop)

    /**
     * Constructor for Player. This is our main character <br>
     *
     * <li> A hitbox is a imaginary, rectangular bounding box that is exactly on our sprite
     *  They are used to determine if two or more game objects are colliding with each other
     *  </li>
     * @param tileX             world x position in tiles where the player is initially spawn
     * @param tileY             world y position in tiles where the player is initially spawn
     * @param width             the width of the sprite's frame in pixels in the original image file
     * @param height            the height of the sprite's frame in pixels in the original image file
     * @param hitboxWidth       the width of the sprite's hitbox in pixels in the original image file
     * @param hitboxHeight      the height of the sprite's hitbox in pixels in the original image file
     * @param widthOnScreen     the actual size of the sprite (frame) drawn on the screen
     * @param heightOnScreen    the actual size of the sprite on the screen
     * @param lives             Number of lives the player starts with.
     * @param hasKey            Whether the player starts with the key.
     * @param collisionLayer    The layer used for collision detection.
     */
    public Player(int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives, boolean hasKey, TiledMapTileLayer collisionLayer) {
        super((int) ((tileX + 0.5f) * TILE_SCREEN_SIZE), (int) ((tileY + 0.5f) * TILE_SCREEN_SIZE), width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives);
        this.hasKey = hasKey;
        this.isMoving = false;
        // this.speed = BASE_SPEED; // normal speed when moving either vertically or horizontally
        this.collisionLayer = collisionLayer;
    }

    private void handleMovement() {
        float delta = Gdx.graphics.getDeltaTime();

        // define keys pressed to handle keys for player movement; both WASD, and the arrow keys are used
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        boolean boostPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        // Determine movement direction
        // int horizontalInput, verticalInput; they will be -1, 0, or 1 depending on the direction
        int horizontalInput = (rightPressed ? 1 : 0) - (leftPressed ? 1 : 0); // -1, 0, 1 for the left, not moving, right resp.
        int verticalInput = (upPressed ? 1 : 0) - (downPressed ? 1 : 0); // -1, 0, 1 for down, not moving, up resp.

        // update the last direction of movement based on key presses
        if (rightPressed) lastHorizontalDirection = 1;
        if (leftPressed) lastHorizontalDirection = -1;
        if (upPressed) lastVerticalDirection = 1;
        if (downPressed) lastVerticalDirection = -1;

        // to have the player stop the animation if none of the keys are pressed or continues with the animation otherwise
        isMoving = (abs(velX) > 5 || abs(velY) >5);  // horizontalInput != 0 || verticalInput != 0;

        // speed is doubled (times the `BOOST_MULTIPLIER`) when SHIFT key is hold
        // final speed is speed * FPS (delta), since the speed should be independent of the FPS
        if (horizontalInput == 0) targetVelX = 0; // remember that velocities are signed, and the sign indicates the direction
        else targetVelX = boostPressed ? (lastHorizontalDirection * BASE_SPEED * BOOST_MULTIPLIER) : (lastHorizontalDirection * BASE_SPEED); // `lastHorizontalDirection` is the previous direction (could be zero and hence no movement)
        if (verticalInput == 0) targetVelY = 0;
        else targetVelY = boostPressed ? (lastVerticalDirection * BASE_SPEED * BOOST_MULTIPLIER) : (lastVerticalDirection * BASE_SPEED);

        // predict new positions for collision checking
        float newXTest = x + velX * delta;
        float newYTest = y + velY * delta;

        // Checks if the player can move to a given position by verifying collisions at the four corners of the player's hitbox.
        boolean canMoveHorizontally = canMoveTo(newXTest, y);
        boolean canMoveVertically = canMoveTo(x, newYTest);

        // both hor. and ver. are pressed -> move diagonally
        // Adjust speed for diagonal movement (moving diagonally should divide the speed by sqrt(2))
        if (horizontalInput != 0 && verticalInput != 0) {
            if (canMoveVertically) targetVelX /= 1.414f; // but not touching horizontal walls
            if (canMoveHorizontally) targetVelY /= 1.414f; // but not touching vertical walls
        }

        // gradually adjust the actual velocities towards the target velocities for smooth movement
        velX += (targetVelX - velX) * SMOOTH_FACTOR * delta;
        velY += (targetVelY - velY) * SMOOTH_FACTOR * delta;

        // reset last movement direction if the velocity drops below the threshold
        if (abs(velX) < 5) lastHorizontalDirection = 0;
        if (abs(velY) < 5) lastVerticalDirection = 0;

        // update the player's coordinates
        float newX = x + velX * delta; // `lastHorizontalDirection` is the previous direction (could be zero and hence no movement)
        float newY = y + velY * delta;
        speed = (float) Math.sqrt(velX * velX * (canMoveHorizontally ? 1 : 0) // pythagoras theorem
                                + velY * velY * (canMoveVertically ? 1 : 0)); // hor/ver component of the vel is 0 if canMove on that axis is false

        // horizontally
        if (canMoveHorizontally) {
            x = newX; // update position if no collision
        }
        else{
            targetVelX *= 0.5f; // reduce velocity when collides
        }

        // vertically
        if (canMoveVertically) {
            y = newY;
        }
        else{
            targetVelY *= 0.5f;
        }

        // Constrain Player to World Boundaries
        x = MathUtils.clamp(x, hitboxWidthOnScreen / 2, WORLD_WIDTH - hitboxWidthOnScreen / 2);
        y = MathUtils.clamp(y, hitboxHeightOnScreen / 2, WORLD_HEIGHT - hitboxHeightOnScreen / 2);

        if (rightPressed || leftPressed || upPressed || downPressed){
            // Gdx.app.log("Player", "x: " + x + "; y: " + y);
        }
    }

    /**
     * Checks if the player can move to a given position because of the wall blocks.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @return True if the position is valid, false otherwise.
     */
    public boolean canMoveTo(float x, float y){
        String property = "collidable";
        return isNotTouching(x, y, -hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2, property) &&
                isNotTouching(x, y, hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2, property) &&
                isNotTouching(x, y, -hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2, property) &&
                isNotTouching(x, y, hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2, property);
    }

    public boolean isTouching(float x, float y, String property){
        return !isNotTouching(x, y, -hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2, property) &&
                !isNotTouching(x, y, hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2, property) &&
                !isNotTouching(x, y, -hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2, property) &&
                !isNotTouching(x, y, hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2, property);
    }

    /**
     * Checks if a specific corner of the player's hitbox is touching a collidable tile.
     *
     * @param x       The world x-coordinate to check
     * @param y       The world y-coordinate to check
     * @param offsetX The x offset for the corner (offset from the center to the left or right)
     * @param offsetY The y offset for the corner (offset from the center to the top or bottom)
     * @return True if the corner is not touching a collidable tile, false otherwise
     */
    private boolean isNotTouching(float x, float y, float offsetX, float offsetY, String property) {
        int tileX = (int) ((x + offsetX) / TILE_SCREEN_SIZE);
        int tileY = (int) ((y + offsetY) / TILE_SCREEN_SIZE);
        return !isColliding(tileX, tileY, offsetX>0, offsetY>0, property);
    }

    /**
     * Checks if a specific tile is collidable.
     *
     * @param tileX  The x-coordinate (in tiles) of the tile.
     * @param tileY  The y-coordinate (in tiles) of the tile.
     * @param isRight True if checking the right side of the player's hitbox, false if checking the left side.
     * @param isUp    True if checking the upper side of the player's hitbox, false if checking the lower side.
     * @return True if the tile is collidable, false otherwise.
     */
    public boolean isColliding(int tileX, int tileY, boolean isRight, boolean isUp, String property) {
        // Get the cell at the specified tile position
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);

        // Check if the cell exists and if it has the "collidable" property (like walls)
        if (cell != null && cell.getTile() != null) {
            Object collidable = cell.getTile().getProperties().get(property);
            if (collidable != null && collidable.equals(true)) {
                String horizontalDesc = isRight ? "right" : "left";
                String verticalDesc = isUp ? "upper" : "lower";
                Gdx.app.log("Player", "Player's " + verticalDesc + "-" + horizontalDesc + " corner collided with tile at position " + tileX + ", " + tileY);
                return true;
            }
        }
        return false; // No collision by default
    }

    // getters and setters
    public boolean hasKey() {
        return hasKey;
    }
    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    /**
     * Updates the player's state based on the elapsed time.
     * First, we handle the movement based on our keyboard input
     *
     * @param delta The time in seconds since the last update.
     */
    @Override
    void update(float delta) {
        handleMovement();
    }

    @Override
    void pause() {

    }

    @Override
    void resume() {

    }

    @Override
    void hide() {

    }

    @Override
    void dispose() {

    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }
}
