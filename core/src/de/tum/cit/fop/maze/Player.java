package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import static de.tum.cit.fop.maze.Constants.*;

public class Player extends Character {
    private boolean hasKey;
    private boolean isMoving;

    /**
     * Constructor for Player. This is our main character
     *
     * @param x world x of the sprite (origin is the center of the sprite)
     * @param y world y of the sprite (origin is the center of the sprite)
     * @param width the width of the sprite's frame in pixels in the original image file
     * @param height the height of the sprite's frame in pixels in the original image file
     * @param hitboxWidth the width of the sprite's non-transparent part (=hitbox) in pixels in the original image file
     * @param hitboxHeight the height of the sprite's non-transparent part (=hitbox) in pixels in the original image file
     * @param widthOnScreen the actual size of the sprite drawn on the screen
     * @param heightOnScreen the actual size of the sprite on the screen
     */
    public Player(int x, int y, int width, int height, int hitboxWidth, int hitboxHeight, int widthOnScreen, int heightOnScreen, int lives, boolean hasKey) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives);
        this.hasKey = hasKey;
        this.isMoving = false;
        this.speed = 240f;  // normal speed when moving either vertically or horizontally
    }

    private void handleInput() {
        float speedDiagonal; // moving diagonally should divide the speed by sqrt(2)
        float delta = Gdx.graphics.getDeltaTime();

        // define keys pressed to handle keys for player movement; both WASD and the arrow keys are used
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        int horizontal = (rightPressed ? 1 : 0) - (leftPressed ? 1 : 0); // -1, 0, 1 for left, not moving, right resp.
        int vertical = (upPressed ? 1 : 0) - (downPressed ? 1 : 0); // -1, 0, 1 for down, not moving, up resp.

        // to have the player stop the animation if none of the keys are pressed or continues with the animation otherwise
        isMoving = rightPressed || leftPressed || upPressed || downPressed;

        // speed is doubled when SHIFT is hold
        // final speed is speed * FPS (delta), since the speed should be independent of the FPS
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            speed = 480f;
        }
        else { // otherwise normal speed
            speed = 240f;
        }
        speedDiagonal = speed / 1.414f; // moving diagonally should divide the speed by sqrt(2)

        // change the player's coordinates
        if (horizontal != 0 && vertical != 0) { // both hor. and ver. have speed -> move diagonal
            x += horizontal * speedDiagonal * delta; // horizontal is the direction (could be zero and hence no movement)
            y += vertical * speedDiagonal * delta;
        }
        else{ // move vertically or horizontally
            x += horizontal * speed * delta;
            y += vertical * speed * delta;
        }

        // Actual size of the non-transparent part shown on the screen
        float hitboxWidthOnScreen = widthOnScreen * hitboxWidth / width;
        float hitboxHeightOnScreen = heightOnScreen * hitboxHeight / height;
        // collision with the borders
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
            Gdx.app.log("Player", "x: " + x + "; y: " + y);
        }

        /*
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }*/

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
