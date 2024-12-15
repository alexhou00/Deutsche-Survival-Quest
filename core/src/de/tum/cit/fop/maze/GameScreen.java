package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.InputAdapter;

import java.util.HashMap;
import java.util.Map;


/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen extends InputAdapter implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;

    private float sinusInput = 0f;

    float spriteX;
    float spriteY;
    private boolean isMoving;

    private final float spriteWidth = 16;
    private final float spriteHeight = 32;

    float worldWidth = 2000;  // Replace with your actual world width
    float worldHeight = 1500; // Replace with your actual world height

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

        spriteX = camera.position.x;
        spriteY = camera.position.y;

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");


        isMoving = false;

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
        float speed = 240f; // final speed is speed * FPS (delta), since the speed should be independent of the FPS
        float delta = Gdx.graphics.getDeltaTime();

        // define keys pressed
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);

        // handle keys for player movement
        if (rightPressed || leftPressed || upPressed || downPressed) {
            isMoving = true; // to have the player continues with the animation
            if (rightPressed) {
                spriteX += speed * delta;
                // collision with the borders
                if (spriteX > worldWidth - spriteWidth) {
                    spriteX = worldWidth - spriteWidth; // TODO: make the spriteX (and spriteY) in the center of the character instead of left-bottom corner
                }
            }
            if (leftPressed) {
                spriteX -= speed * delta;
                if (spriteX < 0) {
                    spriteX = 0;
                }
            }
            if (upPressed) {
                spriteY += speed * delta;
                if (spriteY > worldHeight - spriteHeight) {
                    spriteY = worldHeight - spriteHeight;
                }
            }
            if (downPressed) {
                spriteY -= speed * delta;
                if (spriteY < 0) {
                    spriteY = 0;
                }
            }
        }
        else{
            isMoving = false; // to have the player stop the animation
        }


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

        /*// Mouse scroll wheel controls
        float scrollAmount = Gdx.input.
        if (scrollAmount != 0) {
            camera.zoom += scrollAmount * 0.1f; // Adjust sensitivity as needed
        }*/

        targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL); // Clamp to avoid extreme zoom level
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
            Map<String, Float> variablesToShow = new HashMap<>();
            variablesToShow.put("spriteX", spriteX);
            variablesToShow.put("spriteY", spriteY);
            variablesToShow.put("camera zoom", camera.zoom);

            int currentLine = 0;
            for (Map.Entry<String, Float> entry : variablesToShow.entrySet()) {
                String varName = entry.getKey();
                float displayedValue = entry.getValue();
                font.draw(game.getSpriteBatch(), String.format("%s: %.2f", varName, displayedValue), windowX + BORDER_OFFSET + camera.position.x, windowY - BORDER_OFFSET + camera.position.y - Y_OFFSET * currentLine);
                currentLine++;
            }

            // font.draw(game.getSpriteBatch(), "spriteX: " + round(spriteX, 2), windowX + BORDER_OFFSET + camera.position.x, -BORDER_OFFSET + windowY + camera.position.y);
            // font.draw(game.getSpriteBatch(), "spriteY: " + round(spriteY, 2), windowX + BORDER_OFFSET + camera.position.x, -30 - BORDER_OFFSET + windowY + camera.position.y);
            // font.draw(game.getSpriteBatch(), "Camera Zoom: " + round(camera.zoom, 2), windowX + BORDER_OFFSET + camera.position.x, -60 - BORDER_OFFSET + windowY + camera.position.y);

            if (isMoving) {  // Character Walking Animation
                // Draw the character next to the text :) / We can reuse sinusInput here
                game.getSpriteBatch().draw(
                        game.getCharacterDownAnimation().getKeyFrame(sinusInput, true),
                        spriteX,
                        spriteY,
                        64,
                        128
                ); // width and height are size on the screen
            } else { // Character Idle Animation
                game.getSpriteBatch().draw(
                        game.getCharacterIdleAnimation().getKeyFrame(sinusInput, true),
                        spriteX,
                        spriteY,
                        64,
                        128
                );
            }

            //float worldWidth = viewport.getWorldWidth();
            //float worldHeight = viewport.getWorldHeight();


            // make sure the camera follows the player
            camera.position.set(spriteX, spriteY, 0); // TODO: make sure the camera follows the center point of the player
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
