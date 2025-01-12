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
import de.tum.cit.fop.maze.base.Character;
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

    private Player player = null;

    private static final float DAMAGE_COOLDOWN_TIME = 2.0f; // 2-second cooldown
    private float damageCooldown = 0;

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
    /*public ChasingEnemy(TextureRegion textureRegion, int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight,
                        float widthOnScreen, float heightOnScreen, float lives, GameScreen gameScreen, TiledMapTileLayer collisionLayer, Player player) {
        super((int) ((tileX + 0.5f) * TILE_SCREEN_SIZE), (int) ((tileY + 0.5f) * TILE_SCREEN_SIZE),
                width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives);
        this.collisionLayer = collisionLayer;
        this.targetX = x; // Start at the enemy's initial position
        this.targetY = y;
        this.detectionRadius = 300f; // Default detection radius
        this.isChasing = false;
        this.randomMoveCooldown = RANDOM_MOVE_TIME;
        this.randomTargetX = x; // Initial random target position
        this.randomTargetY = y;
        this.player = player;


        // Load the enemy's texture
        this.enemyTexture = textureRegion; // new Texture("mobs.png"); // Make sure the path matches your assets folder
    }*/

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
        if (isPlayerWithinDetectionRadius(player)) {
            // If the player is within the detection radius, chase the player
            isChasing = true;
            chase(player, delta); // Call the chase method
            Gdx.app.log("Enemy", "Chasing the player");
        } else {
            // If the player is outside the detection radius, move randomly
            isChasing = false;
            randomMoveCooldown -= delta; // Decrease cooldown time
            if (randomMoveCooldown <= 0) {
                // Set a new random target position
                setRandomTarget();
                Gdx.app.log("Enemy", "Reset cooldown");
            }
            moveTowardsTarget(delta); // Gradually move towards the random target
            //Gdx.app.log("Enemy", "Move Randomly");
        }

        // Check for collision between the enemy and the player
        attackPlayer(player); // Check if the enemy touched the player
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
            System.out.println("The enemy touched the player! Player loses 1 life.");
            System.out.println("this.hitbox: " + this.getHitbox());
            System.out.println("player.hitbox: " + player.getHitbox());
        }
    }


    /*private boolean isPlayerWithinDetectionRadius(Player player) {
        // Calculate the distance between the enemy and the player
        float distance = (float) Math.sqrt(Math.pow(player.getX() - x, 2) + Math.pow(player.getY() - y, 2));
        return distance <= detectionRadius; // Return true if the player is within the detection radius
    }*/
    private boolean isPlayerWithinDetectionRadius(Player player) {
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= detectionRadius * detectionRadius;
    }

    /*public void chase(Player player, float delta) {
        // Get the player's position
        float playerX = player.getX();
        float playerY = player.getY();

        // Calculate the distance to the player
        float distanceToPlayer = (float) Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2));

        // If the player is within the detection radius, start chasing
        if (distanceToPlayer <= detectionRadius) {
            targetX = playerX; // Set the target position to the player's position
            targetY = playerY;
            moveTowardsTarget(delta); // Move towards the target (the player)
        }
    }*/
    /**
     * Chase the player by moving towards the player's position.
     * @param player The player object.
     * @param delta The delta time.
     */
    private void chase(Player player, float delta) {
        targetX = player.getX();
        targetY = player.getY();
        if (damageCooldown <= 0) moveTowardsTarget(delta);
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
        float newX = x + velX * delta;
        float newY = y + velY * delta;

        // Check if the enemy can move to the new position (collision detection)
        if (canMoveTo(newX, y)) {
            x = newX; // Move horizontally if no collision
        }
        else setRandomTarget();
        if (canMoveTo(x, newY)) {
            y = newY; // Move vertically if no collision
        }
        else setRandomTarget();

        // Constrain enemy position within the game world boundaries
        x = MathUtils.clamp(x, hitboxWidthOnScreen / 2, getWorldWidth() - hitboxWidthOnScreen / 2);
        y = MathUtils.clamp(y, hitboxHeightOnScreen / 2, getWorldHeight() - hitboxHeightOnScreen / 2);
    }

    /*/**
     * Checks if the enemy can move to a target position.
     * @param targetX The target x-coordinate.
     * @param targetY The target y-coordinate.
     * @return True if the enemy can move to the target position, otherwise false.
     */
    /*
    private boolean canMoveTo(float targetX, float targetY) {
        // Convert the target coordinates to tile coordinates
        int tileX = (int) (targetX / TILE_SCREEN_SIZE);
        int tileY = (int) (targetY / TILE_SCREEN_SIZE);

        // Check if the tile is walkable (assuming 1 is a solid tile and 0 is a walkable tile)
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);

        if (cell != null && cell.getTile() != null) {
            // If the cell is not null, it means there's something at that position.
            // Check if the tile is a solid object that the enemy cannot pass through
            //return cell.getTile().getProperties().containsKey("walkable") &&
                    (Boolean) cell.getTile().getProperties().get("walkable");
            return (cell.getTile() instanceof Wall);
        }

        // If there's no tile at the target position, assume it's walkable
        return true;
    }*/

    private void setRandomTarget() {
        /*randomTargetX = MathUtils.random(hitboxWidthOnScreen / 2, getWorldWidth() - hitboxWidthOnScreen / 2);
        randomTargetY = MathUtils.random(hitboxHeightOnScreen / 2, getWorldHeight() - hitboxHeightOnScreen / 2);
   */
        boolean moveHorizontally = MathUtils.randomBoolean(); // Randomly decide whether to move horizontally or vertically

        if (moveHorizontally) {
            // Horizontal movement: Keep the same y-coordinate, change x-coordinate
            targetX = MathUtils.random(hitboxWidthOnScreen / 2, getWorldWidth() - hitboxWidthOnScreen / 2);
            targetY = y; // Maintain the current y-coordinate
        } else {
            // Vertical movement: Keep the same x-coordinate, change y-coordinate
            targetX = x; // Maintain the current x-coordinate
            targetY = MathUtils.random(hitboxHeightOnScreen / 2, getWorldHeight() - hitboxHeightOnScreen / 2);
        }

        randomMoveCooldown = RANDOM_MOVE_TIME; // Reset cooldown
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
