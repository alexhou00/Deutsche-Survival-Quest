package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.InputAdapter;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.tum.cit.fop.maze.Constants.*;
import static de.tum.cit.fop.maze.Position.PositionUnit.*;


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

    // For zooming
    private float targetZoom; // targetZoom stores the intermediate zoom value so that we can zoom smoothly

    private final Player player;
    Tiles tiles; // Tile system for the map
    Key key;
    TextureRegion keyRegion;

    private final OrthogonalTiledMapRenderer mapRenderer;

    private final ElementRenderer hudObjectRenderer; // Hearts and other objects on the HUD

    private final SpotlightEffect spotlightEffect;

    private List<ChasingEnemy> chasingEnemies;

    private TiledMapTileLayer collisionLayer;
    PopUpPanel popUpPanel;

    private final ShaderProgram shader;



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
        hudObjectRenderer = new ElementRenderer("objects.png");

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        game.setMuted(false);
        shapeRenderer = new ShapeRenderer();

        // initialize game world elements
        tiles = new Tiles();

        TiledMap tiledMap;
        switch (game.getGameLevel()) {
            case 1 -> tiledMap = tiles.loadTiledMap("maps/level-1-map.properties", Gdx.files.internal("level1_tileset.png").path(), 40, 40);
            case 2 -> tiledMap = tiles.loadTiledMap("maps/level-2.properties", Gdx.files.internal("level1_tileset.png").path(), 40, 40);
            default -> tiledMap = tiles.loadTiledMap("maps/level-1.properties", Gdx.files.internal("level1_tileset.png").path(), 40, 40);
        }

        // Initialize the key. Only after we lod the tiled map, we can access the key's position
        Position keyPosition = tiles.getKeyTilePosition().convertTo(PIXELS);
        float keyX = keyPosition.getX();
        float keyY = keyPosition.getY();
        key = new Key(keyX, keyY, TILE_SIZE,TILE_SIZE,10,9,TILE_SCREEN_SIZE, TILE_SCREEN_SIZE);
        // After loading the tiles,
        // get the array of tiles from our tile generator: tiles.getTiles()
        // and then get the texture region where our key is at
        keyRegion = tiles.getTileset()[Tiles.KEY].getTextureRegion();

        // Set up map renderer
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,  (float) TILE_SCREEN_SIZE / TILE_SIZE); // Scale tiles, so like unitScale is times how many

        // initialize player at entrance position
        player = new Player(
                tiles.entrance.getTileX(),
                tiles.entrance.getTileY(),
                16, 32, 12, 19, 64f, 128f, 6.5f,
                tiles.layer, tiles, this);//"this" is already a game screen




        // Initialize traps and add one trap (you can add more as needed)
        // traps = new ArrayList<>();
        //Trap trap1 = new Trap(100f, 150f, 50, 50, 30, 30, 50f, 50f, 1.0f);
        // traps.add(trap1);




        // Initialize ChasingEnemy with player and collisionLayer
        chasingEnemies = new ArrayList<>();
        ChasingEnemy chasingEnemy1 = new ChasingEnemy(10, 10, 32, 32, 32, 32, 64, 64, 3, tiles.layer, player);
        chasingEnemies.add(chasingEnemy1); // Add enemy targeting the player

        popUpPanel = new PopUpPanel();


        spotlightEffect = new SpotlightEffect();

        // Load and compile shaders
        ShaderProgram.pedantic = false; // Allow non-pedantic GLSL code
        shader = new ShaderProgram(
                Gdx.files.internal("default.vert"),
                Gdx.files.internal("hurtEffect.frag")
        );
        if (!shader.isCompiled()) {
            Gdx.app.error("ShaderError", shader.getLog());
        }

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
            game.setMuted(!game.isMuted());
            if (game.isMuted()) {
                game.muteBGM();
            } else {
                game.normalizeBGM();
            }
            Gdx.app.log("GameScreen", "Mute toggled: " + (game.isMuted() ? "ON" : "OFF"));
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

        if (player.getLives() <= 0) {
            game.goToGameOverScreen();  // Trigger game over screen
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

        game.checkExitToNextLevel(player);


        game.getSpriteBatch().begin();
        renderTrap();
        // renderText((float) (0 + Math.sin(sinusInput) * 100), (float) (750 + Math.cos(sinusInput) * 100), "Press ESC to go to menu");
        renderPlayer();
        renderArrow();
        renderKey();


        if (!chasingEnemies.isEmpty()) {
            chasingEnemies.get(0).draw(game.getSpriteBatch());
        }


        moveCamera();

        game.getSpriteBatch().end(); // Important to call this after drawing everything

        // renderSpotlightEffect(player.getX(), player.getY(), 100); // TODO: reserved for future use (use the spotlight to introduce new feature of the game)
        renderHUD();
    }

    /**
     * Calculates the angle between the player and a target position.
     * Used for directing the arrow towards something like, for example, the exit(s)
     *
     * @param position The target position to calculate an angle to
     * @return The angle in degrees, or -1 if position is null
     */
    private float getAngle(Position position) {
        if (position != null) {
            if (position.getUnit() != PIXELS)
                position = position.convertTo(PIXELS);
            float x = position.getX();
            float y = position.getY();
            // Calculate the angle using arc tangent, adjusting for the coordinate system of LibGDX
            float angle = (float) Math.toDegrees(Math.atan2(y - player.getY(), x - player.getX())); // atan2 is a useful version of atan;
            angle = (angle + 270) % 360; // rotate counter-clockwise by 90° and normalize to [0, 360)
            return angle;
        }
        return -1; // position not found
    }

    /**
     * Renders the game world, including the map and background.
     * Must be called between SpriteBatch begin() and end().
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

    private void renderText(float textX, float textY, String text) {
        // Render the text
        font.draw(game.getSpriteBatch(), text, textX, textY);
    }

    /**
     * Renders the player's character based on movement state.
     */
    private void renderPlayer(){
        if (player.isHurt()){
            game.getSpriteBatch().setShader(shader);
            shader.setUniformf("isHurt", 0.1f);
        }

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
        game.getSpriteBatch().setShader(null);
    }

    private void renderArrow(){
        // Draw arrow that points at the exit
        Position exitPosition = null;
        if (!tiles.exits.isEmpty())
            exitPosition = tiles.getNearestExit(player.getX(), player.getY()).getTilePosition();

        float angle = getAngle(exitPosition);
        
        if (angle > 0) hudObjectRenderer.drawArrow(game.getSpriteBatch(), angle, player.getX(), player.getY());
    }

    /**
     * Renders the collectible key in the game world if it hasn't been collected.
     * The key is rendered at its designated position on the map and checks for
     * collision with the player. If a collision occurs, the key is marked as collected
     * and will no longer be rendered.
     * <p>
     * The key's position is converted from tile coordinates to pixel coordinates
     * before rendering to ensure proper placement in the game world.
     */
    public void renderKey() {
        float keyScale = 1f;
        if (key.isCollected()){
            key.setX(player.getX());
            key.setY(player.getY() - 10);
            keyScale = 0.5f;
        }
            //return;
        // else the key is not collected, render the key:

        /* uncomment this when the key's position should be regularly updated. i.e., the key is dynamic
        //get our key position and render the key there
        Position keyPosition = tiles.getKeyTilePosition();
        // convert key's tile position to pixel coordinates for rendering
        keyPosition = keyPosition.convertTo(PIXELS);
        key.setX(keyPosition.getX());
        key.setY(keyPosition.getY());*/

        game.getSpriteBatch().draw(
                keyRegion,
                key.getOriginX(),
                key.getOriginY(),
                key.getWidthOnScreen() * keyScale,
                key.getHeightOnScreen() * keyScale
        ); // width and height are size on the screen

        // check for collision with player and collect key if touching
        if (key.isTouching(player)){
            key.collect();
        }
    }

    private void renderChasingEnemies() {
        chasingEnemies.get(0).draw(game.getSpriteBatch());
    }

    private void renderTrap(){
        for (Trap trap : tiles.traps){
            trap.draw(game.getSpriteBatch());
        }
    }

    /**
     * Renders the Heads-Up Display (HUD), including player stats and health.
     */
    private void renderHUD() {
        SpriteBatch hudBatch = game.getSpriteBatch();
        hudBatch.setProjectionMatrix(hudCamera.combined); // HUD uses its own camera so that it does not follow the player and the position is fixed on the screen.
        hudBatch.begin();


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
        hudObjectRenderer.drawHearts(hudBatch, player.getLives(), 20, Gdx.graphics.getHeight() - 26f - 20, 32, 2);
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
        Position screenCoordinates = getScreenCoordinates(x, y);
        float xOnScreen = screenCoordinates.getX();
        float yOnScreen = screenCoordinates.getY();
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
     * @return A {@link Position} with x and y in PIXELS representing the actual coordinates on the screen.
     */
    private Position getScreenCoordinates(float x, float y) {
        // Get the camera's window (viewport) size
        float windowWidth = Gdx.graphics.getWidth();
        float windowHeight = Gdx.graphics.getHeight();

        // Calculate the scaling factor based on the zoom and viewport size
        float scaleX = windowWidth / 2 / camera.viewportWidth;
        float scaleY = windowHeight / 2 / camera.viewportHeight;

        // Apply the scaling factor and camera position to the player's world coordinates
        float screenX = (x - camera.position.x) * scaleX + windowWidth / 2;
        float screenY = (y - camera.position.y) * scaleY + windowHeight / 2;

        return new Position(screenX, screenY);
    }

    /**
     * Handles the resizing of the game window. Will be automatically called once the window is being resized by user.
     * Updates both the main game camera and HUD camera to maintain proper rendering proportions when the window size changes.
     *
     * @param width The new width of the game window in pixels
     * @param height The new height of the game window in pixels
     */
    @Override
    public void resize(int width, int height) {
        player.pause();
        camera.setToOrtho(false);
        hudCamera.setToOrtho(false, width, height); // Adjust HUD camera to new screen size
        player.resume();
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

    //getter for trap and enemy

    public List<ChasingEnemy> getChasingEnemies() {
        return chasingEnemies;
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

}
