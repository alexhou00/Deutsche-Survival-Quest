package de.tum.cit.fop.maze;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

import static de.tum.cit.fop.maze.Constants.TILE_SCREEN_SIZE;

public class Enemy extends Character {

    private final TiledMapTileLayer collisionLayer;
    private Vector2 targetPosition;
    private float detectionRadius;
    private boolean isChasing;

    private static final float ENEMY_BASE_SPEED = 180f;

    /**
     * Constructs a new Enemy instance with specified parameters.
     *
     * //@param x              World x-coordinate of the character's initial position (origin is the center of the sprite)
     * //@param y              World y-coordinate of the character's initial position. (origin is the center of the sprite)
     * @param width          The width of the character.
     * @param height         The height of the character.
     * @param hitboxWidth    The width of the character's hitbox.
     * @param hitboxHeight   The height of the character's hitbox.
     * @param widthOnScreen  The width of the character as displayed on screen.
     * @param heightOnScreen The height of the character as displayed on screen.
     * @param lives          The number of lives the character starts with.
     */
    public Enemy(int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight,
                            float widthOnScreen, float heightOnScreen, float lives, TiledMapTileLayer collisionLayer) {
        super((int) ((tileX + 0.5f) * TILE_SCREEN_SIZE), (int) ((tileY + 0.5f) * TILE_SCREEN_SIZE),
                width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives);
        this.collisionLayer = collisionLayer;
        this.targetPosition = new Vector2(x, y);
        this.detectionRadius = 300f; // Default detection radius
        this.isChasing = false;
    }


    @Override
    void update(float delta) {

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
}
