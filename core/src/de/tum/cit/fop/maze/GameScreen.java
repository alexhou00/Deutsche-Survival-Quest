package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.InputAdapter;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.tum.cit.fop.maze.Constants.*;


/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 * By extending InputAdapter, we can override the scroll method to detect mouse scroll (for zooming)
 */
public class GameScreen extends InputAdapter implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private OrthographicCamera hudCamera; // HUD camera

    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer; // For drawing shapes like health bars

    private float sinusInput = 0f;  // work as a timer to create a smooth movement with trig

    // private boolean isMoving; // to see if the player needs the walking animation
    private boolean isMuted;

    // For zooming
    private float targetZoom; // targetZoom stores the intermediate zoom value so that we can zoom smoothly

    private Player player;
    private Tiles tiles;

    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap tiledMap;

    float windowWidth = Gdx.graphics.getWidth();
    float windowHeight = Gdx.graphics.getHeight();

    private ObjectRenderer hudObjectRenderer;

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
        targetZoom = 1.0f; // create a smooth little zooming animation when start (0.8 -> 1.0)

        // Create and configure the HUD camera
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Load textures for HUD
        hudObjectRenderer = new ObjectRenderer("objects.png");

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        isMuted = false;
        shapeRenderer = new ShapeRenderer();

        // Load tiled map
        tiles = new Tiles();
        tiledMap = tiles.loadTiledMap("maps/level-2.properties", Gdx.files.internal("basictiles.png").path(), 40, 40);

        // Set up map renderer
        int horizontalTilesCount = 20; // number of tiles on the width
        TILE_SCREEN_SIZE = WORLD_WIDTH / horizontalTilesCount;
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,  (float) TILE_SCREEN_SIZE / TILE_SIZE); // Scale tiles (20 is the number of tiles of the width // so like unitScale is times how many


        player = new Player(0, 1, 16, 32, 13, 21, 64, 128, 6, false, tiles.layer);

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
        // Handle keys input for zooming
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) { // "+" key
            targetZoom -= 0.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) { // "-" key
            targetZoom += 0.02f;
        }

        targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL); // Clamp to avoid extreme zoom level

        // Handle Mute
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
            float textX = (float) (0 + Math.sin(sinusInput) * 100);
            float textY = (float) (750 + Math.cos(sinusInput) * 100);

            Gdx.input.setInputProcessor(this);

            updateZoom(delta); // Smoothly adjust zoom
            handleInput(); // handle the keys input

            // All the player functionalities are here
            player.update(delta);


            // Set up and begin drawing with the sprite batch
            game.getSpriteBatch().setProjectionMatrix(camera.combined);

            game.getSpriteBatch().begin(); // Important to call this before drawing anything

            game.getSpriteBatch().draw(game.getBackgroundTexture(), 0, 0);

            game.getSpriteBatch().end();

            // mapRenderer use another rendering batch, so we have to end the ones first, render the map, and then begin our spriteBatch again
            mapRenderer.setView(camera);
            mapRenderer.render();

            game.getSpriteBatch().begin();

            // Render the text
            font.draw(game.getSpriteBatch(), "Press ESC to go to menu", textX, textY);

            if (player.isMoving()) {  // Character Walking Animation
                // Draw the character next to the text :) / We can reuse sinusInput here
                game.getSpriteBatch().draw(
                        game.getCharacterDownAnimation().getKeyFrame(sinusInput, true),
                        player.getOriginX(),
                        player.getOriginY(),
                        player.getWidthOnScreen(),
                        player.getHeightOnScreen()
                ); // width and height are size on the screen
            } else { // Character Idle Animation
                game.getSpriteBatch().draw(
                        game.getCharacterIdleAnimation().getKeyFrame(sinusInput, true),
                        player.getOriginX(),
                        player.getOriginY(),
                        player.getWidthOnScreen(),
                        player.getHeightOnScreen()
                );
            }


            // make sure the camera follows the player
            // camera.viewportWidth is the window width; camera.viewportHeight is the window height
            camera.position.set(player.getX(), player.getY(), 0);
            camera.position.x = Math.max(camera.viewportWidth / 2 * camera.zoom,
                    Math.min(WORLD_WIDTH - camera.viewportWidth / 2 * camera.zoom, camera.position.x));
            camera.position.y = Math.max(camera.viewportHeight / 2 * camera.zoom,
                    Math.min(WORLD_HEIGHT - camera.viewportHeight / 2 * camera.zoom, camera.position.y));
            camera.update();
            game.getSpriteBatch().end(); // Important to call this after drawing everything
        }

        renderHUD();
    }

    private void renderHUD() {
        SpriteBatch hudBatch = game.getSpriteBatch();
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();

        font.draw(hudBatch, "This is the HUD", 20, Gdx.graphics.getHeight() - 20);
        font.draw(hudBatch, "Score: " + Math.round(sinusInput), 20f, Gdx.graphics.getHeight() - 50f);
        font.draw(hudBatch, "Lives:", 20f, windowHeight - 80f);

        // Show all the variables in the bottom-left corner here
        // Variables to show, stored in a map (LinkedHashMap preserves the order)
        Map<String, Float> variablesToShow = new LinkedHashMap<>();
        variablesToShow.put("player.x", player.getX());
        variablesToShow.put("player.y", player.getY());
        variablesToShow.put("camera zoom", camera.zoom);
        variablesToShow.put("player.speed", player.getSpeed());

        drawVariables(variablesToShow);

        hudBatch.end();

        // shapeRenderer use another rendering batch, so we have to end the batch first, and start it again
        hudBatch.begin();
        hudObjectRenderer.drawHearts(hudBatch, player.getLives(), 128, windowHeight - 106f, 32, 2);

        /* Health bar
        // Draw health bar
        float healthBarWidth = 180f;
        float healthBarHeight = 20f;
        float healthBarX = 128f;
        float healthBarY = windowHeight - 104f;
        float healthPercentage = (float) player.getLives() / MAX_PLAYER_LIVES;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background bar
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

        // Foreground bar
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth * healthPercentage, healthBarHeight);

        shapeRenderer.end();*/
        hudBatch.end();
    }


    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false);
        hudCamera.setToOrtho(false, width, height); // Adjust HUD camera to new screen size
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
    private void drawVariables(Map<String, Float> variablesToShow) {
        SpriteBatch hudBatch = game.getSpriteBatch();
        // Calculate the window's origin adjusted by the camera's (current) zoom
        float windowX = (-windowWidth / 2) * camera.zoom;
        float windowY = (windowHeight / 2) * camera.zoom;

        final float BORDER_OFFSET = 20;
        final float Y_OFFSET = 30;

        int currentLine = 0;
        for (Map.Entry<String, Float> entry : variablesToShow.entrySet()) {
            String varName = entry.getKey();
            float displayedValue = entry.getValue();
            font.draw(hudBatch, String.format("%s: %.2f", varName, displayedValue), BORDER_OFFSET, BORDER_OFFSET + variablesToShow.size() * Y_OFFSET - Y_OFFSET * currentLine);
            currentLine++;
        }
    }

}
