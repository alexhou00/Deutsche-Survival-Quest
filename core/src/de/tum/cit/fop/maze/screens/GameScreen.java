package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.*;
import de.tum.cit.fop.maze.game_objects.*;
import de.tum.cit.fop.maze.level.Tiles;
import de.tum.cit.fop.maze.rendering.ElementRenderer;
import de.tum.cit.fop.maze.rendering.SpotlightEffect;
import de.tum.cit.fop.maze.tiles.Exit;
import de.tum.cit.fop.maze.util.Position;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.PositionUnit.*;
import static de.tum.cit.fop.maze.util.Position.getWorldCoordinateInPixels;
import static java.lang.Math.abs;


/**
 * The GameScreen class is responsible for rendering the gameplay screen. <br>
 * It handles the game logic and rendering of the game elements. <br>
 * By extending InputAdapter, we can override the scroll method to detect mouse scroll (for zooming)
 */
public class GameScreen extends InputAdapter implements Screen {

    public final MazeRunnerGame game;

    private final OrthographicCamera camera;
    private final OrthographicCamera hudCamera; // HUD camera. HUD uses another camera so that it does not follow the player and is fixed on the screen.
    // For zooming
    private float targetZoom; // targetZoom stores the intermediate zoom value so that we can zoom smoothly

    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer; // For drawing shapes like health bars
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final ElementRenderer hudObjectRenderer; // Hearts and other objects on the HUD

    private float sinusInput = 0f;  // work as a timer to create a smooth animation with trig functions

    private final Player player;
    public Tiles tiles; // Tile system for the map
    private final Key key;
    TextureRegion keyRegion;
    private final Array<Collectibles> collectibles;

    private final SpotlightEffect spotlightEffect;

    //private TiledMapTileLayer collisionLayer;
    //PopUpPanel popUpPanel;

    private final ShaderProgram shader;
    private final Stage stage1;

    // Show all the variables in the bottom-left corner here
    // Variables to show, stored in a map (LinkedHashMap preserves the order)
    Map<String, Float> variablesToShow = new LinkedHashMap<>();
    InputMultiplexer inputMultiplexer = new InputMultiplexer();

    Animation<TextureRegion> playerAnimation;
    Animation<TextureRegion> mobGuyAnimation;

    // Timer to track how long the stamina bar should stay visible after refill
    private float staminaTimer = 0f;
    private static final float STAMINA_DISPLAY_TIME = 1f; // Duration to show stamina bar in seconds

    private boolean isPaused;




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

        Viewport viewport1 = new ScreenViewport(hudCamera);
        stage1 = new Stage(viewport1, game.getSpriteBatch());

        createIntroPanel();


        // We use an InputMultiplexer instead of only stage or "this",
        // since both stage1 (for intro panel) and the GameScreen (for scrolling) handle inputs
        inputMultiplexer.addProcessor(stage1); // the stage is for the intro panel
        inputMultiplexer.addProcessor(this);  // used to detect mouse scrolls
        Gdx.input.setInputProcessor(stage1);

        // Load textures for HUD
        hudObjectRenderer = new ElementRenderer("original/objects.png");

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");

        game.setMuted(false);
        shapeRenderer = new ShapeRenderer();

        // initialize game world elements
        tiles = new Tiles();

        TiledMap tiledMap;
        switch (game.getGameLevel()) {
            case 1 -> tiledMap = tiles.loadTiledMap("maps/level-1-map.properties", Gdx.files.internal("tilesets/level1_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
            case 2 -> tiledMap = tiles.loadTiledMap("maps/level-2.properties", Gdx.files.internal("tilesets/level1_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
            case 3 -> tiledMap = tiles.loadTiledMap("maps/level-n-map.properties", Gdx.files.internal("tilesets/germanbar_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
            default -> tiledMap = tiles.loadTiledMap("maps/level-1-map.properties", Gdx.files.internal("tilesets/level1_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path()); // TODO: problems reading other maps given by the tutors
        }

        // Initialize the key. Only after we lod the tiled map, we can access the key's position
        Position keyPosition = tiles.getKeyTilePosition().convertTo(PIXELS);
        float keyX = keyPosition.getX();
        float keyY = keyPosition.getY();
        key = new Key(keyX, keyY, TILE_SIZE,TILE_SIZE,10,9,TILE_SCREEN_SIZE, TILE_SCREEN_SIZE, game);
        // After loading the tiles,
        // get the array of tiles from our tile generator: tiles.getTiles()
        // and then get the texture region where our key is at
        keyRegion = tiles.getTileset()[Tiles.KEY].getTextureRegion();

        collectibles = new Array<>();
        System.out.println(Arrays.deepToString(tiles.getTileEnumOnMap()));
        spawnCollectibles();

        // Set up map renderer
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,  (float) TILE_SCREEN_SIZE / TILE_SIZE); // Scale tiles, so like unitScale is times how many

        // initialize player at entrance position
        player = new Player(
                tiles.entrance.getTileX(),
                tiles.entrance.getTileY(),
                16, 32, 12, 19, 64f, 128f, 200f,
                this, tiles);//"this" is already a game screen

        // Initialize traps and add one trap (you can add more as needed)
        // traps = new ArrayList<>();
        //Trap trap1 = new Trap(100f, 150f, 50, 50, 30, 30, 50f, 50f, 1.0f);
        // traps.add(trap1);

        // Initialize ChasingEnemy with player and collisionLayer
        //chasingEnemy = new ArrayList<>();
        //ChasingEnemy chasingEnemy1 = new ChasingEnemy(10, 10, 32, 32, 32, 32, 64, 64, 3, tiles.layer, player, chasingEnemyTexture);
        //chasingEnemy.add(chasingEnemy1); // Add enemy targeting the player*/
        //chasingEnemy = new ChasingEnemy(0, 0, 32, 32, 32, 32, 64, 64, 3, this, tiles.layer, player); //new TextureRegion(new Texture(Gdx.files.internal( "mob_guy.png")), 0, 0, 32, 32));

        // for whatever that requires touching the player
        for (ChasingEnemy enemy : iterate(tiles.chasingEnemies)){
            enemy.init(player);
        }
        for (Collectibles collectible : iterate(collectibles)){
            collectible.init(player, game.getSoundEffectKey());
        }

        //popUpPanel = new PopUpPanel();

        spotlightEffect = new SpotlightEffect();

        // Load and compile shaders
        ShaderProgram.pedantic = false; // Allow non-pedantic GLSL code
        shader = new ShaderProgram(
                Gdx.files.internal("effects/default.vert"),
                Gdx.files.internal("effects/hurtEffect.frag")
        );
        if (!shader.isCompiled()) {
            Gdx.app.error("ShaderError", shader.getLog());
        }

        this.pause(false); // pause the game but don't create a pause panel
        Gdx.input.setInputProcessor(stage1);
        //Gdx.app.log("Size" ,  horizontalTilesCount + "x" + verticalTilesCount);


        //createPausePanel();
    }

    private void spawnCollectibles() {
        // Get the 2D array of tiles
        // Find all "OTHER" tiles
        Array<Position> emptyTiles = new Array<>();
        for (int x = 0; x < tiles.getTileEnumOnMap().length; x++) {
            for (int y = 0; y < tiles.getTileEnumOnMap()[x].length; y++) {
                if (tiles.getTileEnumOnMap()[x][y] == Tiles.TileType.OTHER || tiles.getTileEnumOnMap()[x][y] == null) {
                    emptyTiles.add(new Position(x, y, TILES));
                }
            }
        }

        // Randomly select 5 unique "OTHER" tiles
        generateCollectibles(emptyTiles, Collectibles.Type.HEART, 5, 11, 48);

        generateCollectibles(emptyTiles, Collectibles.Type.COIN, 5, 11, 48);
    }

    private void generateCollectibles(Array<Position> emptyTiles, Collectibles.Type type, int numberToGenerate, int imageSize, float sizeOnScreen) {
        int collectiblesToGenerate = Math.min(numberToGenerate, emptyTiles.size);
        for (int i = 0; i < collectiblesToGenerate; i++) {
            int randomIndex = MathUtils.random(emptyTiles.size - 1);
            Position position = emptyTiles.removeIndex(randomIndex).convertTo(PIXELS); // Remove selected position to avoid duplicates
            float worldX = position.getX();
            float worldY = position.getY();

            // Generate a collectible at the selected position
            collectibles.add(new Collectibles(worldX, worldY, imageSize, imageSize, imageSize, imageSize,
                    sizeOnScreen, sizeOnScreen, type));
        }
    }

    public void createIntroPanel(){
       // game.getPauseMusic().pause();
        Table table = new Table();
        Drawable background = createSolidColorDrawable(Color.WHITE);
        stage1.addActor(table);

        // Gdx.input.setInputProcessor(stage1);
        table.setBackground(background);
        table.setSize(Gdx.graphics.getWidth() * 0.9f,Gdx.graphics.getHeight() * 0.9f);
        table.setPosition(Gdx.graphics.getWidth() * 0.05f,Gdx.graphics.getHeight() * 0.05f);

        Label label = new Label("Game Instructions",game.getSkin(),"title");
        table.add(label).padBottom(80).center().row();
        label.getStyle().font.getData().setScale(0.5f);

        TextButton button = new TextButton("Start now", game.getSkin());

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                Gdx.app.log("start game", "Start game");
                table.remove(); // Change to the game screen when the button is pressed
                game.resume();
                // Reset the player's position to start position just in case there's velocity from the previous level
                // and that the player would go into the walls because the collision detecting hasn't started yet
                player.setX(getWorldCoordinateInPixels(tiles.entrance.getTileX()));
                player.setY(getWorldCoordinateInPixels(tiles.entrance.getTileY()));
            }});
        table.add(button); // TODO: fix button
        button.setPosition(200, 200); // Set a clear position on the stage
    }

    public void createPausePanel() {
        System.out.println("pause panel created");

        Table pausePanelTable = new Table();
        Drawable background = createSolidColorDrawable(Color.GRAY); // Semi-transparent background
        stage1.addActor(pausePanelTable);

        final float BUTTON_PADDING = 10f; // Vertical padding

        pausePanelTable.setBackground(background);
        pausePanelTable.setSize(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight() * 0.6f);
        pausePanelTable.setPosition(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.2f);

        Label pauseLabel = new Label("Game Paused", game.getSkin(), "title");
        pausePanelTable.add(pauseLabel).padBottom(80).center().row();
        pauseLabel.getStyle().font.getData().setScale(0.5f);

        Button resumeButton =  new TextButton("Resume", game.getSkin());
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                Gdx.app.log("start game", "Start game");
                pauseLabel.remove(); // Change to the game screen when the button is pressed
                pausePanelTable.remove();
                game.resume();
                isPaused = false;
            }});
        pausePanelTable.add(resumeButton).padBottom(BUTTON_PADDING).row(); // row() is to add new row, or else elements will stay on the same row
        // resumeButton.setPosition(200, 200); // Set a clear position on the stage

        Button selectLevelButton =  new TextButton("Select Level", game.getSkin());
        selectLevelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                game.selectLevel();
            }});
        pausePanelTable.add(selectLevelButton).padBottom(BUTTON_PADDING).row();
        //selectLevelButton.setPosition(100, 400); // Set a clear position on the stage

        Button goToMenuButton =  new TextButton("Back to Menu", game.getSkin());
        goToMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                game.goToMenu();
            }});
        pausePanelTable.add(goToMenuButton).padBottom(BUTTON_PADDING).row();
        //goToMenuButton.setPosition(900, 600); // Set a clear position on the stage

        Button ExitGameButton =  new TextButton("Exit Game", game.getSkin());
        ExitGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                game.exitGame();
            }});
        pausePanelTable.add(ExitGameButton).padBottom(BUTTON_PADDING).row();
        //ExitGameButton.setPosition(200, 800); // Set a clear position on the stage
    }

    public void createVictoryPanel() {
        System.out.println("Victory panel created");

        Table victoryPanelTable = new Table();
        Drawable background = createSolidColorDrawable(Color.GOLD); // Gold background to signify victory
        stage1.addActor(victoryPanelTable);

        final float BUTTON_PADDING = 10f; // Vertical padding

        victoryPanelTable.setBackground(background);
        victoryPanelTable.setSize(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight() * 0.6f);
        victoryPanelTable.setPosition(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.2f);

        Label victoryLabel = new Label("Victory!", game.getSkin(), "title");
        victoryPanelTable.add(victoryLabel).padBottom(80).center().row();
        victoryLabel.getStyle().font.getData().setScale(0.5f);

        Button nextLevelButton =  new TextButton("Next Level", game.getSkin());
        nextLevelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                Gdx.app.log("next level", "Next Level");
                game.setGameLevel(game.getGameLevel() + 1); // Increment level
                game.getVictorySoundEffect().stop();
                game.getBackgroundMusic().stop();
                dispose();
                GameScreen gameScreen = new GameScreen(game); // Initialize new game screen
                gameScreen.getKey().setCollected(false);
                game.setScreen(gameScreen);
            }});
        victoryPanelTable.add(nextLevelButton).padBottom(BUTTON_PADDING).row();

        /*Button playAgainButton = new TextButton("Play Again", game.getSkin());
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //game.restartLevel(); // Restart the current level
            }
        });
        victoryPanelTable.add(playAgainButton).padBottom(BUTTON_PADDING).row();*/

        Button goToMenuButton = new TextButton("Back to Menu", game.getSkin());
        goToMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu(); // Navigate back to the main menu
            }
        });
        victoryPanelTable.add(goToMenuButton).padBottom(BUTTON_PADDING).row();

        /*Button exitGameButton = new TextButton("Exit Game", game.getSkin());
        exitGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.exitGame(); // Exit the game
            }
        });
        victoryPanelTable.add(exitGameButton).padBottom(BUTTON_PADDING).row();*/
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
        if (isPaused) return true;
        targetZoom += amountY * 0.1f; // Adjust sensitivity as needed
        targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL); // Clamp zoom level
        Gdx.app.log("GameScreen", "mouse scrolled to adjust zoom");
        return true; // Return true to indicate the event was handled
    }

    private Drawable createSolidColorDrawable(Color color) {
        // Create a Pixmap with the solid color
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        // Create a Texture from the Pixmap
        Texture texture = new Texture(pixmap);

        // Clean up the Pixmap
        pixmap.dispose();

        // Return a Drawable
        return new TextureRegionDrawable(texture);
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
        handlePauseInput();
        // https://stackoverflow.com/questions/46080673/libgdx-game-pause-state-animation-flicker-bug
        // we couldn't stop drawing even if the game is paused
        /*if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
            return;
        }*/

        if (player.getLives() <= 0) {
            game.goToGameOverScreen();  // Trigger game over screen
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen
        camera.update(); // Update the camera

        // Move text in a circular path to have an example of a moving object
        sinusInput += ((!isPaused) ? delta : 0);  // sinusInput is like `time`, storing the time for animation

        updateZoom(delta); // Smoothly adjust zoom
        handleInput(); // handle input of the keys

        player.update(delta); // ALL the player functionalities are here
        for (ChasingEnemy enemy : iterate(tiles.chasingEnemies)) {
            enemy.update(delta);
        }
        for (int i = collectibles.size - 1; i >= 0; i--) {
            Collectibles collectible = collectibles.get(i);
            if (collectible.isCollected()) {
                collectibles.removeIndex(i);
            } else {
                collectible.update();
            }
        }

        if (key.isCollected() && player.isCenterTouchingTile(Exit.class)) {
            if (!isPaused) {
                createVictoryPanel(); // Show the victory panel
                isPaused = true;
                game.pause(); // Pause the game
                game.getBackgroundMusic().pause();
                game.getPauseMusic().pause();
                game.getVictorySoundEffect().play();
            }
        }

        game.checkExitToNextLevel(player);

        renderGameWorld();


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawMapBorder();
        renderStamina();
        shapeRenderer.end();

        game.getSpriteBatch().begin();
        renderTrap();
        // renderText((float) (0 + Math.sin(sinusInput) * 100), (float) (750 + Math.cos(sinusInput) * 100), "Press ESC to go to menu");
        renderCollectibles();
        renderChasingEnemy();
        renderPlayer();
        renderArrow();
        renderKey();

        renderSpeechBubble();

        game.getSpriteBatch().end(); // Important to call this after drawing everything


        moveCamera();

        stage1.act(delta);
        stage1.draw(); // stage1 is for the panels, like the intro panel and the pause panel
        // renderSpotlightEffect(player.getX(), player.getY(), 100); // TODO: reserved for future use (use the spotlight to introduce new feature of the game)

        renderHUD();
    }
   /* private void renderPausedState() {
        if (isPaused) {
            createPausePanel();
        }
    }*/

    private void update(float delta) {
        if (!isPaused) {
            // Update characters
            player.update(delta);

            // Update enemies
            for (ChasingEnemy enemy : iterate(tiles.chasingEnemies)) {
                enemy.update(delta);
            }

            // Update timer
           // gameTimer += delta;
        }
    }

    public void drawMapBorder() {
        // if (mapTiles.isEmpty()) return;

        // Set up ShapeRenderer to match game world projection
        //shapeRenderer.setProjectionMatrix(game.getSpriteBatch().getProjectionMatrix());

        // Begin drawing with filled shapes for the border
        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE); // Set border color

        // Draw top border (horizontal)
        shapeRenderer.rect(0, getWorldHeight(), getWorldWidth(), TILE_SIZE);

        // Draw bottom border (horizontal)
        shapeRenderer.rect(0, -TILE_SIZE, getWorldWidth(), TILE_SIZE);

        // Draw left border (vertical)
        shapeRenderer.rect(-TILE_SIZE, -TILE_SIZE, TILE_SIZE, (getWorldHeight() + (2*TILE_SIZE)));

        // Draw right border (vertical)
        shapeRenderer.rect(getWorldWidth() , -TILE_SIZE, TILE_SIZE, (getWorldHeight() + (2*TILE_SIZE)));

        // End drawing the border
        //shapeRenderer.end();
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
            // atan2 is a useful version of atan: angle := (float) Math.toDegrees(Math.atan2(y - player.getY(), x - player.getX()));
            float angle = MathUtils.atan2Deg(y - player.getY(), x - player.getX()); // equiv. as the above comment, but for
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
        // this adds a darker shade to it, like night effect
        //mapRenderer.getBatch().setColor(0.5f, 0.5f, 0.5f, 1);
        mapRenderer.render(); // mapRenderer renders the map, also the layers or so the tiles
    }

    private void renderText(float textX, float textY, String text) {
        // Render the text
        font.draw(game.getSpriteBatch(), text, textX, textY);
    }

    /**
     * Renders the player's character based on movement state.
     */
    private void renderPlayer(){
        if (player.getHurtTimer() > 0.3f){ // 0.8 s ~ 0.3 s, during the 0.5 s duration, add the red tint
            game.getSpriteBatch().setShader(shader);
            shader.setUniformf("isHurt", 0.1f);
        }

        if (player.isMoving()) {  // Character Walking Animation
            // Draw the character next to the text :) / We can reuse sinusInput here
            int hurtFactor = (player.isHurt()) ? -1 : 1; // used to adjust our conditional expression so that the player can still face the same direction even when getting damage
            if (abs(player.getVelX()) > abs(player.getVelY())){ // x velocity > y velocity -> either left or right
                if (player.getVelX() * hurtFactor < 0) playerAnimation = game.getCharacterLeftAnimation();
                else playerAnimation = game.getCharacterRightAnimation();
            }
            else { // v_y > v_x
                if (player.getVelY() * hurtFactor < 0) playerAnimation = game.getCharacterDownAnimation();
                else playerAnimation = game.getCharacterUpAnimation();
            }

            game.getSpriteBatch().draw(
                    playerAnimation.getKeyFrame(sinusInput, true),
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
        game.getSpriteBatch().setShader(null); // end the shader so we only shade the player
    }


    private void renderArrow(){
        // Draw arrow that points at the exit
        Position exitPosition = null;
        if (!tiles.exits.isEmpty())
            exitPosition = tiles.getNearestExit(player.getX(), player.getY()).getTilePosition();

        float angle = getAngle(exitPosition);

        if (angle > 0) hudObjectRenderer.drawArrow(game.getSpriteBatch(), angle, player.getX(), player.getY());

    }

    private void renderCollectibles(){
        for (Collectibles collectible : iterate(collectibles)) {
            if (collectible.getType().equals(Collectibles.Type.HEART))
                collectible.render(game.getSpriteBatch(), game.getHeartAnimation().getKeyFrame(sinusInput/1.5f, true));
            else if (collectible.getType().equals(Collectibles.Type.COIN)){
                collectible.render(game.getSpriteBatch(), game.getCoinAnimation().getKeyFrame(sinusInput/1.5f, true));
            }
        }

    }

    private void renderSpeechBubble(){
        if (player.canSpeak) {
            player.getSpeechBubble().show(5.0f);
            player.canSpeak = false;
        }

        player.say("""
                        The quick brown fox jumps over the lazy dog.
                        Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich.
                        """, game.getSpriteBatch(),
                true, player.getSpeechBubble().getElapsedTime(), 0.03f);
    }

    private void handlePauseInput(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isPaused) {
            pause();
        }

        // If the Enter key is pressed and the game is paused, resume the game
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && isPaused) {
            resume();
        }
    }
    /*private void handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { // Assuming "P" pauses the game
            if (isPaused == true) {
                createPausePanel();
                inputMultiplexer.addProcessor(stage1); // Add pause stage
            } else if (isPaused== false) {
                stage1.clear(); // Clear the pause panel
                inputMultiplexer.removeProcessor(stage1); // Remove pause stage
            }
        }
    }*/

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

    private void renderTrap(){
        for (Trap trap : iterate(tiles.traps)){ // for (trap : tiles.traps){
            trap.draw(game.getSpriteBatch());
        }
    }
    private void renderChasingEnemy(){
        //chasingEnemy.draw(game.getSpriteBatch());
        for (ChasingEnemy enemy : iterate(tiles.chasingEnemies)){ // for (ChasingEnemy enemy : tiles.chasingEnemies)

            if (abs(enemy.getVelX()) > abs(enemy.getVelY())){ // x velocity > y velocity -> either left or right
                if (enemy.getVelX() < 0) mobGuyAnimation = game.getMobGuyAnimations().get("left");
                else mobGuyAnimation = game.getMobGuyAnimations().get("right");
            }
            else { // v_y > v_x
                if (enemy.getVelY() < 0) mobGuyAnimation = game.getMobGuyAnimations().get("down");
                else mobGuyAnimation = game.getMobGuyAnimations().get("up");
            }

            enemy.draw(game.getSpriteBatch(), mobGuyAnimation.getKeyFrame(sinusInput, true));
        }
    }

    private void renderStamina(){
        float currentStamina = player.getStamina();
        //if (currentStamina >= Player.maxStamina) return;

        if (currentStamina >= Player.maxStamina) {
            // Start the timer if stamina is full
            staminaTimer += Gdx.graphics.getDeltaTime();
            if (staminaTimer > STAMINA_DISPLAY_TIME) {
                // Hide the stamina bar if the timer exceeds the display duration
                return;
            }
        } else {
            // Reset the timer when stamina is not full
            staminaTimer = 0f;
        }

        Gdx.gl.glLineWidth(5f); // Set line width for better visibility
        int staminaRadius = 10;
        float offsetX = -player.getHitboxWidthOnScreen() / 2 - 5;
        float offsetY = player.getHitboxHeightOnScreen() / 2 + 5;
        float staminaX = player.getX() + offsetX;// + staminaRadius / 2;
        float staminaY = player.getY() + offsetY;// - staminaRadius;
        //shapeRenderer.setProjectionMatrix(camera.combined);
        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the background circle (full arc for reference)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.arc(staminaX, staminaY, staminaRadius, 0, 360); // arc's 0° (where it starts drawing) starts at +x of the cartesian plane

        // Draw the stamina arc
        shapeRenderer.setColor(Color.LIME);
        float angle = (player.getStamina() / Player.maxStamina) * 360f; // Calculate the angle based on stamina
        shapeRenderer.arc(staminaX, staminaY, staminaRadius, 90 - angle, angle); // Draw arc clockwise

        //shapeRenderer.end();
    }

    /**
     * Renders the Heads-Up Display (HUD), including player stats and health.
     */
    private void renderHUD() {
        game.getSpriteBatch().setProjectionMatrix(hudCamera.combined); // HUD uses its own camera so that it does not follow the player and the position is fixed on the screen.
        game.getSpriteBatch().begin();

        variablesToShow.clear();
        variablesToShow.put("player.x", player.getX());
        variablesToShow.put("player.y", player.getY());
        variablesToShow.put("player.speed", player.getSpeed());
        variablesToShow.put("camera zoom", camera.zoom);
        variablesToShow.put("player.coins", (float) player.getCoins());
        drawVariables(variablesToShow);

        game.getSpriteBatch().end();

        // hudObjectRenderer use another rendering batch, so we have to end the batch first, and start it again
        game.getSpriteBatch().begin();
        hudObjectRenderer.drawHearts(game.getSpriteBatch(), player.getLives(), 20, Gdx.graphics.getHeight() - 26f - 20, 32, 2);
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
        game.getSpriteBatch().end();
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
        float maxX = getWorldWidth() - marginX;
        float maxY = getWorldHeight() - marginY;

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
        for (var panel : iterate(stage1.getActors())){
            panel.setSize(Gdx.graphics.getWidth() * 0.9f,Gdx.graphics.getHeight() * 0.9f);
        }
        Gdx.input.setInputProcessor(inputMultiplexer);
    }



    public void pause(boolean createPausePanel) {
        Gdx.app.log("GameScreen", "Game paused");
        // Stop processing input temporarily
        //Gdx.input.setInputProcessor(null); // Disable input handling during pause
        isPaused = true; // Set the game to "paused"

        game.getBackgroundMusic().pause();
        game.getPauseMusic().play();

        player.pause();
        for (ChasingEnemy enemy : iterate(tiles.chasingEnemies)){
            enemy.pause();
        }

        if (createPausePanel) createPausePanel(); // Show the pause panel

        //inputMultiplexer.addProcessor(stage1);
        Gdx.input.setInputProcessor(stage1); // Set input processor to stage1 (pause menu)
    }

    @Override
    public void pause() { // Overloading method
        pause(true);
    }

    @Override
    public void resume() {
        Gdx.app.log("GameScreen", "Game resumed");

        Gdx.input.setInputProcessor(inputMultiplexer);
        isPaused = false; // Set the game to unpaused

        game.getBackgroundMusic().play();
        game.getPauseMusic().pause();

        player.resume();
        for (ChasingEnemy enemy : iterate(tiles.chasingEnemies)){
            enemy.resume();
        }

        stage1.clear(); // Clear the pause panel from the screen
        inputMultiplexer.removeProcessor(stage1);
    }

    @Override
    public void show() {
      //  createPausePanel();
       // Gdx.input.setInputProcessor(stage1);
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        //shapeRenderer.dispose();
        // i think we shouldn't even dispose the shapeRenderer, right? (else the program will exit unexpectedly)
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

    public void getCreateVictoryPanel(){
        getCreateVictoryPanel(); // TODO: THIS WILL RECURSE INFINITELY, MAKE SURE NOT TO DO SO WHEN YOU WANT TO USE THIS METHOD
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Key getKey() {
        return key;
    }

    public boolean isPaused() {
        return isPaused;
    }
}
