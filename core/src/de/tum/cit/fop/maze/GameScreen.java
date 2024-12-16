package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.InputAdapter;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
    private final BitmapFont font;

    private float sinusInput = 0f;  // work as a timer to create a smooth movement with trig

    // private boolean isMoving; // to see if the player needs the walking animation
    private boolean isMuted;

    // For zooming
    private float targetZoom; // targetZoom stores the intermediate zoom value so that we can zoom smoothly

    private Player player;
    private TiledMap tile;

    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap tiledMap;

    int TILE_SIZE = 16;

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

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        isMuted = false;

        player = new Player(700, 500, 16, 32, 14, 22, 64, 128, 1, false);
        System.out.println("Here");
        this.tile = new TiledMap();
        System.out.println("Here2");

        // Load tiled map
        tiledMap = loadTiledMap("maps/level-5.properties", Gdx.files.internal("basictiles.png").path());

        // Set up map renderer

        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,  WORLD_WIDTH / 20f / TILE_SIZE); // Scale tiles (20 is the number of tiles of the width // so like unitScale is times how many (16 x 2 in this case)

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

            // tile.render();


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
            variablesToShow.put("player.x", player.getX());
            variablesToShow.put("player.y", player.getY());
            variablesToShow.put("camera zoom", camera.zoom);

            int currentLine = 0;
            for (Map.Entry<String, Float> entry : variablesToShow.entrySet()) {
                String varName = entry.getKey();
                float displayedValue = entry.getValue();
                font.draw(game.getSpriteBatch(), String.format("%s: %.2f", varName, displayedValue), windowX + BORDER_OFFSET + camera.position.x, windowY - BORDER_OFFSET + camera.position.y - Y_OFFSET * currentLine);
                currentLine++;
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

    private TiledMap loadTiledMap(String mapFilePath, String tileSheetPath) {
        // Load tile sheet
        var tileSheet = new Texture(tileSheetPath);
        int tileCols = tileSheet.getWidth() / TILE_SIZE;
        int tileRows = tileSheet.getHeight() / TILE_SIZE;

        // Create tiles based on tile sheet
        StaticTiledMapTile[] tiles = new StaticTiledMapTile[tileCols * tileRows];
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                tiles[y * tileCols + x] = new StaticTiledMapTile(new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE));
            }
        }

        // Parse properties file
        ObjectMap<String, Integer> mapData = parsePropertiesFile(mapFilePath);

        // Create a TiledMap
        TiledMap map = new TiledMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(100, 100, TILE_SIZE, TILE_SIZE); // put our width/height here
        int i = 0;
        // Populate the layer with tiles
        for (String key : mapData.keys()) {
            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[0]);
            int tileValue = mapData.get(key);

            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(tiles[tileValue]);
            layer.setCell(x, y, cell);
            i++;
            System.out.println(i);
        }

        map.getLayers().add(layer);
        Gdx.app.log("Tiles", "Tiled Map loaded");
        return map;
    }

    private ObjectMap<String, Integer> parsePropertiesFile(String filePath) {
        ObjectMap<String, Integer> mapData = new ObjectMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) continue;
                String[] parts = line.split("=");
                mapData.put(parts[0], Integer.parseInt(parts[1]));
                Gdx.app.log("Tiles", "Parsed: " + parts[0] + " = " + parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }

}
