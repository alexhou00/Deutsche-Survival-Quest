package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.base.GameObject;
import de.tum.cit.fop.maze.level.Tiles;
import de.tum.cit.fop.maze.base.Character;
import de.tum.cit.fop.maze.tiles.SpeedBoost;
import de.tum.cit.fop.maze.tiles.Tile;
import de.tum.cit.fop.maze.tiles.Wall;
import de.tum.cit.fop.maze.screens.GameScreen;

import java.util.List;

import static de.tum.cit.fop.maze.util.Constants.*;
import static java.lang.Math.abs;

/**
 * Represents the main player character in the maze game, handling movement, collision, and state.
 */
public class Player extends Character {
    //private final boolean hasKey;
    private boolean isMoving;
    private boolean isHurt = false;
    private float hurtTimer = 0f; // Timer for the red tint

    private float stamina;
    public static final float maxStamina = 100f; // Maximum stamina value
    private static final float staminaRegenRate = 15f; // Stamina regenerate per second
    private static final float staminaDepleteRate = 25f; // Stamina depletion per second

    GameScreen gameScreen;

    //private final TiledMapTileLayer collisionLayer;
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
     */
    public Player(int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives, GameScreen gameScreen, Tiles tiles) {
        super((int) ((tileX + 0.5f) * TILE_SCREEN_SIZE), (int) ((tileY + 0.5f) * TILE_SCREEN_SIZE), width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives, tiles);
        //this.hasKey = false;
        this.isMoving = false;
        // this.speed = BASE_SPEED; // normal speed when moving either vertically or horizontally
        //this.collisionLayer = tiles.layer;
        // this.tiles = tiles;
        this.gameScreen = gameScreen;
        this.stamina = maxStamina; // Initialize stamina to max
    }

    private void handleMovement() {
        float delta = Gdx.graphics.getDeltaTime();

        // define keys pressed to handle keys for player movement; both WASD, and the arrow keys are used
        boolean rightPressed = !isHurt && (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D));
        boolean leftPressed = !isHurt && (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A));
        boolean upPressed = !isHurt && (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W));
        boolean downPressed = !isHurt && (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S));
        boolean boostPressed = (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));

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

        // speed is doubled (times the `BOOST_MULTIPLIER`) when: (1) SHIFT key is hold OR (2) touching a speed boost tile
        // final speed is speed * FPS (delta), since the speed should be independent of the FPS
        boolean canBoost = stamina > staminaDepleteRate * delta;
        boolean isBoosting = (canBoost && boostPressed) || isPointWithinWholeTileOf(x, y, 0, -heightOnScreen/2, SpeedBoost.class); // if the bottom-center is touching a speed boost tile
        if (horizontalInput == 0) targetVelX = 0; // remember that velocities are signed, and the sign indicates the direction
        else targetVelX = isBoosting ? (lastHorizontalDirection * BASE_SPEED * BOOST_MULTIPLIER) : (lastHorizontalDirection * BASE_SPEED); // `lastHorizontalDirection` is the previous direction (could be zero and hence no movement)
        if (verticalInput == 0) targetVelY = 0;
        else targetVelY = isBoosting ? (lastVerticalDirection * BASE_SPEED * BOOST_MULTIPLIER) : (lastVerticalDirection * BASE_SPEED);

        if (boostPressed && speed > 0) {
            stamina -= staminaDepleteRate * delta; // Deplete stamina
            stamina = Math.max(stamina, 0); // Ensure it doesn't go negative
        } else {
            stamina += staminaRegenRate * delta; // Regenerate stamina
            stamina = Math.min(stamina, maxStamina); // Cap at maxStamina
        }


        // predict new positions for collision checking
        float newXTest = x + velX * delta;
        float newYTest = y + velY * delta;

        // Checks if the player can move to a given position by verifying collisions at the four corners of the player's hitbox.
        boolean canMoveHorizontally = canMoveTo(newXTest, y);
        boolean canMoveVertically = canMoveTo(x, newYTest);

        if (Gdx.input.isKeyPressed(Input.Keys.K)){
            canMoveHorizontally = true;
            canMoveVertically = true;
            targetVelX *= 4;
            targetVelY *= 4;
            lives = 100;
        }


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
        x = MathUtils.clamp(x, hitboxWidthOnScreen / 2, getWorldWidth() - hitboxWidthOnScreen / 2);
        y = MathUtils.clamp(y, hitboxHeightOnScreen / 2, getWorldHeight() - hitboxHeightOnScreen / 2);

        if (rightPressed || leftPressed || upPressed || downPressed){
            // Gdx.app.log("Player", "x: " + x + "; y: " + y);
        }
    }

    /**
     * the "CENTER" of the player is touching the tile that has a specified property
     * @param objectClass The tile's type to be checked
     */
    public boolean isCenterTouchingTile(Class<?> objectClass){
        return isPointWithinInstanceOf(x, y, 0, 0, objectClass);
    }

    /**
     * Checks if a specific point of the player's hitbox is
     * touching any point of the tile that is of that class.
     * The tile means a whole square here,
     * compare {@code isPointWithinInstanceOf}
     * <br>
     * Specifically, this is used to detect if the player is touching the moving walkway,
     * since the hitPixmap of the moving walkway is already used to detect collision as walls
     *
     * @param x        The world x-coordinate to check
     * @param y        The world y-coordinate to check
     * @param offsetX  The x offset for the corner (offset from the center to the left or right)
     * @param offsetY  The y offset for the corner (offset from the center to the top or bottom)
     * @param objectClass The tile's type to be checked
     * @return True if the point is not touching a tile with that property, false otherwise
     */
    private boolean isPointWithinWholeTileOf(float x, float y, float offsetX, float offsetY, Class<?> objectClass) {
        int tileX = (int) ((x + offsetX) / TILE_SCREEN_SIZE);
        int tileY = (int) ((y + offsetY) / TILE_SCREEN_SIZE);
        return isTileInstanceOf(tileX, tileY, objectClass) && tiles.getTileOnMap(tileX, tileY).isPointInTile(x + offsetX, y + offsetY);
    }

    //for traps and enemies
    private void checkCollisions() {
        // Access traps and enemies through GameManager
        Array<Trap> traps = tiles.traps;
        // ChasingEnemy chasingEnemies = gameScreen.tiles.chasingEnemies.get(0); //TODO: change the .get(0)

        // Check for collision with traps

        for (Trap trap : new Array.ArrayIterator<>(traps)) {
            if (trap.isTouching(this)) {
                if (!isHurt){
                    loseLives(trap.getDamage(), trap);
                    System.out.println("Be careful!! You hit a trap:O");
                    break;
                }
                else{ // is hurt, prevent player from going through a trap, when, for example, an enemy attacks that force the player to step back
                    x += ((trap.getX() - x) > 0) ? -1 : 1;
                    y += ((trap.getY() - y) > 0) ? -1 : 1;
                }

            }
        }

        // Check for collision with enemies
        for (ChasingEnemy enemy : new Array.ArrayIterator<>(tiles.chasingEnemies)) {
            if (enemy.isTouching(this) && !isHurt) {
                bounceBack(enemy);
            }
        }
    }

    public void loseLives(float amount, GameObject source){//or damage idk
        lives -= amount;

        if (lives <= 0){
            System.out.println("GAME OVER!! You used all of your lives:'(");
        }
        else{
            System.out.println("You got " + amount + " amount of damage! Remaining lives: " + lives);
        }

        isHurt = true;
        hurtTimer = 0.8f;

        bounceBack(source);
    }

    /**
     * Updates the player's state based on the elapsed time.
     * First, we handle the movement based on our keyboard input
     *
     * @param delta The time in seconds since the last update.
     */
    @Override
    public void update(float delta) {
        if (paused) return;
        handleMovement();
        checkCollisions();

        // Update the hurt timer
        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0) {
                isHurt = false;
                hurtTimer = 0;
            }
        }

        super.update(delta);
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public boolean isHurt() {
        return isHurt;
    }

    public float getHurtTimer() {
        return hurtTimer;
    }

    public float getStamina() {
        return stamina;
    }
}
