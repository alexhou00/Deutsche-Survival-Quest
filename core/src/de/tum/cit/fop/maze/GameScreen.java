package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.InputAdapter;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 * By extending InputAdapter, we can override the scroll method to detect mouse scroll (for zooming)
 */
public class GameScreen extends InputAdapter implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;

    private float sinusInput = 0f;  // work as a timer to create a smooth movement with trig

    // some variables for the player character (sprite)
    float spriteX; // world x of the sprite
    float spriteY; // world y of the sprite
    float spriteCenterX; // origin is the center of the sprite (rather than top-bottom corner)
    float spriteCenterY;

    private boolean isMoving; // to see if the player needs the walking animation
    private boolean isMuted;

    private static final float SPRITE_WIDTH = 16; // the width of the sprite's frame in pixels in the original image file
    private static final float SPRITE_HEIGHT = 32; // the height of the sprite's frame in pixels in the original image file
    private static final float SPRITE_HITBOX_WIDTH = 14; // the width of the sprite's non-transparent part (=hitbox) in pixels in the original image file
    private static final float SPRITE_HITBOX_HEIGHT = 22; // the height of the sprite's non-transparent part (=hitbox) in pixels in the original image file
    private static final float SPRITE_SCREEN_WIDTH = 64;  // the actual size of the sprite drawn on the screen
    private static final float SPRITE_SCREEN_HEIGHT = 128;// the actual size of the sprite on the screen

    float worldWidth;
    float worldHeight;

    // For zooming
    private float targetZoom; // targetZoom stores the intermediate zoom value so that we can zoom smoothly
    private static final float ZOOM_SPEED = 0.1f; // Controls how quickly the camera adjusts to the target zoom
    private static final float MIN_ZOOM_LEVEL = 0.8f; // MIN is actually zoom in
    private static final float MAX_ZOOM_LEVEL = 1.5f; // MAX is actually zoom out

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 0.8f;
        targetZoom = 1.0f; // create a smooth little zooming animation when start

        spriteCenterX = camera.position.x;
        spriteCenterY = camera.position.y;

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");


        isMoving = false;
        isMuted = false;

        worldWidth = 2000;
        worldHeight = 1500;

    }

    private void updateZoom(float delta) {
        // Gradually adjust the camera's zoom level towards the target zoom
        camera.zoom += (targetZoom - camera.zoom) * ZOOM_SPEED;
    }


    // scrolling is automatically detected (for zooming)
    @Override
    public boolean scrolled(float amountX, float amountY) {
        targetZoom += amountY * 0.1f; // Adjust sensitivity as needed
        targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL); // Clamp zoom level
        Gdx.app.log("GameScreen", "mouse scrolled to adjust zoom");
        return true; // Return true to indicate the event was handled
    }

    private void handleInput() {
        // final speed is speed * FPS (delta), since the speed should be independent of the FPS
        float speedOrthogonal = 240f; // normal speed when moving either vertically or horizontally
        float speedDiagonal = speedOrthogonal / 1.414f; // moving diagonally should divide the speed by sqrt(2)
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

        // change the player's coordinates
        if (horizontal != 0 && vertical != 0) { // both hor. and ver. have speed -> move diagonal
            spriteCenterX += horizontal * speedDiagonal * delta; // horizontal is the direction (could be zero and hence no movement)
            spriteCenterY += vertical * speedDiagonal * delta;
        }
        else{ // move vertically or horizontally
            spriteCenterX += horizontal * speedOrthogonal * delta;
            spriteCenterY += vertical * speedOrthogonal * delta;
        }

        // Actual size of the non-transparent part shown on the screen
        float SPRITE_HITBOX_SCREEN_WIDTH = SPRITE_SCREEN_WIDTH * SPRITE_HITBOX_WIDTH / SPRITE_WIDTH;
        float SPRITE_HITBOX_SCREEN_HEIGHT = SPRITE_SCREEN_HEIGHT * SPRITE_HITBOX_HEIGHT / SPRITE_HEIGHT;
        // collision with the borders
        if (spriteCenterX > worldWidth - SPRITE_HITBOX_SCREEN_WIDTH / 2) { // Prevent sprite from moving beyond right world boundary
            spriteCenterX = worldWidth - SPRITE_HITBOX_SCREEN_WIDTH / 2;
        }
        if (spriteCenterX < SPRITE_HITBOX_SCREEN_WIDTH / 2) { // left world boundary
            spriteCenterX = SPRITE_HITBOX_SCREEN_WIDTH / 2;
        }
        if (spriteCenterY > worldHeight - SPRITE_HITBOX_SCREEN_HEIGHT / 2) { // top world boundary
            spriteCenterY = worldHeight - SPRITE_HITBOX_SCREEN_HEIGHT / 2;
        }
        if (spriteCenterY < SPRITE_HITBOX_SCREEN_HEIGHT / 2) { // bottom world boundary
            spriteCenterY = SPRITE_HITBOX_SCREEN_HEIGHT / 2;
        }

        // update spriteX and spriteY to render the sprite
        spriteX = spriteCenterX - SPRITE_SCREEN_WIDTH / 2;
        spriteY = spriteCenterY - SPRITE_SCREEN_HEIGHT / 2;


        /*
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }*/


        // Handle keys input for zooming
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) { // "+" key
            targetZoom -= 0.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) { // "-" key
            targetZoom += 0.02f;
        }

        targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL); // Clamp to avoid extreme zoom level

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { // Press 'M' to mute/unmute
            isMuted = !isMuted;
            if (isMuted) {
                game.muteBGM();
            } else {
                game.normalizeBGM();
            }
            Gdx.app.log("GameScreen", "Mute toggled: " + (isMuted ? "ON" : "OFF"));
        }

    }

    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        // Check for escape key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        } else {
            ScreenUtils.clear(0, 0, 0, 1); // Clear the screen
            camera.update(); // Update the camera

            // Move text in a circular path to have an example of a moving object
            sinusInput += delta;  // sinusInput is like `time`, storing the time for animation
            float textX = (float) (1000 + Math.sin(sinusInput) * 100);
            float textY = (float) (750 + Math.cos(sinusInput) * 100);

            Gdx.input.setInputProcessor(this);

            updateZoom(delta); // Smoothly adjust zoom
            handleInput(); // handle the keys input

            // Set up and begin drawing with the sprite batch
            game.getSpriteBatch().setProjectionMatrix(camera.combined);

            game.getSpriteBatch().begin(); // Important to call this before drawing anything

            game.getSpriteBatch().draw(game.getBackgroundTexture(), 0, 0);

            // Render the text
            font.draw(game.getSpriteBatch(), "Press ESC to go to menu", textX, textY);

            float windowWidth = Gdx.graphics.getWidth();
            float windowHeight = Gdx.graphics.getHeight();

            // Show all the variables in the top-left corner here
            // Calculate the window's origin adjusted by the camera's (current) zoom
            float windowX = (-windowWidth / 2) * camera.zoom;
            float windowY = (windowHeight / 2) * camera.zoom;

            final float BORDER_OFFSET = 20;
            final float Y_OFFSET = 30;

            // Variables to show, stored in a map (LinkedHashMap preserves the order)
            Map<String, Float> variablesToShow = new LinkedHashMap<>();
            variablesToShow.put("spriteCenterX", spriteCenterX);
            variablesToShow.put("spriteCenterY", spriteCenterY);
            variablesToShow.put("camera zoom", camera.zoom);

            int currentLine = 0;
            for (Map.Entry<String, Float> entry : variablesToShow.entrySet()) {
                String varName = entry.getKey();
                float displayedValue = entry.getValue();
                font.draw(game.getSpriteBatch(), String.format("%s: %.2f", varName, displayedValue), windowX + BORDER_OFFSET + camera.position.x, windowY - BORDER_OFFSET + camera.position.y - Y_OFFSET * currentLine);
                currentLine++;
            }


            if (isMoving) {  // Character Walking Animation
                // Draw the character next to the text :) / We can reuse sinusInput here
                game.getSpriteBatch().draw(
                        game.getCharacterDownAnimation().getKeyFrame(sinusInput, true),
                        spriteX,
                        spriteY,
                        SPRITE_SCREEN_WIDTH,
                        SPRITE_SCREEN_HEIGHT
                ); // width and height are size on the screen
            } else { // Character Idle Animation
                game.getSpriteBatch().draw(
                        game.getCharacterIdleAnimation().getKeyFrame(sinusInput, true),
                        spriteX,
                        spriteY,
                        SPRITE_SCREEN_WIDTH,
                        SPRITE_SCREEN_HEIGHT
                );
            }




            // make sure the camera follows the player
            // camera.viewportWidth is the window width; camera.viewportHeight is the window height
            camera.position.set(spriteCenterX, spriteCenterY, 0);
            camera.position.x = Math.max(camera.viewportWidth / 2 * camera.zoom,
                    Math.min(worldWidth - camera.viewportWidth / 2 * camera.zoom, camera.position.x));
            camera.position.y = Math.max(camera.viewportHeight / 2 * camera.zoom,
                    Math.min(worldHeight - camera.viewportHeight / 2 * camera.zoom, camera.position.y));
            camera.update();
            game.getSpriteBatch().end(); // Important to call this after drawing everything
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    // Additional methods and logic can be added as needed for the game screen

}
