package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.base.Character;
import de.tum.cit.fop.maze.base.GameObject;
import de.tum.cit.fop.maze.level.LevelManager;

import java.util.Arrays;
import java.util.Random;

import static de.tum.cit.fop.maze.util.Constants.*;
import static java.lang.Math.abs;

/**
 * Represents an enemy as a DYNAMIC obstacle in the maze game */
public class ChasingEnemy extends Character {

    protected final TiledMapTileLayer collisionLayer;
    protected float targetX, targetY;
    protected float detectionRadius;
    protected boolean isChasing;
    protected final TextureRegion enemyTexture;
    protected final TextureRegion alertSymbolTexture;

    protected static float ENEMY_BASE_SPEED = 180f;// we can change it when we want to

    // Time to wait before the enemy moves randomly again
    protected static final float RANDOM_MOVE_TIME = 6f;
    protected float randomMoveCooldown;
    protected static final float DAMAGE_COOLDOWN_TIME = 2.0f; // 2-second cooldown
    protected float damageCooldown = 0;
    protected static final int MAX_DAMAGE_TIMES = 3;
    protected int damageTimes = 0;
    protected final float ALERT_SHOWING_TIME = 1.5f;
    protected float alertTime = 0;

    protected Direction previousDirection = Direction.down;
    protected float previousVelX = 0, previousVelY = 0;

    protected Player player = null;

    protected final MazeRunnerGame game;

    protected final int enemyIndex;

    protected float speakingElapsedTime; // Tracks time for the speech bubble's state
    protected int speechTextIndex;
    public final float SPEAKING_ACTIVE_DURATION = MathUtils.random(3f, 5f); // Duration for which the portal is active
    protected final float SPEAKING_CYCLE_DURATION = MathUtils.random(10f, 30f); // Total duration of a cycle (inactive + active)

    /**
     * Constructs a new Enemy instance with specified parameters.
     *
     * @param tileX          The initial x-coordinate (in levels) of the character.
     * @param tileY          The initial y-coordinate (in levels) of the character.
     * @param width          The width of the character.
     * @param height         The height of the character.
     * @param hitboxWidth    The width of the character's hitbox.
     * @param hitboxHeight   The height of the character's hitbox.
     * @param widthOnScreen  The width of the character as displayed on screen.
     * @param heightOnScreen The height of the character as displayed on screen.
     * @param lives          The number of lives the character starts with.
     */
    public ChasingEnemy(TextureRegion textureRegion, int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight,
                        float widthOnScreen, float heightOnScreen, float lives, LevelManager levels, MazeRunnerGame game, int enemyIndex) {
        super((int) ((tileX + 0.5f) * TILE_SCREEN_SIZE), (int) ((tileY + 0.5f) * TILE_SCREEN_SIZE),
                width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives, levels);
        this.collisionLayer = levels.layer;
        this.targetX = 0; // Start at the enemy's initial position
        this.targetY = 0;
        setRandomTarget();
        this.detectionRadius = 400f; // Default detection radius
        this.isChasing = false;
        this.randomMoveCooldown = RANDOM_MOVE_TIME;
        this.enemyIndex = enemyIndex;

        this.speakingElapsedTime = 0;

        this.enemyTexture = textureRegion; // Texture("mobs.png"); // Make sure the path matches your assets folder
        this.alertSymbolTexture = new TextureRegion(new Texture(Gdx.files.internal("original/objects.png")), 32, 130, 13, 12);
        this.game = game;
    }

    public void init(Player player) {
        this.player = player;
    }

    /**
     * Updates the object's state and behavior based on the current game context.
     *
     * <p>This method manages the object's movement, collision detection, interaction with
     * the player, and other behaviors, such as chasing the player or moving randomly.</p>
     *
     * @param delta the time in seconds since the last frame.
     */
    @Override
    public void update(float delta) {
        if (paused || (game.getGameScreen() != null && game.getGameScreen().isPaused())) return;

        if (damageCooldown > 0) {
            damageCooldown -= delta;
        }
        updateSpeakingTime(delta);

        setDirection();

        if (game.getGameScreen() != null && game.getGameScreen().isTutorial())
            ENEMY_BASE_SPEED = 100;

        // Check if the player is within the detection radius
        if (isPlayerWithinDetectionRadius(player, detectionRadius) && damageTimes < MAX_DAMAGE_TIMES) {
            // If the player is within the detection radius, chase the player
            if (!isChasing){ // previously, it wasn't chasing
                alertTime = ALERT_SHOWING_TIME; // reset the time that the exclamation mark [!] need to be shown
                if (game.isMuted()){
                    game.getWarningMusic().pause();
                }
                else{
                    game.getWarningMusic().play();
                }
            }

            isChasing = true;
            chase(player, delta); // Call the chase method

        } else {
            // If the player is outside the detection radius, move randomly
            if (!isPlayerWithinDetectionRadius(player, detectionRadius)) // if player isn't close enough anymore
                damageTimes = 0; // immediately reset back the times it has damaged the player
            if (isChasing){ // previously, it was chasing
                float dx = player.getX() - x; // dx > 0 means the player is on the right side, < 0 if on the left.
                float dy = player.getY() - y; // same for dy
                // reset the target (randomly), especially, in the opposite direction (quadrant) of the player
                setRandomTarget(((dx > 0) ? 0 : x * 1.5f),
                        ((dy > 0) ? 0 : y * 1.5f),
                        ((dx > 0) ? x * 0.5f : getWorldWidth()),
                        ((dy > 0) ? y * 0.5f : getWorldHeight()));
            }
            isChasing = false;
            randomMoveCooldown -= delta; // Decrease cooldown time
            // damageTimes = 0;
            if (randomMoveCooldown <= 0) {
                // Set a new random target position
                setRandomTarget();
                damageTimes = 0; // reset damage times
                Gdx.app.log("Enemy", "Reset cooldown");
            }
            moveTowardsTarget(delta); // Gradually move towards the random target
        }

        // Check for collision between the enemy and the player
        attackPlayer(player); // Check if the enemy touched the player
        checkCollisions(delta);

        super.update(delta);
    }

    /**
     * Checks if the enemy collides with the player.
     * If a collision is detected, the player loses lives.
     *
     * @param player The player object.
     */
    //like the damage player in trap class
    private void attackPlayer(Player player) {
        if (damageCooldown <=0 && this.isTouching(player)) {
            player.loseLives(1, this);
            //bounceBack(player);
            damageCooldown = DAMAGE_COOLDOWN_TIME; // Reset the cooldown
            damageTimes++;
            Gdx.app.log("Enemy", "Attack! The enemy touched the player! Player loses 1 life.");
        }
    }


    /**
     * Checks if the given player is within the detection radius of this object.
     *
     * <p>The detection is based on the squared Euclidean distance between the player's
     * position and this object's position, compared to the square of the detection radius.
     *
     * @param player the {@link Player} whose position is to be checked
     * @return {@code true} if the player is within the detection radius; {@code false} otherwise
     */
    protected boolean isPlayerWithinDetectionRadius(Player player, float radius) {
// radius is in pixels
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= radius * radius;
    }



    /**
     * Chase the player by moving towards the player's position.
     * @param player The player object.
     * @param delta The delta time.
     */
    protected void chase(Player player, float delta) {

        alertTime -= delta;

        if (handleCooldown(player, delta)) return;

        targetX = player.getX();
        targetY = player.getY();
        moveTowardsTarget(delta);


    }

    /**
     * Handles the cooldown period for the enemy after taking damage. During the cooldown,
     * the enemy either moves away from the player or faces the player, depending on the
     * remaining cooldown time.
     *
     * If the cooldown time is greater than half the cooldown duration, the enemy temporarily
     * moves away from the player. If the cooldown is still active but less than half the time,
     * the enemy faces the player without moving.
     *
     * @param player The player object, used to determine the relative position for movement.
     * @param delta The time in seconds since the last frame, used for movement calculations.
     * @return True if the enemy has completed its action during the cooldown (either moving or facing the player).
     *         False if the enemy is still chasing the player and should continue processing.
     */
    protected boolean handleCooldown(Player player, float delta) {
        if (damageCooldown > DAMAGE_COOLDOWN_TIME * (1 - 1 / 2f)) { // leave for a while (1/2 of the cooldown time)
            // Move away temporarily
            targetX = x + (x - player.getX()) * 5000;
            targetY = y + (y - player.getY()) * 5000;
            Gdx.app.debug("ChasingEnemy", "Going away...");
            moveTowardsTarget(delta);
            Gdx.app.debug("ChasingEnemy", "Moved away from the target.");
            return true; // Stop further processing
        }
        else if (damageCooldown > 0) {
            faceThePlayer();
            return true; // Stop further processing
        }

        return false; // Continue chasing
    }

    /**
     * Move towards the target position (the player).
     */
    protected void moveTowardsTarget(float delta) {
        float dirX = targetX - x;
        float dirY = targetY - y;

        // Normalize the direction vector
        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (distance < ENEMY_BASE_SPEED * delta * 2){ // if already arrived at the target (distance is within the error)
            setRandomTarget();
            return;
        }

        dirX = dirX / distance * 3; // dirX is in [-3, 3], scaling for the input of tanh
        dirY = dirY / distance * 3;

        // Set the velocity towards the target
        velX = (float) (Math.tanh(dirX)) * ENEMY_BASE_SPEED; // tanh is between 1~-1 and preserves the sign. it looks like something like this: ___/‾‾‾
        velY = (float) (Math.tanh(dirY)) * ENEMY_BASE_SPEED;

        // Predict new position
        float newX = x + velX * delta * 2;
        float newY = y + velY * delta * 2;

        // Check if the enemy can move to the new position (collision detection)
        if (canMoveTo(newX, y)) {
            x = x + velX * delta; // Move horizontally if no collision
        }
        else
            setRandomTarget();

        if (canMoveTo(x, newY)) {
            y = y + velY * delta; // Move vertically if no collision
        }
        else
            setRandomTarget();

        // Constrain enemy position within the game world boundaries
        x = MathUtils.clamp(x, getHitboxWidthOnScreen() / 2, getWorldWidth() - getHitboxWidthOnScreen() / 2);
        y = MathUtils.clamp(y, getHitboxHeightOnScreen() / 2, getWorldHeight() - getHitboxHeightOnScreen() / 2);

        setDirection();
    }

    /**
     * Sets the direction of the character based on its velocity (velX, velY).
     * The method updates the previous direction based on the relative magnitudes
     * of the X and Y velocities. It determines the primary direction (horizontal or vertical)
     * and updates the `previousDirection` accordingly. Additionally, it stores the current
     * velocities in `previousVelX` and `previousVelY` for reference.
     *
     * The method checks which velocity (X or Y) is greater in absolute value:
     * - If the X velocity is greater, it sets the direction to either `right` or `left` depending on the sign of `velX`.
     * - If the Y velocity is greater, it sets the direction to either `up` or `down` depending on the sign of `velY`.
     *
     * The `previousDirection` reflects the last movement direction, while `previousVelX` and `previousVelY`
     * store the last known velocities.
     */
    public void setDirection(){
        if (abs(velX) > abs(velY)){
            previousDirection = (velX > 0) ? Direction.right : Direction.left;
        }
        if (abs(velY) > abs(velX)){
            previousDirection = (velY > 0) ? Direction.up : Direction.down;
        }

        previousVelX = velX;
        previousVelY = velY;
    }

    /**
     * Determines if the player can move to the specified position on the game world. (to prevent going through walls when moving)
     * Overriding the "super" 's method to check also for the traps
     */
    @Override
    protected boolean canMoveTo(float x, float y){
        if (isTouchingTraps()) return false;

        if (isTouchingOtherEnemies()) return false;

        return super.canMoveTo(x,y);
    }

    /**
     * Checks and handles collisions between this object and traps or enemies in the game world. <br>
     * Handles reaction to collisions by stepping back
     *
     * @param delta the time in seconds since the last frame.
     */
    //for traps and enemies
    private void checkCollisions(float delta) {
        // Access traps and enemies through GameManager
        Array<Trap> traps = levels.traps;

        // Check for collision with traps
        for (Trap trap : iterate(traps)) {
            if (trap.isTouching(this)) {
                Gdx.app.log("ChasingEnemy", "A chasing enemy has hit a trap :O00");
                // step back to original
                stepBackABit(delta, trap);
            }
        }


        // Check for collision with enemies
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)) {
            if (!enemy.equals(this) && enemy.isTouching(this)) {
                stepBackABit(delta, enemy);
            }
        }
    }

    /**
     * Moves this object slightly away from another game object to prevent overlap.
     *
     * <p>The direction and magnitude of the step-back are determined based on the
     * relative position of the other object and the current velocity of this object.
     * If the other object is to the right or above, the movement is in the opposite direction.
     * The method also ensures the step-back avoids walls or obstacles by checking movement validity.
     *
     * <p>After stepping back, a new random target is set for this object.
     *
     * @param delta the time elapsed since the last frame, used to calculate the movement distance
     * @param other the {@link GameObject} that this object collided with
     */
    private void stepBackABit(float delta, GameObject other) {
        float dx = ((other.getX() - x) > 0) ? -1 * abs(velX) * delta : 1 * abs(velX) * delta; // if trap is on the right then
        float dy = ((other.getY() - y) > 0) ? -1 * abs(velY) * delta : 1 * abs(velY) * delta;
        if (super.canMoveTo(x + dx, y + dy)){ // only detect touching walls, so step back to where there are no walls
            x += dx;
            y += dy;
        }
        setRandomTarget();
    }
    /**
     * Checks if this object is currently touching any traps in the game world.
     *
     * <p>The method iterates through all traps in the game and determines if
     * a collision exists between this object and any trap using the {@code isTouching} method.
     *
     * @return {@code true} if this object is touching at least one trap; {@code false} otherwise
     */
    private boolean isTouchingTraps() {
        for (Trap trap : iterate(levels.traps)) {
            if (trap.isTouching(this)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current enemy is touching any other enemy in the game.
     * It iterates over all the chasing enemies and checks if the current enemy
     * is in contact with another enemy, excluding itself. If a collision is detected,
     * the method logs the event and returns `true`. If no collisions are found, it returns `false`.
     *
     * This method is used to determine if the enemy is in close proximity or overlapping
     * with any other chasing enemies, which may affect the game's behavior (e.g., movement, interactions).
     *
     * @return True if the current enemy is touching another enemy, otherwise false.
     */
    protected boolean isTouchingOtherEnemies(){
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)) {
            if (!enemy.equals(this) && enemy.isTouching(this)) {
                Gdx.app.log("Enemy", "Touching other enemies...");
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a random target position for the object within the specified bounds.
     *
     * @param minX the minimum x-coordinate for the target.
     * @param minY the minimum y-coordinate for the target.
     * @param maxX the maximum x-coordinate for the target.
     * @param maxY the maximum y-coordinate for the target.
     */
    private void setRandomTarget(float minX, float minY, float maxX, float maxY) {
        boolean moveHorizontally = MathUtils.randomBoolean(); // Randomly decide whether to move horizontally or vertically

        if (moveHorizontally) {
            // Horizontal movement: Keep the same y-coordinate, change x-coordinate
            targetX = MathUtils.random(minX, maxX);
            targetY = y; // Maintain the current y-coordinate
        } else {
            // Vertical movement: Keep the same x-coordinate, change y-coordinate
            targetX = x; // Maintain the current x-coordinate
            targetY = MathUtils.random(minY, maxY);
        }

        randomMoveCooldown = RANDOM_MOVE_TIME; // Reset cooldown
    }

    /**
     * Sets a random target position for the object,
     * Overloading method defaulting to the entire screen bounds.
     */
    /**
     * Sets a random target position for this object, defaulting to the entire screen.
     *
     * <p>This method uses the object's dimensions and the world boundaries to determine
     * the range for the random target. It ensures the target position is within the screen,
     * avoiding edges by taking the object's hitbox size into account.
     *
     * <p>This is a convenience method that calls the parameterized {@link #setRandomTarget(float, float, float, float)}
     * with default bounds based on the screen size and object dimensions.
     */
    // overloaded, default to the entire screen
    private void setRandomTarget() {
        setRandomTarget(getHitboxWidthOnScreen() / 2, getHitboxHeightOnScreen() / 2, getWorldWidth() - getHitboxWidthOnScreen() / 2, getWorldHeight() - getHitboxHeightOnScreen() / 2);
    }

    // start from 0, and then 1, 2, 3...
    public int getEnemyIndex() {
        return enemyIndex;
    }

    public Direction getPreviousDirection() {
        return previousDirection;
    }


    /**
     * Method to get the player instance (should be implemented to return a reference to the player).
     */
    private Player getPlayer() {
        // You need to find a way to get the reference to the player (either by passing it in the constructor or getting it globally)
        return player;
    }

    /**
     * Draw the enemy to the screen.
     * @param batch The SpriteBatch used to draw the sprite.
     */
    public void draw(SpriteBatch batch, TextureRegion textureRegion) {
        batch.draw(textureRegion, x - widthOnScreen / 2, y - heightOnScreen / 2, widthOnScreen, heightOnScreen);
        if (alertTime>0 && isChasing) batch.draw(alertSymbolTexture, x - 13 * 2, y + heightOnScreen / 1.5f, 13 * 4, 12 * 4);
    }

    /**
     * Handles the bounce-back behavior of the enemy when a collision occurs.
     * The method adjusts the velocity in both the X and Y directions by calling
     * the `bounceVelocity` method. This simulates the enemy being "bounced back"
     * from the collision, typically reversing its direction or reducing its speed
     * depending on the implementation of `bounceVelocity`.
     *
     * This method is typically used when the enemy collides with an obstacle or another object,
     * and needs to move away or change direction in response to the collision.
     *
     * @param source The object that caused the collision, which may influence the bounce behavior.
     */
    @Override
    public void bounceBack(GameObject source){
        velX = bounceVelocity(velX);
        velY = bounceVelocity(velY);
    }

    /**
     * Makes the enemy face the player based on their relative position.
     * The method calculates the direction vector between the enemy and the player,
     * determining which axis (horizontal or vertical) has the largest difference in position.
     * Based on this, the enemy's previous direction is updated to face the player.
     *
     * If the player is not assigned (`null`), the method returns immediately without doing anything.
     *
     * This method is used to adjust the enemy's facing direction to better align with the player’s position.
     *
     * @see Direction for available directions (right, left, up, down)
     */
    protected void faceThePlayer(){
        if (player == null) {
            return; // No player to face
        }

        Gdx.app.log("ChasingEnemy", "facing the player");

        // Calculate direction vector from enemy to player
        float dx = player.getX() - x;
        float dy = player.getY() - y;

        // Determine the dominant direction
        if (Math.abs(dx) > Math.abs(dy)) {
            // Dominant direction is horizontal
            previousDirection = (dx > 0) ? Direction.right : Direction.left;
        } else {
            // Dominant direction is vertical
            previousDirection = (dy > 0) ? Direction.up : Direction.down;
        }
    }

    /**
     * Updates the speaking state of the enemy based on the elapsed time.
     *
     * This method increments the `speakingElapsedTime` by the delta time, and once it exceeds
     * the speaking cycle duration, it resets the timer and selects a new speech string from a list
     * of available options. The speech string is randomly selected from a set of predefined text
     * associated with the enemy. If there is no text available, the enemy is marked as unable to speak.
     *
     * This method also manages the flag `canSpeak`, which indicates whether the enemy is currently
     * allowed to speak based on the elapsed time and whether the selected speech text is empty.
     *
     * @param delta The time elapsed since the last frame, used to increment `speakingElapsedTime`.
     */
    protected void updateSpeakingTime(float delta){
        speakingElapsedTime += delta;
        boolean emptyString = true;

        if (speakingElapsedTime >= SPEAKING_CYCLE_DURATION) {
            speakingElapsedTime -= SPEAKING_CYCLE_DURATION; // Reset to start a new cycle
            String[] textToSelect = levels.getProperties("speechEnemy" + (getEnemyIndex() + 1)).split("\\|");
            Gdx.app.debug("Enemy Speech", Arrays.toString(textToSelect));
            if (textToSelect.length != 0) emptyString = false;
            speechTextIndex = new Random(System.nanoTime()).nextInt(textToSelect.length);
        }
        canSpeak = speakingElapsedTime < SPEAKING_ACTIVE_DURATION;
        if (emptyString)
            canSpeak = false;
    }

    public String getSpeechText(){ // {levels.getProperties("speechEnemy" + getEnemyIndex() + 1), };
        String[] textToSelect = levels.getProperties("speechEnemy" + (getEnemyIndex() + 1)).split("\\|");
        return textToSelect[speechTextIndex];
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
