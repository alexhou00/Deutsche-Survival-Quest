package de.tum.cit.fop.maze.game_objects;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.base.GameObject;
import de.tum.cit.fop.maze.level.LevelManager;
import de.tum.cit.fop.maze.base.Character;
import de.tum.cit.fop.maze.tiles.SpeedBoost;
import de.tum.cit.fop.maze.screens.GameScreen;

import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.*;
import static java.lang.Math.abs;
/**
 * Represents the main player character in the maze game, handling movement, collision, and state.
 */
public class Player extends Character {

    private boolean isMoving;
    public boolean hasMoved = false;
    public boolean hasReachedExit = false;
    private boolean isHurt = false;
    private float hurtTimer = 0f; // Timer for the red tint

    private int coins = 0;

    private float stamina;
    public static final float maxStamina = 100f; // Maximum stamina value
    private static final float staminaRegenRate = 15f; // Stamina regenerate per second
    private static final float staminaDepleteRate = 25f; // Stamina depletion per second
    private float currentStaminaMultiplier = 1;

    GameScreen gameScreen;
    MazeRunnerGame game;

    //private final TiledMapTileLayer collisionLayer;
    float targetVelX, targetVelY;
    int lastHorizontalDirection = 0, lastVerticalDirection = 0;

    private static float BASE_SPEED = 240f; // normal speed when moving either vertically or horizontally
    private static final float BOOST_MULTIPLIER = 2f; // the speed will be multiplied by this number when the SHIFT key is pressed
    private static final float SMOOTH_FACTOR = 5f; // the lower the value, the smoother it gets (and needs more time to stop)

    private static final float SPEED_THRESHOLD = 5; // a number to determine if the player has stopped moving or not. if lower than this number, it is considered that the player has stopped moving.

    /**
     * Constructor for Player. This is our main character <br>
     *
     * <li> A hitbox is a imaginary, rectangular bounding box that is exactly on our sprite
     *  They are used to determine if two or more game objects are colliding with each other
     *  </li>
     * @param tileX             world x position in levels where the player is initially spawn
     * @param tileY             world y position in levels where the player is initially spawn
     * @param width             the width of the sprite's frame in pixels in the original image file
     * @param height            the height of the sprite's frame in pixels in the original image file
     * @param hitboxWidth       the width of the sprite's hitbox in pixels in the original image file
     * @param hitboxHeight      the height of the sprite's hitbox in pixels in the original image file
     * @param widthOnScreen     the actual size of the sprite (frame) drawn on the screen
     * @param heightOnScreen    the actual size of the sprite on the screen
     * @param lives             Number of lives the player starts with.
     */
    public Player(int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives, GameScreen gameScreen, LevelManager levels) {
        super(getWorldCoordinateInPixels(tileX), getWorldCoordinateInPixels(tileY), width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives, levels);

        this.isMoving = false;
        // this.speed = BASE_SPEED; // normal speed when moving either vertically or horizontally
        //this.collisionLayer = levels.layer;
        // this.levels = levels;
        this.gameScreen = gameScreen;
        this.game = gameScreen.game;
        this.stamina = maxStamina; // Initialize stamina to max
        if (levels.isCameraAngled()) {
            this.hitboxHeight /= 2;
        }

        if (gameScreen.isTutorial())
            BASE_SPEED = 210f;

    }

    /**
     * Sets the player's position on the game world grid while ensuring
     * it remains within valid boundaries and does not enter restricted areas.
     *
     * @param tileX The target x-coordinate (tile-based).
     * @param tileY The target y-coordinate (tile-based).
     */
    public void setPosition(float tileX, float tileY) {
        // Ensure the position is valid and does not cause out-of-bounds issues.
        if (!canMoveTo(tileX, tileY)) {
            Gdx.app.log("Player", "Invalid position: Cannot move to " + tileX + ", " + tileY);
            return; // Prevent setting invalid position.
        }

        // Clamp position to ensure player remains within world boundaries.
        this.x = MathUtils.clamp(tileX, getHitboxWidthOnScreen() / 2, getWorldWidth() - getHitboxWidthOnScreen() / 2);
        this.y = MathUtils.clamp(tileY, getHitboxHeightOnScreen() / 2, getWorldHeight() - getHitboxHeightOnScreen() / 2);

        // Optionally, print debug information
        Gdx.app.log("Player", "Player position updated to: (" + this.x + ", " + this.y + ")");

        // Reset movement-related flags and velocities to prevent inconsistencies.
        velX = 0;
        velY = 0;
        targetVelX = 0;
        targetVelY = 0;

        // If the player is no longer moving, stop animations and movement flags.
        isMoving = false;

        checkCollisions();  // Recheck collisions after the position change
    }

    /**
     * Handles the player's movement, including:
     * <ul>
     *   <li>Reading input from keyboard keys for movement and boosting</li>
     *   <li>Calculating velocities for smooth movement</li>
     *   <li>Managing stamina for boost usage and regeneration</li>
     *   <li>Preventing movement through obstacles based on collision checks</li>
     *   <li>Clamping the player's position within world boundaries</li>
     * </ul>
     * Movement directions are determined based on WASD or arrow keys, and
     * speed adjustments account for boosting or diagonal movement.
     */
    private void handleMovement() {
        float delta = Gdx.graphics.getDeltaTime();
        //Gdx.app.log("player", "running in handle movement");
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
        isMoving = (abs(velX) > SPEED_THRESHOLD || abs(velY) > SPEED_THRESHOLD);  // horizontalInput != 0 || verticalInput != 0;
        if (isMoving && !hasMoved) hasMoved = true;

        // speed is doubled (times the `BOOST_MULTIPLIER`) when: (1) SHIFT key is hold OR (2) touching a speed boost tile
        // final speed is speed * FPS (delta), since the speed should be independent of the FPS
        boolean canBoost = stamina > staminaDepleteRate * delta; //slightly >0
        boolean isBoosting = (canBoost && boostPressed) || isPointWithinWholeTileOf(x, y, 0, -heightOnScreen/2, SpeedBoost.class); // if the bottom-center is touching a speed boost tile
        if (horizontalInput == 0) targetVelX = 0; // remember that velocities are signed, and the sign indicates the direction
        else targetVelX = isBoosting ? (lastHorizontalDirection * BASE_SPEED * BOOST_MULTIPLIER) : (lastHorizontalDirection * BASE_SPEED); // `lastHorizontalDirection` is the previous direction (could be zero and hence no movement)
        if (verticalInput == 0) targetVelY = 0;
        else targetVelY = isBoosting ? (lastVerticalDirection * BASE_SPEED * BOOST_MULTIPLIER) : (lastVerticalDirection * BASE_SPEED);

        if (boostPressed && speed > SPEED_THRESHOLD && !isHurt) { // if SHIFT is pressed and the player is indeed moving, plus if not being restricted in movement (because of the enemy attack)
            stamina -= staminaDepleteRate * delta; // Deplete stamina
            if (stamina <= 0 && !game.getSoundEffectPanting().isPlaying()){
                Gdx.app.log("player", "panting...");
                game.getSoundEffectPanting().play();
            }
            stamina = Math.max(stamina, 0); // Ensure it doesn't go negative

        } else if (!isHurt) { // if the player is being hurt, it doesn't regen either
            if (currentStaminaMultiplier == 1 || stamina < maxStamina) // filter out when stamina multiplied and there's excess stamina (filter out his case)
                stamina += staminaRegenRate * delta; // Regenerate stamina

            stamina = Math.min(stamina, maxStamina * currentStaminaMultiplier); // Cap at maxStamina
        }


        // predict new positions for collision checking
        float newXTest = x + velX * delta;
        float newYTest = y + velY * delta;

        // Checks if the player can move to a given position by verifying collisions at the four corners of the player's hitbox.
        boolean canMoveHorizontally = canMoveTo(newXTest, y);
        boolean canMoveVertically = canMoveTo(x, newYTest);

        handleCheatCode();
        if (isGodMode){
            canMoveHorizontally = true;
            canMoveVertically = true;
            targetVelX *= 5;
            targetVelY *= 5;
        }

        if (canBoost && boostPressed && speed > SPEED_THRESHOLD && !game.isMuted()){
            game.getSoundEffectRunning().setVolume(0.5f);
            game.getSoundEffectRunning().play();
            //game.getSoundEffectRunning().loop();
        }
        else{
            game.getSoundEffectRunning().pause();

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
        if (abs(velX) < SPEED_THRESHOLD) lastHorizontalDirection = 0;
        if (abs(velY) < SPEED_THRESHOLD) lastVerticalDirection = 0;

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
        x = MathUtils.clamp(x, getHitboxWidthOnScreen() / 2, getWorldWidth() - getHitboxWidthOnScreen() / 2);
        y = MathUtils.clamp(y, getHitboxHeightOnScreen() / 2, getWorldHeight() - getHitboxHeightOnScreen() / 2);

        if (rightPressed || leftPressed || upPressed || downPressed){
            // Gdx.app.log("Player", "x: " + x + "; y: " + y);
        }
    }

    /**
     * Determines if the player can move to the specified position on the game world. (to prevent going through walls when moving)
     * Overriding the "super" 's method
     * to make some adjustment for a slightly angled camera view by modifying the y-coordinate to align
     * with the center of the player's lower hitbox.
     *
     * @param x the x-coordinate to check for movement
     * @param y the y-coordinate to check for movement
     * @return {@code true} if the player can move to the specified position; {@code false} otherwise
     */
    @Override
    protected boolean canMoveTo(float x, float y){
        if (levels.isCameraAngled()) // not completely top-down 90° view; instead, it's with a slightly angled view
            y -= getHitboxHeightOnScreen()/2; // hitboxHeight is updated, this is the center of the lower-half of the current hitbox
        return super.canMoveTo(x,y);
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
        return isTileInstanceOf(tileX, tileY, objectClass) && levels.getTileOnMap(tileX, tileY).isPointInTile(x + offsetX, y + offsetY);
    }

    /**
     * Checks for collisions between the player and traps or enemies in the game.
     * Handles the effects of collisions, such as reducing the player's lives,
     * bouncing back from enemies, and adjusting the player's position to prevent
     * unintended movement through traps when already hurt.
     */
    //for traps and enemies
    private void checkCollisions() {
        // Access traps and enemies through GameManager
        Array<Trap> traps = levels.traps;

        // Check for collision with traps
        for (Trap trap : iterate(traps)) {
            if (trap.isTouching(this)) {
                if (!isHurt){
                    loseLives(trap.getDamage(), trap);
                    Gdx.app.log("Player", "Be careful!! You hit a trap:O");
                    break;
                }
                else{ // is hurt, prevent player from going through a trap, when, for example, an enemy attacks that force the player to step back
                    x += ((trap.getX() - x) > 0) ? -1 : 1;
                    y += ((trap.getY() - y) > 0) ? -1 : 1;
                }

            }
        }

        // Check for collision with enemies
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)) {
            if (enemy.isTouching(this) && !isHurt) {
                bounceBack(enemy);
            }
        }
    }

    /**
     * Checks if the player is colliding with any portals and handles teleportation accordingly.
     *
     * @param portals An array of {@link Portal} objects to check for collisions.
     */
    public void checkPortalCollisions(Array<Portal> portals) {
        for (Portal portal : iterate(portals)) {
            if (isTouching(portal)) {
                Gdx.app.log("Player", "Collision detected with portal!");

                if (portal.isActive()) { // Ensure portal is active
                    portal.onPlayerTouch(this); // Let the portal handle the teleportation logic
                    Gdx.app.log("Player", "Player teleported to entrance.");
                    break; // Prevent processing other portals this frame
                } else {
                    Gdx.app.log("Player", "Portal is inactive. No teleportation.");
                }
            }
        }
    }

    private final Array<Integer> cheatCodeSequence = new Array<>();
    private static final int[] CHEAT_CODE = {
            Input.Keys.L, Input.Keys.T, Input.Keys.L, Input.Keys.B,
            Input.Keys.R, Input.Keys.T, Input.Keys.R, Input.Keys.B
    };
    private boolean isGodMode = false;

    private void handleCheatCode(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.L) || Gdx.input.isKeyJustPressed(Input.Keys.T) ||
                Gdx.input.isKeyJustPressed(Input.Keys.B) || Gdx.input.isKeyJustPressed(Input.Keys.R)) {

            cheatCodeSequence.add(Gdx.input.isKeyJustPressed(Input.Keys.L) ? Input.Keys.L :
                    (Gdx.input.isKeyJustPressed(Input.Keys.T) ? Input.Keys.T :
                            (Gdx.input.isKeyJustPressed(Input.Keys.B) ? Input.Keys.B :
                                    Input.Keys.R)));

            // Keep only the last 8 keys
            if (cheatCodeSequence.size > CHEAT_CODE.length) {
                cheatCodeSequence.removeIndex(0);
            }

            // Check if the sequence matches
            if (cheatCodeSequence.size == CHEAT_CODE.length) {
                boolean matched = true;
                for (int i = 0; i < CHEAT_CODE.length; i++) {
                    if (!cheatCodeSequence.get(i).equals(CHEAT_CODE[i])) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    Gdx.app.log("player", "Cheat mode activated!");
                    lives = 999;
                    isGodMode = true;
                }
            }
        }
    }

    /**
     * Reduces the player's lives when they take damage and plays a sound effect if not muted.
     * If the player's lives reach zero, the game ends.
     *
     * @param amount The amount of damage the player takes.
     * @param source The {@link GameObject} that caused the damage.
     */
    public void loseLives(float amount, GameObject source){//or damage idk
        lives -= amount;
        if (!game.isMuted()){
            game.getSoundEffectHurt().play(game.getSoundManager().getVolume());
        }
        else{
            game.getSoundEffectHurt().pause();
        }

        if (lives <= 0){
            Gdx.app.log("Player", "GAME OVER!! You used all of your lives:'(");
        }
        else{
            Gdx.app.log("Player", "You got " + amount + " amount of damage! Remaining lives: " + lives);
        }

        isHurt = true;
        hurtTimer = 0.8f;

        bounceBack(source);
    }

    /**
     * Updates the player's state based on the elapsed time.
     * First, we handle the movement based on our keyboard input
     * Then, we check the player's collision with traps and enemies
     *
     * @param delta The time in seconds since the last update.
     */
    @Override
    public void update(float delta) {
        if (gameScreen == null) return;
        if (gameScreen.isPaused()) return;
        if (gameScreen.isTutorial() && gameScreen.getCurrentTutorialStage() == GameScreen.TutorialStage.ZOOM) return;
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

        super.update(delta); // still need to update everything that a character should
    }

    /**
     * Checks whether this game object is within a specified radius of another game object.
     * Uses the squared distance formula to avoid unnecessary square root calculations.
     *
     * @param object The {@link GameObject} to compare distance with.
     * @param radius The distance threshold to check proximity.
     * @return {@code true} if the object is within the given radius, otherwise {@code false}.
     */
    public boolean isCloseTo(GameObject object, float radius){
        float dx = (object.getX() - x);
        float dy = (object.getY() - y);
        return dx * dx + dy * dy <= radius * radius;
    }

    /**
     * Checks if this game object is within a specified radius of any trap.
     * Returns the first trap found within the radius or {@code null} if no trap is nearby.
     *
     * @param radius The distance threshold to check proximity.
     * @return The first {@link Trap} found within the given radius, or {@code null} if none are close.
     */
    public Trap isCloseToTraps(float radius){
        for (Trap trap : iterate(levels.traps)){
            float dx = (trap.getX() - x);
            float dy = (trap.getY() - y);
            if( dx * dx + dy * dy <= radius * radius){
                return trap;
            }
        }
        return null;
    }

    /**
     * Checks if this game object is within a specified radius of any chasing enemy.
     * Returns the first enemy found within the radius or {@code null} if no enemy is nearby.
     *
     * @param radius The distance threshold to check proximity.
     * @return The first {@link ChasingEnemy} found within the given radius, or {@code null} if none are close.
     */
    public ChasingEnemy isCloseToEnemies(float radius){
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){
            float dx = (enemy.getX() - x);
            float dy = (enemy.getY() - y);
            if (dx * dx + dy * dy <= radius * radius){
                return enemy;
            }
        }
        return null;
    }

    /**
     * Checks if this game object is within a specified radius of any collectible item.
     * Returns the first collectible found within the radius or {@code null} if none are nearby.
     *
     * @param radius The distance threshold to check proximity.
     * @return The first {@link Collectibles} found within the given radius, or {@code null} if none are close.
     */
    public Collectibles isCloseToCollectibles(float radius){
        for (Collectibles collectible : iterate(gameScreen.getCollectibles())){
            float dx = (collectible.getX() - x);
            float dy = (collectible.getY() - y);
            if (dx * dx + dy * dy <= radius * radius){
                return collectible;
            }
        }
        return null;
    }

    /**
     * Checks if this game object is within a specified radius of any active portal.
     * Returns the first active portal found within the radius or {@code null} if none are nearby.
     *
     * @param radius The distance threshold to check proximity.
     * @return The first {@link Portal} found within the given radius and is active, or {@code null} if none are close.
     */
    public Portal isCloseToPortals(float radius){
        for (Portal portal : iterate(gameScreen.getPortals())){
            float dx = (portal.getX() - x);
            float dy = (portal.getY() - y);
            if (portal.isActive() && dx * dx + dy * dy <= radius * radius){
                return portal;
            }
        }
        return null;
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

    public boolean isHurt() {
        return isHurt;
    }

    public float getHurtTimer() {
        return hurtTimer;
    }

    public float getStamina() {
        return stamina;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setStamina(float stamina) {
        this.stamina = stamina;
    }

    public float getCurrentStaminaMultiplier() {
        return currentStaminaMultiplier;
    }

    public void setCurrentStaminaMultiplier(float currentStaminaMultiplier) {
        this.currentStaminaMultiplier = currentStaminaMultiplier;
    }

    @Override
    public Rectangle getHitbox() {
        if (levels.isCameraAngled()){
            // hitbox height is already divided by 2
            return hitbox.set(x - getHitboxWidthOnScreen() / 2, y - getHitboxHeightOnScreen(), getHitboxWidthOnScreen(), getHitboxHeightOnScreen());
        }
        return super.getHitbox();
    }
}
