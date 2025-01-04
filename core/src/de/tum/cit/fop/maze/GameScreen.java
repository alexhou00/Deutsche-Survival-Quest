package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
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
 * The GameScreen class is responsible for rendering the gameplay screen. <br>
 * It handles the game logic and rendering of the game elements. <br>
 * By extending InputAdapter, we can override the scroll method to detect mouse scroll (for zooming)
 */
public class GameScreen extends InputAdapter implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final OrthographicCamera hudCamera; // HUD camera. HUD uses another camera so that it does not follow the player and is fixed on the screen.

    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer; // For drawing shapes like health bars

    private float sinusInput = 0f;  // work as a timer to create a smooth animation with trig functions
    private boolean isMuted;

    // For zooming
    private float targetZoom; // targetZoom stores the intermediate zoom value so that we can zoom smoothly

    private final Player player;
    Tiles tiles; // Tile system for the map

    private final OrthogonalTiledMapRenderer mapRenderer;

    private final ObjectRenderer hudObjectRenderer; // Hearts and other objects on the HUD

    private final SpotlightEffect spotlightEffect;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     * This will be our main screen while playing the game. So it manages everything while gaming.
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
        TiledMap tiledMap = tiles.loadTiledMap("maps/level 1 map.properties", Gdx.files.internal("level1_tileset.png").path(), 40, 40);

        // Set up map renderer
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,  (float) TILE_SCREEN_SIZE / TILE_SIZE); // Scale tiles, so like unitScale is times how many

        player = new Player(0, 1, 16, 32, 12, 19, 64f, 128f, 6.5f, false, tiles.layer);

        spotlightEffect = new SpotlightEffect();
        // spotlightEffect.create();
    }

    /**
     * Updates the camera's zoom smoothly based on the target zoom level.
     *
     * @param delta Time elapsed since the last frame.
     */
    private void updateZoom(float delta) {
        // Gradually adjust the camera's zoom level towards the target zoom
        camera.zoom += (targetZoom - camera.zoom) * ZOOM_SPEED;
    }


    /**
     * Handles mouse scroll events to adjust zoom levels.
     * Scrolling is automatically detected (for zooming)
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        targetZoom += amountY * 0.1f; // Adjust sensitivity as needed
        targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL); // Clamp zoom level
        Gdx.app.log("GameScreen", "mouse scrolled to adjust zoom");
        return true; // Return true to indicate the event was handled
    }

    /**
     * Handles user input for something throughout the whole game, like zooming and muting.
     */
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
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen
        camera.update(); // Update the camera

        // Move text in a circular path to have an example of a moving object
        sinusInput += delta;  // sinusInput is like `time`, storing the time for animation
        Gdx.input.setInputProcessor(this);

        updateZoom(delta); // Smoothly adjust zoom
        handleInput(); // handle input of the keys
        player.update(delta); // ALL the player functionalities are here

        renderGameWorld();

        game.getSpriteBatch().begin();
        // Render the text
        float textX = (float) (0 + Math.sin(sinusInput) * 100);
        float textY = (float) (750 + Math.cos(sinusInput) * 100);
        font.draw(game.getSpriteBatch(), "Press ESC to go to menu", textX, textY);

        renderPlayer();

        // Draw arrow that points at the exit
        float angle = getAngle();
        if (angle > 0) hudObjectRenderer.drawArrow(game.getSpriteBatch(), angle, player.getX(), player.getY());

        moveCamera();

        game.getSpriteBatch().end(); // Important to call this after drawing everything

        renderSpotlightEffect(player.x, player.y, 100); // TODO: reserved for future use (use the spotlight to introduce new feature of the game)
        renderHUD();
    }

    private float getAngle() {
        Map<String, Float> exitPosition = tiles.exitPositions.get(0); // TODO: (future) if there are multiple exit, create a function that finds the closest one
        if (exitPosition != null) {
            float exitX = exitPosition.get("x");
            float exitY = exitPosition.get("y");
            float angle = (float) Math.toDegrees(Math.atan2(exitY - player.y, exitX - player.x)); // atan2 is a useful version of atan;
            angle = (angle + 270) % 360; // rotate counter-clockwise by 90 deg to fit the system of LibGDX and ensure the angle is within [0, 360)
            return angle;
        }
        return -1;
    }

    /**
     * Renders the game world, including the map and background.
     */
    private void renderGameWorld(){
        // Set up and begin drawing with the sprite batch
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin(); // Important to call this before drawing anything
        game.getSpriteBatch().draw(game.getBackgroundTexture(), 0, 0);
        game.getSpriteBatch().end();

        // mapRenderer use another rendering batch, so we have to end the ones first, render the map, and then begin our spriteBatch again outside of this function
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    /**
     * Renders the player's character based on movement state.
     */
    private void renderPlayer(){
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
    }

    /**
     * Renders the Heads-Up Display (HUD), including player stats and health.
     */
    private void renderHUD() {
        SpriteBatch hudBatch = game.getSpriteBatch();
        hudBatch.setProjectionMatrix(hudCamera.combined); // HUD uses its own camera so that it does not follow the player and the position is fixed on the screen.
        hudBatch.begin();

        font.draw(hudBatch, "This is the HUD", 20, Gdx.graphics.getHeight() - 20);
        font.draw(hudBatch, "Score: " + Math.round(sinusInput), 20f, Gdx.graphics.getHeight() - 50f);
        font.draw(hudBatch, "Lives:", 20f, Gdx.graphics.getHeight() - 80f);

        // Show all the variables in the bottom-left corner here
        // Variables to show, stored in a map (LinkedHashMap preserves the order)
        Map<String, Float> variablesToShow = new LinkedHashMap<>();
        variablesToShow.put("player.x", player.getX());
        variablesToShow.put("player.y", player.getY());
        variablesToShow.put("player.speed", player.getSpeed());
        variablesToShow.put("camera zoom", camera.zoom);

        drawVariables(variablesToShow);

        hudBatch.end();

        // hudObjectRenderer use another rendering batch, so we have to end the batch first, and start it again
        hudBatch.begin();
        hudObjectRenderer.drawHearts(hudBatch, player.getLives(), 128, Gdx.graphics.getHeight() - 106f, 32, 2);
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

    private void renderSpotlightEffect(float x, float y, float spotlightRadius) {
        float[] screenCoordinates = getScreenCoordinates(x, y);
        float xOnScreen = screenCoordinates[0];
        float yOnScreen = screenCoordinates[1];
        Gdx.app.log("GameScreen", "screen x: " + xOnScreen + "; screen y: " + yOnScreen);
        spotlightEffect.render(camera, x, y, spotlightRadius, 0.8f);
    }

    /**
     * make sure the camera follows the player
     */
    private void moveCamera(){
        // camera.viewportWidth is the window width; camera.viewportHeight is the window height
        // Define the 80% boundary margins
        float marginX = camera.viewportWidth * 0.8f / 2 * camera.zoom; // 80% of the window width, divided by 2 because we go from the center; also consider zoom here
        float marginY = camera.viewportHeight * 0.8f / 2 * camera.zoom; // 80% of the window height, divided by 2 because we go from the center; also consider zoom here

        // Calculate the camera bounds to ensure the player is always in the middle 80%
        float maxX = WORLD_WIDTH - marginX;
        float maxY = WORLD_HEIGHT - marginY;

        // Set the camera position based on the player's position
        camera.position.set(player.getX(), player.getY(), 0);

        // Ensure the camera stays within the calculated bounds
        camera.position.x = MathUtils.clamp(camera.position.x, marginX, maxX);
        camera.position.y = MathUtils.clamp(camera.position.y, marginY, maxY);

        // Update the camera
        camera.update();
    }

    /**
     * Gets the actual screen coordinates of a character.
     * This method calculates the screen position based on the camera's position
     * and the given world position.
     *
     * @param x the world x-coordinate
     * @param y the world y-coordinate
     * @return A float array with two elements: [x, y], representing the actual coordinates on the screen.
     */
    private float[] getScreenCoordinates(float x, float y) {
        // Get the camera's window (viewport) size
        float windowWidth = Gdx.graphics.getWidth();
        float windowHeight = Gdx.graphics.getHeight();

        // Calculate the scaling factor based on the zoom and viewport size
        float scaleX = windowWidth / 2 / camera.viewportWidth;
        float scaleY = windowHeight / 2 / camera.viewportHeight;

        // Apply the scaling factor and camera position to the player's world coordinates
        float screenX = (x - camera.position.x) * scaleX + windowWidth / 2;
        float screenY = (y - camera.position.y) * scaleY + windowHeight / 2;

        return new float[]{screenX, screenY};
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
        shapeRenderer.dispose();
        mapRenderer.dispose();
        hudObjectRenderer.dispose();
    }

    /**
     * Draws a list of variables and their values on the HUD.
     * Used for debugging.
     *
     * @param variablesToShow A map of variable names and their corresponding values.
     */
    private void drawVariables(Map<String, Float> variablesToShow) {
        SpriteBatch hudBatch = game.getSpriteBatch();

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

    public OrthographicCamera getCamera() {
        return camera;
    }


    // Additional methods and logic can be added as needed for the game screen

}
