package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.base.Character;
import de.tum.cit.fop.maze.base.GameObject;
import de.tum.cit.fop.maze.level.Tiles;
import de.tum.cit.fop.maze.screens.GameScreen;
import de.tum.cit.fop.maze.tiles.Wall;

import static de.tum.cit.fop.maze.util.Constants.*;

public class ChasingEnemy extends Character {

    private final TiledMapTileLayer collisionLayer;
    private float targetX, targetY;
    private final float detectionRadius;
    private boolean isChasing;
    private final TextureRegion enemyTexture;

    private static final float ENEMY_BASE_SPEED = 180f;// we can change it when we want to

    // Time to wait before the enemy moves randomly again
    private static final float RANDOM_MOVE_TIME = 6f;
    private float randomMoveCooldown;
    private static final float DAMAGE_COOLDOWN_TIME = 2.0f; // 2-second cooldown
    private float damageCooldown = 0;
    private static final int MAX_DAMAGE_TIMES = 3;
    private int damageTimes = 0;

    private Player player = null;

    /**
     * Constructs a new Enemy instance with specified parameters.
     *
     * @param tileX          The initial x-coordinate (in tiles) of the character.
     * @param tileY          The initial y-coordinate (in tiles) of the character.
     * @param width          The width of the character.
     * @param height         The height of the character.
     * @param hitboxWidth    The width of the character's hitbox.
     * @param hitboxHeight   The height of the character's hitbox.
     * @param widthOnScreen  The width of the character as displayed on screen.
     * @param heightOnScreen The height of the character as displayed on screen.
     * @param lives          The number of lives the character starts with.
     */
    public ChasingEnemy(TextureRegion textureRegion, int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight,
                        float widthOnScreen, float heightOnScreen, float lives, Tiles tiles) {
        super((int) ((tileX + 0.5f) * TILE_SCREEN_SIZE), (int) ((tileY + 0.5f) * TILE_SCREEN_SIZE),
                width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives, tiles);
        this.collisionLayer = tiles.layer;
        this.targetX = 0; // Start at the enemy's initial position
        this.targetY = 0;
        setRandomTarget();
        this.detectionRadius = 400f; // Default detection radius
        this.isChasing = false;
        this.randomMoveCooldown = RANDOM_MOVE_TIME;
        //this.randomTargetX = x; // Initial random target position
        //this.randomTargetY = y;
        //this.player = player;


        // Load the enemy's texture
        this.enemyTexture = textureRegion; // Texture("mobs.png"); // Make sure the path matches your assets folder
    }

    public void init(Player player) {
        this.player = player;
    }


    @Override
    public void update(float delta) {
        if (paused) return; // Check if the game is paused

        if (damageCooldown > 0) {
            damageCooldown -= delta;
        }

        // rectangle.set();
        // Check if the player is within the detection radius
        if (isPlayerWithinDetectionRadius(player) && damageTimes<3) {
            // If the player is within the detection radius, chase the player
            isChasing = true;
            chase(player, delta); // Call the chase method
            Gdx.app.log("Enemy", "Chasing the player");
        } else {
            // If the player is outside the detection radius, move randomly
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
                damageTimes = 0;
                Gdx.app.log("Enemy", "Reset cooldown");
            }
            moveTowardsTarget(delta); // Gradually move towards the random target
            //Gdx.app.log("Enemy", "Move Randomly");
        }

        // Check for collision between the enemy and the player
        attackPlayer(player); // Check if the enemy touched the player
        checkCollisions(delta);
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
            bounceBack(player);
            damageCooldown = DAMAGE_COOLDOWN_TIME; // Reset the cooldown
            damageTimes++;
            System.out.println("The enemy touched the player! Player loses 1 life.");
            System.out.println("this.hitbox: " + this.getHitbox());
            System.out.println("player.hitbox: " + player.getHitbox());
            //stepBack(player);
        }
    }


    private boolean isPlayerWithinDetectionRadius(Player player) {
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= detectionRadius * detectionRadius;
    }


    /**
     * Chase the player by moving towards the player's position.
     * @param player The player object.
     * @param delta The delta time.
     */
    private void chase(Player player, float delta) {
        if (damageCooldown <= 0) {
            /*if ((damageTimes < 3)) {*/
                targetX = player.getX();
                targetY = player.getY();
                moveTowardsTarget(delta);/*
            }
            else { // the other direction
                isChasing = false;
                randomMoveCooldown -= delta;
                targetX = x - (player.getX() - x);
                targetY = y - (player.getY() - y);
                if (randomMoveCooldown <= 0) {
                    float dx = player.getX() - x; // dx > 0 means player is on the right side
                    float dy = player.getY() - y;
                    setRandomTarget(((dx > 0) ? 0 : x),
                                    ((dy > 0) ? 0 : y),
                                    ((dx > 0) ? x : getWorldWidth()),
                                    ((dy > 0) ? y : getWorldHeight()));
                    damageTimes = 0;
                    randomMoveCooldown = RANDOM_MOVE_TIME; // Reset the cooldown
                }
                moveTowardsTarget(delta); // Gradually move towards the random target
            }*/
        }
    }

    /**
     * Move towards the target position (the player).
     */
    private void moveTowardsTarget(float delta) {
        float dirX = targetX - x;
        float dirY = targetY - y;

        // Normalize the direction vector
        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (distance < ENEMY_BASE_SPEED * delta * 2) return;
        dirX /= distance;
        dirY /= distance;

        // Set the velocity towards the target
        velX = (float) (Math.tanh(dirX)) * ENEMY_BASE_SPEED; // tanh is between 1~-1 and preserves the sign. it looks like something like this: ___/‾‾‾
        velY = (float) (Math.tanh(dirY)) * ENEMY_BASE_SPEED;
        //Gdx.app.log("Enemy Move", "velocity: " + velocityX + ", " + velocityY);
        // Predict new position
        float newX = x + velX * delta * 2;
        float newY = y + velY * delta * 2;

        // Check if the enemy can move to the new position (collision detection)
        if (canMoveTo(newX, y)) {
            x = x + velX * delta; // Move horizontally if no collision
        }
        else setRandomTarget();
        if (canMoveTo(x, newY)) {
            y = y + velY * delta; // Move vertically if no collision
        }
        else setRandomTarget();

        // Constrain enemy position within the game world boundaries
        x = MathUtils.clamp(x, hitboxWidthOnScreen / 2, getWorldWidth() - hitboxWidthOnScreen / 2);
        y = MathUtils.clamp(y, hitboxHeightOnScreen / 2, getWorldHeight() - hitboxHeightOnScreen / 2);
    }

    @Override
    protected boolean canMoveTo(float x, float y){
        /*if (MathUtils.clamp(x, hitboxWidthOnScreen / 2 + 1 , getWorldWidth() - hitboxWidthOnScreen / 2) - 1 != x ||
                MathUtils.clamp(y, hitboxHeightOnScreen / 2 + 1, getWorldHeight() - hitboxHeightOnScreen / 2 - 1) != y){
            return false;
        }*/
        if (isTouchingTraps()) return false;
        return super.canMoveTo(x,y);
    }

    //for traps and enemies
    private void checkCollisions(float delta) {
        // Access traps and enemies through GameManager
        Array<Trap> traps = tiles.traps;
        // ChasingEnemy chasingEnemies = gameScreen.tiles.chasingEnemies.get(0); //TODO: change the .get(0)

        // Check for collision with traps

        for (Trap trap : new Array.ArrayIterator<>(traps)) {
            if (trap.isTouching(this)) {
                System.out.println("A chasing enemy has hit a trap :O00");
                // step back to original
                float dx = ((trap.getX() - x) > 0) ? -1 * velX * delta : 1 * velX * delta;
                float dy = ((trap.getY() - y) > 0) ? -1 * velY * delta : 1 * velY * delta;
                if (super.canMoveTo(x + dx, y + dy)){ // only detect touching walls, so step back to where there are no walls
                    x += dx;
                    y += dy;
                }
                setRandomTarget();
            }
        }


        // Check for collision with enemies
        /*for (ChasingEnemy enemy : chasingEnemies) {
            if (enemy.isTouching(this)) {
                enemy.checkPlayerCollision(this);
            }
        }*/
    }

    private boolean isTouchingTraps() {
        for (Trap trap : new Array.ArrayIterator<>(tiles.traps)) {
            if (trap.isTouching(this)) {
                return true;
            }
        }
        return false;
    }

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

    // overloaded
    private void setRandomTarget() {
        setRandomTarget(hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2, getWorldWidth() - hitboxWidthOnScreen / 2, getWorldHeight() - hitboxHeightOnScreen / 2);
    }

    protected boolean isTouchingTrap() {
        for (Trap trap : new Array.ArrayIterator<>(tiles.traps)) {
            if (trap.isTouching(this)) {
                return true;
            }
        }
        return false;
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
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
