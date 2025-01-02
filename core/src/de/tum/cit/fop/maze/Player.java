package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import static de.tum.cit.fop.maze.Constants.*;

public class Player extends Character {
    private boolean hasKey;
    private boolean isMoving;
    private TiledMapTileLayer collisionLayer;
    float targetVelX, targetVelY;
    int lastHorizontalDirection = 0, lastVerticalDirection = 0;

    private static final float BASE_SPEED = 240f; // normal speed when moving either vertically or horizontally
    private static final float BOOST_MULTIPLIER = 2f;
    private static final float SMOOTH_FACTOR = 5f;

    /**
     * Constructor for Player. This is our main character
     * x: world x of the sprite (origin is the center of the sprite); y: world y of the sprite (origin is the center of the sprite)
     *
     * @param tileX world x position in tiles where the player is initially spawn
     * @param tileY world y position in tiles where the player is initially spawn
     * @param width the width of the sprite's frame in pixels in the original image file
     * @param height the height of the sprite's frame in pixels in the original image file
     * @param hitboxWidth the width of the sprite's non-transparent part (=hitbox) in pixels in the original image file
     * @param hitboxHeight the height of the sprite's non-transparent part (=hitbox) in pixels in the original image file
     * @param widthOnScreen the actual size of the sprite (frame) drawn on the screen
     * @param heightOnScreen the actual size of the sprite on the screen
     */
    public Player(int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight, int widthOnScreen, int heightOnScreen, int lives, boolean hasKey, TiledMapTileLayer collisionLayer) {
        super((int) ((tileX + 0.5f) * TILE_SCREEN_SIZE), (int) ((tileY + 0.5f) * TILE_SCREEN_SIZE), width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives);
        this.hasKey = hasKey;
        this.isMoving = false;
        // this.speed = BASE_SPEED;  // normal speed when moving either vertically or horizontally
        this.collisionLayer = collisionLayer;
    }

    private void handleInput() {
        float delta = Gdx.graphics.getDeltaTime();

        // define keys pressed to handle keys for player movement; both WASD and the arrow keys are used
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        boolean boostPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        // Determine movement direction
        // int horizontalInput, verticalInput; they will be -1, 0, or 1 depending on the direction
        int horizontalInput = (rightPressed ? 1 : 0) - (leftPressed ? 1 : 0); // -1, 0, 1 for left, not moving, right resp.
        int verticalInput = (upPressed ? 1 : 0) - (downPressed ? 1 : 0); // -1, 0, 1 for down, not moving, up resp.
        // update the last direction of movement based on key presses
        if (rightPressed) lastHorizontalDirection = 1;
        if (leftPressed) lastHorizontalDirection = -1;
        if (upPressed) lastVerticalDirection = 1;
        if (downPressed) lastVerticalDirection = -1;

        // to have the player stop the animation if none of the keys are pressed or continues with the animation otherwise
        isMoving = (velX > 5 || velY >5);  // horizontalInput != 0 || verticalInput != 0;

        // speed is doubled (times the `BOOST_MULTIPLIER`) when SHIFT key is hold
        // final speed is speed * FPS (delta), since the speed should be independent of the FPS
        if (horizontalInput == 0) {
            targetVelX = 0;
        }
        else{
            targetVelX = boostPressed ? BASE_SPEED * BOOST_MULTIPLIER : BASE_SPEED;
        }
        if (verticalInput == 0){
            targetVelY = 0;
        }
        else{
            targetVelY = boostPressed ? BASE_SPEED * BOOST_MULTIPLIER : BASE_SPEED;
        }

        // gradually adjust the actual velocities towards the target velocities for smooth movement
        velX += (targetVelX - velX) * SMOOTH_FACTOR * delta;
        velY += (targetVelY - velY) * SMOOTH_FACTOR * delta;

        // reset last movement direction if the velocity drops below the threshold
        if (velX < 5) lastHorizontalDirection = 0;
        if (velY < 5) lastVerticalDirection = 0;

        // both hor. and ver. have speed -> move diagonal
        // (moving diagonally should divide the speed by sqrt(2))
        float actualVelX = velX, actualVelY = velY;
        if (horizontalInput != 0 && verticalInput != 0){ // move diagonally
            actualVelX = velX / ((float) Math.sqrt(2));
            actualVelY = velY / ((float) Math.sqrt(2));
        }

        // update the player's coordinates
        float newX = x + lastHorizontalDirection * actualVelX * delta; // `lastHorizontalDirection` is the previous direction (could be zero and hence no movement)
        float newY = y + lastVerticalDirection * actualVelY * delta;
        speed = (float) Math.sqrt(actualVelX * actualVelX + actualVelY * actualVelY);


        // Checks if the player can move to a given position by verifying collisions at the four corners of the player's hitbox.

        // horizontally
        if (!checkCollision(newX, y, -hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2) &&
                !checkCollision(newX, y, hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2) &&
                !checkCollision(newX, y, -hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2) &&
                !checkCollision(newX, y, hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2)) {
            x = newX; // update position if no collision
        }
        else{
            targetVelX *= 0.5f; // reduce velocity when collides
        }

        // vertically
        if (!checkCollision(x, newY, -hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2) &&
                !checkCollision(x, newY, hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2) &&
                !checkCollision(x, newY, -hitboxWidthOnScreen / 2, -hitboxHeightOnScreen / 2) &&
                !checkCollision(x, newY, hitboxWidthOnScreen / 2, hitboxHeightOnScreen / 2)) {
            y = newY;
        }
        else{
            targetVelY *= 0.5f;
        }


        // collision with the world boundaries
        if (x > WORLD_WIDTH - hitboxWidthOnScreen / 2) { // Prevent sprite from moving beyond right world boundary
            x = WORLD_WIDTH - hitboxWidthOnScreen / 2;
        }
        if (x < hitboxWidthOnScreen / 2) { // left world boundary
            x = hitboxWidthOnScreen / 2;
        }
        if (y > WORLD_HEIGHT - hitboxHeightOnScreen / 2) { // top world boundary
            y = WORLD_HEIGHT - hitboxHeightOnScreen / 2;
        }
        if (y < hitboxHeightOnScreen / 2) { // bottom world boundary
            y = hitboxHeightOnScreen / 2;
        }

        if (rightPressed || leftPressed || upPressed || downPressed){
            // Gdx.app.log("Player", "x: " + x + "; y: " + y);
        }

        /*
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }*/

    }

    public boolean isColliding(int tileX, int tileY) {
        // Get the cell at the specified tile position
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);

        // Check if the cell exists and if it has the "collidable" property (like walls)
        if (cell != null && cell.getTile() != null) {
            Object collidable = cell.getTile().getProperties().get("collidable");
            if (collidable != null && collidable.equals(true)) {
                Gdx.app.log("Player", "collided tile position " + tileX + ", " + tileY);
                return true;
            }
        }
        return false; // No collision by default
    }

    private boolean checkCollision(float x, float y, float offsetX, float offsetY) {
        int tileX = (int) ((x + offsetX) / TILE_SCREEN_SIZE);
        int tileY = (int) ((y + offsetY) / TILE_SCREEN_SIZE);
        return isColliding(tileX, tileY);
    }

    // getters and setters
    public boolean hasKey() {
        return hasKey;
    }
    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    @Override
    void update(float delta) {
        handleInput();
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
