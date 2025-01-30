package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.*;
import de.tum.cit.fop.maze.game_objects.*;
import de.tum.cit.fop.maze.level.LevelManager;
import de.tum.cit.fop.maze.rendering.ElementRenderer;
import de.tum.cit.fop.maze.rendering.Panel;
import de.tum.cit.fop.maze.rendering.ResizeableTable;
import de.tum.cit.fop.maze.rendering.SpotlightEffect;
import de.tum.cit.fop.maze.tiles.TileType;
import de.tum.cit.fop.maze.util.Position;

import java.util.*;

import static de.tum.cit.fop.maze.rendering.Panel.*;
import static de.tum.cit.fop.maze.tiles.TileType.GROUND;
import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.PositionUnit.*;
import static java.lang.Math.*;


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
    public LevelManager levels; // Tile system for the map
    private final Key key;
    private final TextureRegion keyRegion;
    private final Array<Collectibles> collectibles;
    private final Array<Portal> portals;

    private final SpotlightEffect spotlightEffect;

    private final ShaderProgram shader;
    private final Stage stage1;

    // Show all the variables in the bottom-left corner here
    // Variables to show, stored in a map (LinkedHashMap preserves the order)
    private final Map<String, Float> variablesToShow = new LinkedHashMap<>();
    private final InputMultiplexer inputMultiplexer = new InputMultiplexer();

    private Animation<TextureRegion> playerAnimation;
    private Animation<TextureRegion> enemyAnimation;

    // Timer to track how long the stamina bar should stay visible after refill
    private static final float STAMINA_DISPLAY_TIME = 1f; // Duration to show stamina bar in seconds
    private float staminaTimer = STAMINA_DISPLAY_TIME; // set the timer to max first to prevent from showing at the very beginning

    private static final float TIMER = 180f;
    private float timer = TIMER;


    private boolean isPaused;
    private boolean isTutorial;

    private final int totalCoins; // total maximal number of coins that the player should get

    private final SelectLevelScreen selectLevelScreen;
    private final TooltipManager tooltipManager;


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

        // We use an InputMultiplexer instead of only stage or "this",
        // since both stage1 (for intro panel) and the GameScreen (for scrolling) handle inputs
        inputMultiplexer.addProcessor(stage1); // the stage is for the intro panel
        inputMultiplexer.addProcessor(this);  // used to detect mouse scrolls
        Gdx.input.setInputProcessor(stage1);

        // Load textures for HUD
        hudObjectRenderer = new ElementRenderer("original/objects.png");

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");
        shapeRenderer = new ShapeRenderer();
        tooltipManager = new TooltipManager();

        // initialize game world elements
        levels = new LevelManager(game);

        isTutorial = false;
        TiledMap tiledMap;
        Gdx.app.log("Constructor GameScreen", "Game Level upon creation of GameScreen: " + game.getGameLevel());
        if (game.getGameLevel() <= TOTAL_LEVELS && game.getGameLevel() > 0){
            tiledMap = levels.loadTiledMap("maps/level-"+game.getGameLevel()+"-map.properties", Gdx.files.internal("tilesets/level"+ game.getGameLevel()+"_tileset.png").path(), Gdx.files.internal("tilesets/level"+ game.getGameLevel()+"_obstacles.png").path());
        }
        else if (game.getGameLevel() == 0){
            isTutorial = true;
            tiledMap = levels.loadTiledMap("maps/level-0-map.properties", Gdx.files.internal("tilesets/level1_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
        }
        else{
            tiledMap = levels.loadTiledMap("maps/level-1-map.properties", Gdx.files.internal("tilesets/level1_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
        }
        createInstructionPanel();

        // Initialize the key. Only after we lod the tiled map, we can access the key's position
        Position keyPosition = levels.getKeyTilePosition().convertTo(PIXELS);
        float keyX = keyPosition.getX();
        float keyY = keyPosition.getY();
        key = new Key(keyX, keyY, TILE_SIZE,TILE_SIZE,10,9,TILE_SCREEN_SIZE, TILE_SCREEN_SIZE, game);
        // After loading the tiles,
        // get the array of tiles from our tile generator: levels.getTileset()
        // and then get the texture region where our key is at ... ?
        keyRegion = levels.getTileset()[TileType.KEY.getId()];

        collectibles = new Array<>();
        spawnCollectibles();

        portals = new Array<>();
        spawnPortal();

        // Set up map renderer
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap,  (float) TILE_SCREEN_SIZE / TILE_SIZE); // Scale tiles, so like unitScale is times how many

        // initialize player at entrance position
        player = new Player(
                levels.entrance.getTileX(),
                levels.entrance.getTileY(),
                16, 32, 12, 18, 64f, 128f, 10f,
                this, levels);//"this" is already a game screen


        // for whatever that requires touching the player
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){
            enemy.init(player);
        }
        for (Collectibles collectible : iterate(collectibles)){
            collectible.init(player, game.getSoundEffectKey());
        }

        for (Portal portal : iterate(portals)){
            portal.init(player, true, 20f);
        }

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

        if (!isTutorial)
            this.totalCoins = 5;
        else this.totalCoins = 1;

        this.selectLevelScreen = new SelectLevelScreen(game, "previous screen", game.getGameScreen());
    }

    /**
     * Identifies all empty tiles on the map and prepares them for spawning collectibles.
     *
     * <p>This method iterates through the 2D array of tiles on the map, checking each tile's type.
     * If the tile is of type {@code OTHER} or is {@code null}, it is considered an empty tile and
     * added to a list of potential positions for spawning collectibles.
     *
     * <p>The positions of these empty tiles are stored as {@link Position} objects in an array,
     * ready for further processing.
     */
    private void spawnCollectibles() {
        // Get the 2D array of tiles
        // to find all "OTHER" tiles
        Array<Position> emptyTiles = getEmptyTiles(levels);

        int numberOfHearts = (int) (sqrt(horizontalTilesCount * verticalTilesCount) / 10);
        generateCollectibles(emptyTiles, Collectibles.Type.HEART, numberOfHearts, 16, 11, 11, 2.5f);
        generateCollectibles(emptyTiles, Collectibles.Type.PRETZEL, numberOfHearts, 32,28, 27,72/28f);
        generateCollectibles(emptyTiles, Collectibles.Type.GESUNDHEITSKARTE, 1, 32,27,18,72/28f);

        generateCollectibles(emptyTiles, Collectibles.Type.COIN, 5, 16,11, 11,2.5f);

        generateCollectibles(emptyTiles, Collectibles.Type.STAMINA, 1, 32,16,22, 2.5f);
    }

    /**
     * Retrieves all empty tile positions from the level map.
     *
     * <p>This method iterates through the tile map of the given {@link LevelManager}
     * and collects positions where the tile is either {@code GROUND} or {@code null}.
     * These positions represent areas where no obstacles are present.</p>
     *
     * @param levels The {@link LevelManager} instance containing the tile map.
     * @return An {@link Array} of {@link Position} objects representing empty tiles.
     */
    private static Array<Position> getEmptyTiles(LevelManager levels) {
        Array<Position> emptyTiles = new Array<>();
        for (int x = 0; x < levels.getTileEnumOnMap().length; x++) {
            for (int y = 0; y < levels.getTileEnumOnMap()[x].length; y++) {
                TileType tileType = levels.getTileEnumOnMap()[x][y];
                if ((tileType == GROUND) || tileType == null) {
                    emptyTiles.add(new Position(x, y, TILES));
                }
            }
        }
        return emptyTiles;
    }

    /**
     * Generates a specified number of collectibles at random positions from a list of empty tiles.
     *
     * <p>The method selects random positions from the provided {@code emptyTiles} array, ensuring no duplicate
     * positions are used. It then creates collectibles of the specified type and adds them to the game world.
     *
     * <p>The following steps are performed:
     * <ul>
     *     <li>Determine the number of collectibles to generate based on the available empty tiles and the requested amount.</li>
     *     <li>Randomly select positions, avoiding duplicates by removing them from the {@code emptyTiles} array.</li>
     *     <li>Create collectibles at the selected positions with the specified size and type.</li>
     * </ul>
     *
     * @param emptyTiles       an array of positions where collectibles can be placed
     * @param type             the type of collectibles to generate
     * @param numberToGenerate the desired number of collectibles to generate
     * @param frameSize        the original size of the collectible's texture region frame in pixels
     * @param hitboxWidth      the hitbox width of the original collectible image in pixels
     * @param hitboxHeight     the hitbox height of the original collectible image in pixels
     * @param scale            scaling for the size of the collectible's frame as it appears on the screen
     */
    private void generateCollectibles(Array<Position> emptyTiles, Collectibles.Type type, int numberToGenerate, int frameSize, int hitboxWidth, int hitboxHeight, float scale) {
        int collectiblesToGenerate = (!isTutorial) ? (min(numberToGenerate, emptyTiles.size)) : 1;

        List<Position> occupiedSectionIndexes = new ArrayList<>();

        for (int i = 0; i < collectiblesToGenerate; i++) {
            int randomIndex = findRandomIndex(emptyTiles, collectiblesToGenerate, occupiedSectionIndexes, type);
            Position position = emptyTiles.removeIndex(randomIndex).convertTo(PIXELS); // Remove selected position to avoid duplicates
            float worldX = position.getX();
            float worldY = position.getY();

            // Generate a collectible at the selected position
            collectibles.add(new Collectibles(worldX, worldY, frameSize, frameSize, hitboxWidth, hitboxHeight,
                    frameSize * scale, frameSize * scale, type, game));
        }
    }

    /**
     * Generates a specified number of portals at random empty tile positions.
     *
     * <p>This method selects random positions from the provided list of empty tiles
     * and places portal objects at those locations. Each selected position is removed
     * from the list to prevent duplicate portal placement.</p>
     *
     * @param emptyTiles     An {@link Array} of {@link Position} objects representing available empty tiles.
     * @param numberToGenerate The number of portals to generate.
     * @param width         The width of the portal in pixels.
     * @param height        The height of the portal in pixels.
     * @param hitboxWidth   The width of the portal's hitbox.
     * @param hitboxHeight  The height of the portal's hitbox.
     * @param sizeOnScreen  The size of the portal as it appears on the screen.
     */
    private void generatePortals(Array<Position> emptyTiles, int numberToGenerate, int width, int height, int hitboxWidth, int hitboxHeight, float sizeOnScreen) {
        int portalsToGenerate = min(numberToGenerate, emptyTiles.size);

        for (int i = 0; i < portalsToGenerate; i++) {
            // Randomly select a tile index
            int randomIndex = MathUtils.random(emptyTiles.size - 1);
            Position position = emptyTiles.removeIndex(randomIndex).convertTo(PIXELS); // Remove selected position to avoid duplicates
            float x = position.getX();
            float y = position.getY();

            // Generate a portal at the selected position
            portals.add(new Portal(levels, x, y, width, height, hitboxWidth, hitboxHeight, sizeOnScreen, sizeOnScreen, game));
            Gdx.app.log("Generate Portals", "Portal Position: " + x + ", " + y);
        }
    }


    /**
     * Finds a random index in the list of empty tiles while ensuring that no duplicate sections are selected.
     *
     * <p>This method selects a random position from the provided list of empty tiles and ensures that
     * the section containing the selected tile is not already occupied. Once a valid section is found,
     * it marks the surrounding tiles as occupied to avoid overlapping collectible placements.</p>
     *
     * @param emptyTiles             An {@link Array} of {@link Position} objects representing available empty tiles.
     * @param collectiblesToGenerate The total number of collectibles to generate.
     * @param occupiedSectionIndexes A {@link List} of {@link Position} objects representing sections that are already occupied.
     * @param type                   The type of collectible being placed.
     * @return The randomly selected index from the empty tiles.
     */
    public int findRandomIndex(Array<Position> emptyTiles, int collectiblesToGenerate, List<Position> occupiedSectionIndexes, Collectibles.Type type){
        // prevent duplicate sections
        int randomIndex;
        Position sectionIndex;
        // Find an unused section index
        do {
            randomIndex = MathUtils.random(emptyTiles.size - 1);
            sectionIndex = getSectionIndex(emptyTiles.get(randomIndex), collectiblesToGenerate);
        } while (occupiedSectionIndexes.contains(sectionIndex));

        occupiedSectionIndexes.add(sectionIndex);
        for (int j=-1;j<=1;j+=2) occupiedSectionIndexes.add(new Position(sectionIndex.getTileX() + j, sectionIndex.getTileY(),TILES));//int[]{sectionIndex[0] + j, sectionIndex[1]});
        for (int j=-1;j<=1;j+=2) occupiedSectionIndexes.add(new Position(sectionIndex.getTileX(), sectionIndex.getTileY() + j,TILES));//int[]{sectionIndex[0],sectionIndex[1] + j});

        return randomIndex;
    }

    /**
     * Determines the section index for a given position based on the number of collectibles to generate.
     *
     * <p>This method divides the game map into sections and calculates which section a given position belongs to.
     * The number of sections is determined dynamically based on the number of collectibles, ensuring
     * collectibles are evenly distributed across the map.</p>
     *
     * @param position               The {@link Position} of the tile whose section index is to be found.
     * @param collectiblesToGenerate The number of collectibles to generate, which affects the section size.
     * @return A {@link Position} representing the section index (normalized to a grid) where the tile is located.
     */
    public Position getSectionIndex(Position position, int collectiblesToGenerate) {
        int mapWidth = horizontalTilesCount;
        int mapHeight = verticalTilesCount;
        int sectionSideCount = ((collectiblesToGenerate + 5) / 2); // floor(((x+3)/2)^2)^{2} > x
        int sectionWidth = mapWidth / sectionSideCount;
        int sectionHeight = mapHeight / sectionSideCount;
        return new Position((float) (position.getTileX() / sectionWidth), (float) (position.getTileY() / sectionHeight), TILES);
    }

    /**
     * Creates and displays an instruction panel with introductory game information about the storyline and the level.
     *
     * <p>The panel features a background image, a title, and a multi-step instruction sequence that updates
     * when the "Continue" button is clicked. The panel disappears after two clicks, transitioning to the
     * introduction panel.</p>
     *
     * <p>The first set of instructions is retrieved from {@link #getInstructionsText1()}, and upon clicking
     * the "Continue" button, the text updates to the second set of instructions from {@link #getLabelForInstructionsText2()}.
     * If the second set is empty, the panel closes immediately.</p>
     */
    public void createInstructionPanel(){
        NinePatchDrawable backgroundDrawable = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/introduction.png"),
                86, 86, 98, 98);
        Panel instructionPanel = new Panel(stage1, backgroundDrawable, game, 0.8f, 0.8f);
        instructionPanel.init();

        String levelName = levels.getProperties("levelName"); // test

        instructionPanel.addLabel((levelName.isEmpty()) ? "Introduction" : levelName, game.getSkin(), "fraktur", 1, 40);
        String instructionsText1 = getInstructionsText1();

        Label label = instructionPanel.addLabel(instructionsText1, game.getSkin(), "black" , 1f, 20);

        final int[] clickCount = {0}; // has to be an array
        instructionPanel.addButton("Continue", game.getSkin(), new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickCount[0]++;

                String instructionsText2 = getLabelForInstructionsText2();

                if (clickCount[0] == 1) {
                    if (instructionsText2.isEmpty()) {
                        clickCount[0]++;
                    }
                    label.setText(instructionsText2);
                }
                if (clickCount[0] == 2) {
                    instructionPanel.getTable().remove();
                    createIntroPanel();
                }
            }});
    }

    /**
     * Retrieves the first set of instructional text for the level.
     *
     * <p>This method fetches the instructional text stored in the level properties under the key
     * <code>"instructionsText1"</code> and replaces any escaped newline characters (<code>"\n"</code>)
     * with actual newlines for proper formatting.</p>
     *
     * @return The formatted instructional text for the level.
     */
    private String getInstructionsText1() {
        String s = levels.getProperties("instructionsText1");
        return s.replace("\\n", "\n");
    }

    /**
     * Retrieves the second set of instructional text for the level.
     *
     * <p>This method fetches the instructional text stored in the level properties under the key
     * <code>"instructionsText2"</code> and replaces any escaped newline characters (<code>"\n"</code>)
     * with actual newlines for proper formatting.</p>
     *
     * @return The formatted second instructional text for the level.
     */
    private String getLabelForInstructionsText2(){
        String s = levels.getProperties("instructionsText2");
        return s.replace("\\n", "\n");
    }


    /**
     * Creates and displays the introduction panel with game general instructions.
     *
     * <p>This method initializes a panel with a background image and provides introductory
     * instructions for the player, including movement controls, objectives, and hazards to avoid.
     * The player can start the game by clicking the "Start now" button or pressing the space bar.</p>
     *
     * <ul>
     *     <li>Displays the level name if available; otherwise, shows "Game Instructions".</li>
     *     <li>Lists movement controls, key collection mechanics, and enemy/trap avoidance.</li>
     *     <li>Allows progression by clicking the "Start now" button or pressing space.</li>
     * </ul>
     *
     * <p>If the game is on level 1, an additional label informs the player that they can skip using the space bar.</p>
     */
    public void createIntroPanel(){
        NinePatchDrawable backgroundDrawable = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/introduction.png"),
                86, 86, 98, 98);
        Panel introPanel = new Panel(stage1, backgroundDrawable, game, 0.8f, 0.8f);
        introPanel.init();

        String levelName = levels.getProperties("levelName");
        introPanel.addLabel((levelName.isEmpty()) ? "Game Instructions" : levelName, game.getSkin(), "fraktur", 1, 40);

        introPanel.addLabel("Move using W, A, S, D or arrow keys.", game.getSkin(), "black", 1f, 20);
        introPanel.addLabel("Hold Shift key to sprint.", game.getSkin(), "black", 1f, 20);
        introPanel.addLabel("Collect the key to unlock exits.", game.getSkin(), "black", 1f, 20);
        introPanel.addLabel("Avoid enemies and traps!", game.getSkin(), "black", 1f, 20);

        introPanel.addButton("Start now", game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                introPanel.proceedToGame(game, player, levels);
                currentTutorialStage = TutorialStage.EXIT_ARROW;
            }
        });

        introPanel.addListener(ifSpaceKeyPressed(() -> {
            introPanel.proceedToGame(game, player, levels);
            currentTutorialStage = TutorialStage.EXIT_ARROW;
        }));

        if (game.getGameLevel() == 1)
            introPanel.addLabel("[OR YOU CAN ALWAYS PRESS SPACE BAR TO SKIP]", game.getSkin(), "black", 1, 0);
    }

    /**
     * Creates and displays the pause panel in the game.
     *
     * <p>This method initializes a pause menu panel with a background image and multiple
     * interactive buttons that allow the player to resume, access options, select a level,
     * return to the main menu, or exit the game.</p>
     *
     * <ul>
     *     <li>Displays a "Game Paused" label.</li>
     *     <li>Provides a "Resume" button to continue the game.</li>
     *     <li>Includes an "Options" button to navigate to the options panel.</li>
     *     <li>Offers a "Select Level" button to change levels.</li>
     *     <li>Allows returning to the main menu with the "Back to Menu" button.</li>
     *     <li>Provides an "Exit Game" button to quit the game.</li>
     * </ul>
     */
    public void createPausePanel() {
        NinePatchDrawable background = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/pause.png"),
                45+17, 45+17, 45+37, 45+37);
        Panel pausePanel = new Panel(stage1, background, game, 0.9f, 0.9f);
        pausePanel.init();

        pausePanel.addLabel("Game Paused", game.getSkin(), "fraktur", 1, 80);

        pausePanel.addButton("Resume", game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                pausePanel.clear();
                game.resume();
                isPaused = false;
            }
        });

        pausePanel.addButton("Options", game.getSkin(), new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                pausePanel.clear();
                createOptionPanel();
            }
        });

        pausePanel.addButton("Select Level", game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SelectLevelScreen(game, "Pause", game.getGameScreen()));

            }
        });

        pausePanel.addButton("Back to Menu", game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });

        pausePanel.addButton("Exit Game", game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.exitGame();
            }
        });
    }

    /**
     * Creates and displays the options panel in the game.
     *
     * <p>This method initializes an options menu where players can adjust
     * music and sound effects volume, mute/unmute sounds, and return to the
     * pause menu.</p>
     *
     * <ul>
     *     <li>Displays an "Options" label.</li>
     *     <li>Provides a slider to adjust the music volume.</li>
     *     <li>Provides a slider to adjust the sound effects volume.</li>
     *     <li>Includes a "Mute / Unmute" button to toggle sound settings.</li>
     *     <li>Offers a "Back" button to return to the pause menu.</li>
     * </ul>
     */
    public void createOptionPanel() {
        NinePatchDrawable background = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/introduction.png"),
                86, 86, 98, 98);
        Panel optionPanel = new Panel(stage1, background, game, 0.8f, 0.8f);
        optionPanel.init();

        optionPanel.addLabel("Options", game.getSkin(), "fraktur", 1, 80);

        // Add Music Volume Slider
        optionPanel.addSlider("Music Volume", 0, 1, game.getVolume(), 0.05f, game.getSkin(), "black",  new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = ((Slider) actor).getValue();
                game.setVolume(value);
                Gdx.app.log("OptionsScreen", "Music volume changed to: " + value);
            }
        });


        optionPanel.addSlider("Sound Effects Volume", 0, 1, game.getSoundManager().getVolume(), 0.05f, game.getSkin(), "black", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value2 = ((Slider) actor).getValue();
                game.getSoundManager().setVolume(value2);
                Gdx.app.log("OptionsScreen", "Sound effects volume changed to: " + value2);
            }

        });


        optionPanel.addButton("Mute / Unmute" , game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean isMuted = !game.isMuted();
                game.muteAll(isMuted);
                Gdx.app.log("OptionsScreen", "Mute toggled");
            }
        });

        optionPanel.addButton("Back", game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                optionPanel.clear();
                createPausePanel();
        }});
    }

    /**
     * Creates and displays the victory panel when the player wins a level.
     *
     * <p>This method initializes a victory panel that shows the player's success,
     * their score, and provides options for advancing to the next level or returning
     * to the main menu.</p>
     *
     * <ul>
     *     <li>Displays a "Victory!" label.</li>
     *     <li>Shows the player's score with the number of collected coins and the total coins.</li>
     *     <li>If not in tutorial mode, shows the "Next Level" button to proceed to the next level.</li>
     *     <li>Includes a "Back to Menu" button to return to the main menu.</li>
     *     <li>In tutorial mode, pressing space proceeds to the next level.</li>
     *     <li>If not in tutorial mode, displays a prompt to press space to continue.</li>
     * </ul>
     */public void createVictoryPanel() {
        NinePatchDrawable background = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/victory.png"),
                50, 50, 50, 50);
        Panel victoryPanel = new Panel(stage1, background, game, 0.8f, 0.6f);
        victoryPanel.init();
        isPaused = true;

        victoryPanel.addLabel("Victory!", game.getSkin(), "fraktur", 1, 80);

        String grade = calculateScore();
        String scoreText = "Score: " + grade + " (" + player.getCoins() + "/" + totalCoins + ")";
        if (!isTutorial) victoryPanel.addLabel(scoreText, game.getSkin(), "black", 1f, 40);

        Gdx.app.log("Victory", "Game Level: " + game.getGameLevel());
        if (game.getGameLevel() != 0) { // if is not tutorial
            victoryPanel.addButton("Next Level", game.getSkin(), new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    victoryPanel.proceedToNextLevel(game);
                }
            });
        }

        victoryPanel.addButton("Back to Menu", game.getSkin(), new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });

        if (isTutorial) return;

        victoryPanel.addLabel("[OR PRESS SPACE BAR TO CONTINUE]", game.getSkin(), "black", 1f, 40);

        victoryPanel.addListener(ifSpaceKeyPressed(() -> victoryPanel.proceedToNextLevel(game)));
    }

    /**
     * Calculates the player's score based on the number of coins collected.
     *
     * <p>The score is determined by comparing the number of coins the player has collected
     * with the total number of coins available in the game. The following scoring system is applied:
     * <ul>
     *     <li>"A" if the player has collected all the coins</li>
     *     <li>"B" if the player has collected all but one coin</li>
     *     <li>"C" if the player has collected all but two coins</li>
     *     <li>"D" if the player has collected all but three coins</li>
     *     <li>"F" if the player has collected fewer than three coins less than the total</li>
     * </ul>
     *
     * @return a string representing the player's score ("A", "B", "C", "D", or "F")
     */
    private String calculateScore(){
        // Calculate the score
        String score;

        if (player.getCoins() == totalCoins) {
            score = "A";
        } else if (player.getCoins() == totalCoins - 1) {
            score = "B";
        } else if (player.getCoins() == totalCoins - 2) {
            score = "C";
        } else if (player.getCoins() == totalCoins - 3) {
            score = "D";
        } else {
            score = "F";
        }
        return score;
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
        if ((isPaused) && (!isTutorial))
            return true;
        targetZoom += amountY * 0.1f; // Adjust sensitivity as needed
        clampZoomLevel(); // targetZoom = MathUtils.clamp(targetZoom, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL); // Clamp zoom level
        if (currentTutorialStage == TutorialStage.ZOOM) currentTutorialStage = TutorialStage.ESC_PAUSE;
        Gdx.app.debug("GameScreen", "mouse scrolled to adjust zoom");
        return true; // Return true to indicate the event was handled
    }


    /**
     * Handles user input for something throughout the whole game, like zooming and muting.
     */
    private void handleInput() {
        // Handle keys input for zooming
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) { // "+" key
            targetZoom -= 0.02f;
            if (currentTutorialStage == TutorialStage.ZOOM) currentTutorialStage = TutorialStage.ESC_PAUSE;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) { // "-" key
            targetZoom += 0.02f;
            if (currentTutorialStage == TutorialStage.ZOOM) currentTutorialStage = TutorialStage.ESC_PAUSE;
        }

        clampZoomLevel(); // Clamp to avoid extreme zoom level

        // Handle Mute
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { // Press 'M' to mute/unmute
            if (game.isMuted()) {
                game.muteBGM();
            } else {
                game.normalizeBGM();
            }
            Gdx.app.log("GameScreen", "Mute toggled: " + (game.isMuted() ? "ON" : "OFF"));
        }
    }

    /**
     * Renders the game screen during each frame update.
     *
     * <p>This method is responsible for handling the game's visual rendering, updating game logic,
     * and handling player input during the game. It also manages rendering of game elements such
     * as the player, enemies, collectibles, portals, and UI elements like the HUD, pause, and tutorial panels.</p>
     *
     * <ul>
     *     <li>Checks for game over conditions and proceeds accordingly.</li>
     *     <li>Clears the screen and updates the camera view.</li>
     *     <li>Handles movement and animation logic, including sinusoidal animations for moving text.</li>
     *     <li>Manages player input for movement, actions, and interactions.</li>
     *     <li>Updates all game entities (player, enemies, collectibles, portals) each frame.</li>
     *     <li>Draws game elements such as the map border, traps, collectibles, portals, and enemies.</li>
     *     <li>Renders additional game HUD elements like stamina, tutorial tooltips, and the HUD.</li>
     *     <li>Handles smooth zoom transitions and camera movements.</li>
     *     <li>Controls rendering of UI panels (pause, tutorial, etc.) and HUD overlays.</li>
     * </ul>
     *
     * @param delta The time (in seconds) since the last frame was rendered, used for smooth animations and timing.
     */
    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        handlePauseInput();
        // https://stackoverflow.com/questions/46080673/libgdx-game-pause-state-animation-flicker-bug
        // we couldn't stop drawing even if the game is paused

        if (player.getLives() <= 0) {
            if (isTutorial) {
                player.setLives(10);
                return;
            }
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
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)) {
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

        for (Portal portal : iterate(portals)) {
            portal.update(delta);
        }

        player.checkPortalCollisions(portals);
        game.checkExitToNextLevel(player);

        renderGameWorld();


        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawMapBorder();
        shapeRenderer.end();

        game.getSpriteBatch().begin();
        renderTrap();
        renderCollectibles();
        renderPortal();
        renderChasingEnemy();
        renderPlayer();
        renderArrow();
        renderKey();


        renderSpeechBubble();

        game.getSpriteBatch().end(); // Important to call this after drawing everything

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin();
        renderStamina();
        shapeRenderer.end();

        moveCamera();

        if (isTutorial) {
            updateTutorial(delta);
            renderTooltip();
        }

        stage1.act(delta);
        stage1.draw(); // stage1 is for the panels, like the intro panel and the pause panel


        renderHUD();
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * Draws a thick border around the tiled map using shapeRenderer
     */
    public void drawMapBorder() {
        shapeRenderer.setColor(Color.GRAY); // Set border color

        // Draw top border (horizontal)
        shapeRenderer.rect(0, getWorldHeight(), getWorldWidth(), TILE_SIZE);

        // Draw bottom border (horizontal)
        shapeRenderer.rect(0, -TILE_SIZE, getWorldWidth(), TILE_SIZE);

        // Draw left border (vertical)
        shapeRenderer.rect(-TILE_SIZE, -TILE_SIZE, TILE_SIZE, (getWorldHeight() + (2*TILE_SIZE)));

        // Draw right border (vertical)
        shapeRenderer.rect(getWorldWidth() , -TILE_SIZE, TILE_SIZE, (getWorldHeight() + (2*TILE_SIZE)));
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
        game.getSpriteBatch().end();

        // mapRenderer use another rendering batch, so we have to end the ones first, render the map, and then begin our spriteBatch again outside of this function
        mapRenderer.setView(camera);
        // this adds a darker shade to it, like night effect
        //mapRenderer.getBatch().setColor(0.5f, 0.5f, 0.5f, 1);
        mapRenderer.render(); // mapRenderer renders the map, also the layers or so the tiles
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
                    game.getCharacterIdleRegion(),
                    player.getOriginX(),
                    player.getOriginY(),
                    player.getWidthOnScreen(),
                    player.getHeightOnScreen()
            );
        }
        game.getSpriteBatch().setShader(null); // end the shader so we only shade the player
    }

    /**
     * Renders an arrow pointing towards the nearest exit.
     */
    private void renderArrow(){
        // Draw arrow that points at the exit
        Position exitPosition = null;
        if (!levels.exits.isEmpty())
            exitPosition = levels.getNearestExit(player.getX(), player.getY()).getTilePosition();

        float angle = getAngle(exitPosition);

        if (angle > 0) hudObjectRenderer.drawArrow(game.getSpriteBatch(), angle, player.getX(), player.getY());

    }


    /**
     * Renders the tooltip message near the player's position.
     *
     * <p>This method checks if there is a message to display and, if so, calculates the appropriate
     * position for the tooltip based on the player's current position and the camera's view. It then
     * draws the message on the screen at the clamped position, ensuring it stays within the screen bounds.</p>
     *
     * <ul>
     *     <li>Position of the tooltip is calculated based on the player's position.</li>
     *     <li>The tooltip is clamped to ensure it stays within the screen boundaries.</li>
     *     <li>Uses a batch and font renderer to display the tooltip message.</li>
     * </ul>
     *
     * @see TooltipManager
     * @see Player
     */
    private void renderTooltip(){
        if (tooltipManager.message != null) {
            float tooltipX = player.getX() + 50;//camera.position.x - 100; // Adjust to center near camera
            float tooltipY = player.getY() + 50;//camera.position.y + camera.viewportHeight / 2 - 20; // Top of the viewport

            float clampedX = MathUtils.clamp(tooltipX, 0, getWorldWidth() - font.getRegion().getRegionWidth() * min(2, tooltipManager.message.length() / 10));
            float clampedY = MathUtils.clamp(tooltipY, 0, getWorldHeight() - font.getCapHeight());

            tooltipManager.batch.setProjectionMatrix(camera.combined);
            tooltipManager.batch.begin();
            font.draw(tooltipManager.batch, tooltipManager.message, clampedX, clampedY);
            tooltipManager.batch.end();
        }
    }

    /**
     * Renders collectible items with appropriate animations based on their type.
     */
    private void renderCollectibles(){
        for (Collectibles collectible : iterate(collectibles)) {
            if (collectible.getType().equals(Collectibles.Type.HEART))
                collectible.render(game.getSpriteBatch(), game.getHeartAnimation().getKeyFrame(sinusInput/1.5f, true));
            else if (collectible.getType().equals(Collectibles.Type.PRETZEL)){
                collectible.render(game.getSpriteBatch(), game.getPretzelAnimation().getKeyFrame(sinusInput/1.5f, true));
            }
            else if (collectible.getType().equals(Collectibles.Type.GESUNDHEITSKARTE)){
                collectible.render(game.getSpriteBatch(), game.getGesundheitskarteRegion());
            }
            else if (collectible.getType().equals(Collectibles.Type.COIN)){
                collectible.render(game.getSpriteBatch(), game.getCoinAnimation().getKeyFrame(sinusInput/1.5f, true));
            }
            else if (collectible.getType().equals(Collectibles.Type.STAMINA)){
                collectible.render(game.getSpriteBatch(), game.getStaminaPotionAnimation().getKeyFrame(sinusInput/1.5f, true));
            }
        }
    }

    /**
     * Renders all the portals in the game, drawing them with an animation effect.
     *
     * <p>This method iterates through all the active portals and renders them to the screen. Each portal
     * is drawn with an animation frame that cycles based on the game's sinus input, creating a dynamic visual
     * effect for the portals.</p>
     *
     * <ul>
     *     <li>Each portal is rendered with an animation frame retrieved from the portal's animation sequence.</li>
     *     <li>The frame used is based on the sinus input, which creates a smooth, dynamic animation.</li>
     *     <li>The method uses the game's sprite batch to draw the portal images.</li>
     * </ul>
     *
     * @see Portal
     * @see Game
     * @see SpriteBatch
     * @see Animation
     */
    private void renderPortal(){
        for (Portal portal : iterate(portals)) {
            portal.render(game.getSpriteBatch(), game.getPortalAnimation().getKeyFrame(sinusInput/1.5f, true));

        }
    }

    /**
     * Displays the speech bubble I wrote
     * Specify the duration (5 seconds) of the player can speak and shows the text.
     */
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

        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){
            if (enemy.canSpeak && !enemy.getSpeechText().isEmpty()) {
                enemy.getSpeechBubble().show(enemy.SPEAKING_ACTIVE_DURATION);
                enemy.canSpeak = false;
                continue;
            }

            enemy.say(enemy.getSpeechText(), game.getSpriteBatch(),
                    true, enemy.getSpeechBubble().getElapsedTime(), 0.03f);
        }
    }

    /**
     * Handles input for pausing and resuming the game.
     *
     * <p>The game pauses when the Escape key is pressed, and resumes when the Enter key is pressed while paused.
     */
    private void handlePauseInput(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isPaused) {
            pause();
        }

        // If the Enter key is pressed and the game is paused, resume the game
        if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && isPaused && !isTutorial) {
            resume();
            tooltipManager.message = null; // Clear the tooltip
        }
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

    /**
     * Renders all traps on the map.
     */
    private void renderTrap(){
        for (Trap trap : iterate(levels.traps)){ // for (trap : levels.traps){
            trap.draw(game.getSpriteBatch());
        }
    }

    /**
     * Spawns a new portal in the game by generating a portal at a random empty tile.
     *
     * <p>This method first retrieves a list of empty tiles on the map, then generates a portal at one of these
     * empty tiles. The portal's position, size, and hitbox dimensions are specified in the method's parameters.</p>
     *
     * <ul>
     *     <li>The method uses {@link #getEmptyTiles(LevelManager)} to get a list of empty tiles.</li>
     *     <li>The portal is generated using {@link #generatePortals(Array, int, int, int, int, int, float)}.</li>
     *     <li>A message is logged to the console when the portal is spawned and when it has been successfully generated.</li>
     * </ul>
     *
     * @see #getEmptyTiles(LevelManager)
     * @see #generatePortals(Array, int, int, int, int, int, float)
     */
    private void spawnPortal() {
        Gdx.app.log("GameScreen", "Spawning portal...");
        Array<Position> emptyTiles = getEmptyTiles(levels);
        generatePortals(emptyTiles, 1, 64, 64, 48, 48, 96);
        Gdx.app.log("GameScreen", "Portals generated: " + portals.size);
    }

    /**
     * Renders chasing enemies with appropriate animations based on their movement direction.
     */
    private void renderChasingEnemy(){
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){ // for (ChasingEnemy enemy : levels.chasingEnemies)
            switch (enemy.getPreviousDirection()){
                case up -> enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("up");
                case down -> enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("down");
                case left -> enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("left");
                case right -> enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("right");
            }

            enemy.draw(game.getSpriteBatch(), enemyAnimation.getKeyFrame(sinusInput, true));
        }
    }

    /**
     * Draws the player's stamina wheel,
     * including an overflow arc if potion is collected.
     */
    private void renderStamina(){
        float currentStamina = player.getStamina();

        Gdx.gl.glLineWidth((int)(5f / camera.zoom)); // Set line width for better visibility
        int staminaRadius = 12;
        float offsetX = -player.getHitboxWidthOnScreen() / 2 - 5;
        float offsetY = player.getHitboxHeightOnScreen() / 2 + 5;
        float staminaX = player.getX() + offsetX;// + staminaRadius / 2;
        float staminaY = player.getY() + offsetY;// - staminaRadius;

        if (currentStamina >= Player.maxStamina) {
            // Start the timer if stamina is full
            staminaTimer += Gdx.graphics.getDeltaTime();
            if (currentStamina > Player.maxStamina + 2){
                float angle = ((player.getStamina() - Player.maxStamina) / Player.maxStamina) * 360f; // Calculate the angle based on stamina
                drawCircularSector(shapeRenderer, new Color(0x16b816ff), staminaX, staminaY, staminaRadius * 1.6f, angle);

            }
            if (player.getCurrentStaminaMultiplier() == 1 && // if the current multiplier is 1 (either didn't collect potion or extra stamina has used up)
                    staminaTimer > STAMINA_DISPLAY_TIME) { // and if the time has passed,
                // Hide the stamina bar if the timer exceeds the display duration
                return;

            }
        } else {
            // Reset the timer when stamina is not full
            staminaTimer = 0f;
            player.setCurrentStaminaMultiplier(1); // immediately set the multiplier back right after the player uses up the extra stamina
        }


        // Draw the background circle (full arc for reference)
        drawCircularSector(shapeRenderer, Color.DARK_GRAY, staminaX, staminaY, staminaRadius, 360);

        // Draw the stamina arc
        float angle = (player.getStamina() / Player.maxStamina) * 360f; // Calculate the angle based on stamina
        drawCircularSector(shapeRenderer, Color.LIME, staminaX, staminaY, staminaRadius, angle);
    }

    /**
     * Draws a filled circular sector (a portion of a circle) on the screen using the given parameters.
     *
     * <p>The method estimates the number of segments required to draw a smooth arc based on the specified
     * radius and angle. It uses a formula where the arc length is proportional to the radius. This helps
     * ensure that the drawn arc appears smooth, regardless of the sector's size.</p>
     *
     * <p>If the angle is exactly 360 degrees, the method draws an additional small strip to fill any potential gap.</p>
     *
     * @param shapeRenderer The {@link ShapeRenderer} used to draw the sector.
     * @param color The {@link Color} to fill the sector.
     * @param x The x-coordinate of the center of the circle.
     * @param y The y-coordinate of the center of the circle.
     * @param radius The radius of the circle.
     * @param angle The angle of the sector in degrees, where 360 is a full circle.
     */
    // Helper method to calculate segments needed automatically
    private static void drawCircularSector(ShapeRenderer shapeRenderer, Color color, float x, float y, float radius, float angle){
        // the estimation of: "the number of segments", needed for a smooth an arc, provided by LibGDX, just sucks
        int segments = max(1, (int)((angle / (360.0f/(18 * radius))))); // arc length = 2πr ∝ r
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled); // filled by default
        shapeRenderer.setColor(color);
        shapeRenderer.arc(x, y, radius, 90 - angle, angle, segments); // Draw arc clockwise
        if (angle == 360) { // draw one little strip to fill the gap at 0 deg
            shapeRenderer.arc(x, y, radius, 90 - angle - 4, 8);
        }
    }

//       }



    /**
     * Renders the Heads-Up Display (HUD), including player health and collected coins.
     */
    private void renderHUD() {
        game.getSpriteBatch().setProjectionMatrix(hudCamera.combined); // HUD uses its own camera so that it does not follow the player and the position is fixed on the screen.
        game.getSpriteBatch().begin();

        variablesToShow.clear();
        variablesToShow.put("player.x", player.getX());
        variablesToShow.put("player.y", player.getY());
        variablesToShow.put("player.speed", player.getSpeed());
        variablesToShow.put("camera zoom", camera.zoom);
        variablesToShow.put("player.stamina", player.getStamina());

        game.getSpriteBatch().end();

        game.getSpriteBatch().begin();
        hudObjectRenderer.drawHearts(game.getSpriteBatch(), player.getLives(), 20, Gdx.graphics.getHeight() - 26f - 20, 32, 2);

        String coinText = "Coins: " + player.getCoins() + "/" + totalCoins;
        font.draw(game.getSpriteBatch(), coinText, 20, Gdx.graphics.getHeight() - 50);

        String keyStatus = key.isCollected() ? "Key Collected!" : "Find The Key!";
        font.draw(game.getSpriteBatch(), keyStatus, 20, Gdx.graphics.getHeight() - 80);



        if (!isPaused && !isTutorial && levels.isProperties("timer")){
            timer -= (Gdx.graphics.getDeltaTime());
            if (timer <= 0){
                game.goToGameOverScreen();
            }
        }

        if (levels.isProperties("timer")){
            String time = "Time : " + (float)Math.round(timer * 100f) / 100f; // round to 2nd digit
            font.draw(game.getSpriteBatch(), time, 20, Gdx.graphics.getHeight() - 120);
        }
        game.getSpriteBatch().end();
    } // timer is only activated and visible at level 6

    /**
     * Renders a spotlight effect at the specified position
     * It can be used to introduce some new feature and have the user focus on it
     * or create a night time effect which only areas around the player is brightened
     *
     * @param x the x-coordinate of the spotlight center
     * @param y the y-coordinate of the spotlight center
     * @param spotlightRadius the radius of the spotlight circle
     */
    private void renderSpotlightEffect(float x, float y, float spotlightRadius, float secondSpotlightScale, float opacity) {
        Position screenCoordinates = getScreenCoordinates(x, y);
        float xOnScreen = screenCoordinates.getX();
        float yOnScreen = screenCoordinates.getY();
        //Gdx.app.log("GameScreen", "screen x: " + xOnScreen + "; screen y: " + yOnScreen);
        spotlightEffect.render(camera, x, y, spotlightRadius, secondSpotlightScale, opacity);
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
        moveCamera();
        player.resume();

        resizePanels();

        stage1.getViewport().update(width, height, true); // This keeps the stage's coordinate system consistent.
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /**
     * Resizes and repositions all actors within the `stage1` based on the current screen size.
     * Actors that are instances of {@link ResizeableTable} are resized and repositioned according to
     * their specified width and height ratios. For other actors, the default size and position are applied.
     *
     * <p>This method ensures that panels and their contents scale dynamically based on the screen resolution.
     * For `ResizeableTable`, each cell's width is also adjusted to ensure proper layout, with some padding applied
     * to label cells to prevent overflow.</p>
     *
     * <p>The resizing mechanism accounts for both the width and height ratios of the panels, adjusting their size
     * and position relative to the screen dimensions.</p>
     */
    public void resizePanels(){
        for (var actor : iterate(stage1.getActors())){
            if (actor instanceof ResizeableTable table){ // this is the case...
                float w = table.getWidthRatio();
                float h = table.getHeightRatio();
                actor.setSize(Gdx.graphics.getWidth() * w,Gdx.graphics.getHeight() * h);
                actor.setPosition(Gdx.graphics.getWidth() * (1-w)/2, Gdx.graphics.getHeight() * (1-h)/2);

                for (Cell<?> cell : iterate(table.getCells())) {
                    Actor cellActor = cell.getActor();
                    if (cellActor instanceof Label) {
                        cell.width(Gdx.graphics.getWidth() * w * 0.8f * 0.9f); // Adjust width dynamically // 0.9 is the padding
                    }
                }
            }
            else { // default size
                Gdx.app.log("Resize", "actor is not a panel: "+ actor.toString());
                actor.setSize(Gdx.graphics.getWidth() * 0.8f,Gdx.graphics.getHeight() * 0.8f);
                actor.setPosition(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.1f);
            }
        }
    }

    /**
     * Adjusts (clamps) the zoom level of the game camera to ensure it remains within
     * defined bounds based on the number of visible tiles on the screen and the desired
     * aspect ratio. This ensures a consistent and fair gameplay experience regardless of
     * screen size or resolution.
     * <br>
     * The zoom levels are calculated dynamically using:
     * - The screen width and height in pixels.
     * - The size of a single tile in pixels (TILE_SCREEN_SIZE).
     * - The desired number of tiles visible at minimum and maximum zoom levels (MIN_ZOOM_TILES_COUNT and MAX_ZOOM_TILES_COUNT).
     */
    public void clampZoomLevel(){
        int maxZoomTilesCount = MAX_ZOOM_TILES_COUNT;
        if (isTutorial)
            maxZoomTilesCount = (int) max(MIN_ZOOM_TILES_COUNT,  horizontalTilesCount * 1.2);
        // Calculate how many tiles are visible horizontally and vertically based on the current screen dimensions.
        // Math.max is to prevent NaN when the window is minimized
        float numTilesOnScreenWidth = (float) max(Gdx.graphics.getWidth(), MIN_WINDOW_WIDTH) / TILE_SCREEN_SIZE;
        float numTilesOnScreenHeight = (float) max(Gdx.graphics.getHeight(), MIN_WINDOW_HEIGHT) / TILE_SCREEN_SIZE;

        // Adjusted for the screen's aspect ratio (16:9 as a reference). (which means 16:9 is the aspect ratio that can see the most tiles)
        // We take the larger dimension (either width or adjusted height) as the constraint.
        float minZoomLevel = 1.0f * MIN_ZOOM_TILES_COUNT / max(
                numTilesOnScreenWidth,
                numTilesOnScreenHeight * 16 / 9 // Adjust height for 16:9 aspect ratio.
        );
        float maxZoomLevel = 1.0f * maxZoomTilesCount / max(
                numTilesOnScreenWidth,
                numTilesOnScreenHeight * 16 / 9 // Adjust height for 16:9 aspect ratio.
        );
        // Clamp the target zoom level to ensure it remains within the calculated bounds.
        targetZoom = MathUtils.clamp(targetZoom, minZoomLevel, maxZoomLevel);
    }

    /**
     * Checks the current tutorial stage and updates the tutorial task accordingly.
     * Based on the player's progress, it determines which task the player is on
     * (moving, collecting the key, or reaching the exit) and displays the appropriate tooltip
     * with a helpful message to guide the player through the tutorial.
     *
     * <p>The method also triggers a spotlight effect and updates the tutorial stage
     * to reflect the current task:</p>
     * <ul>
     *     <li>Moves: Displays a message prompting the player to move using WASD or arrow keys,
     *     and provides a sprinting tip.</li>
     *     <li>Key: Displays a message prompting the player to find and collect the key.</li>
     *     <li>Exit: Displays a message instructing the player to go to the exit to complete the level.</li>
     * </ul>
     *
     * <p>This method is called periodically to track the player's progression through the tutorial
     * and provide dynamic feedback during gameplay.</p>
     */
    private void checkTutorialTasks() {
        if (!player.hasMoved) {
            currentTutorialStage = TutorialStage.MOVE;
            renderSpotlightEffect(0,0,0, 0.8f, 0.5f);
            tooltipManager.show("Move using WASD or the arrow keys.\nTip: Hold SHIFT to sprint and leave\nthe enemies behind--but watch your stamina wheel!");
        } else if (!key.isCollected()) {
            currentTutorialStage = TutorialStage.KEY;
            tooltipManager.show("Find and collect the key.");
        } else if (!player.hasReachedExit) {
            currentTutorialStage = TutorialStage.EXIT;
            tooltipManager.show("Go to the exit \nto complete the level.");
        }
    }


    /**
     * Checks various game events related to the player's proximity to important objects
     * and triggers spotlight effects with corresponding messages to guide the player.
     * The method detects the player's closeness to different interactive objects such as:
     * key, portals, traps, enemies, and collectibles. When the player is near any of these objects,
     * it displays a spotlight effect and a tooltip with helpful information.
     *
     * <p>Proximity thresholds for triggering events are defined as:</p>
     * <ul>
     *     <li>Key: 150 units</li>
     *     <li>Portal: 200 units</li>
     *     <li>Trap: 170 units</li>
     *     <li>Enemy: 230 units</li>
     *     <li>Collectible: 110 units</li>
     * </ul>
     *
     * <p>The method handles the following events:</p>
     * <ul>
     *     <li>If the player is close to the key, it triggers a spotlight effect and provides a message about the key.</li>
     *     <li>If the player is close to a portal, it triggers a spotlight effect and provides a warning about the portal.</li>
     *     <li>If the player is close to a trap, it triggers a spotlight effect and provides a warning about the trap.</li>
     *     <li>If the player is close to an enemy, it triggers a spotlight effect and provides a message about the enemy.</li>
     *     <li>If the player is close to a collectible, it triggers a spotlight effect and provides information about the collectible.</li>
     * </ul>
     *
     * <p>The spotlight effect draws attention to the relevant object and provides the player with useful guidance
     * to help them interact with objects in the game.</p>
     */
    private void checkForSpotlightEvents() {
        if (!player.hasMoved) return;

        // if player.isCloseToKey(150)
        if (player.isCloseTo(key, 150) && !key.isCollected()){
            tooltipManager.timer = 0f;
            triggerSpotlight(key.getX(), key.getY(), 70, "Collect the Key!\nThe key can look differently \ndepend on different levels.");
            return;
        }

        Portal portal = player.isCloseToPortals(200);
        if (portal != null){
            tooltipManager.timer = 0f;
            triggerSpotlight(portal.getX(), portal.getY(), 100, "A Portal could send you back \nto the entry point.");
            return;
        }

        // Example: Detect proximity to a trap
        Trap trap = player.isCloseToTraps(170);
        if (trap != null) {
            tooltipManager.timer = 0f; // If none of the cases. Reset the timer
            triggerSpotlight(trap.getX(), trap.getY(), 89, "Watch out for traps!");
            return;
        }

        // Example: Detect proximity to an enemy
        ChasingEnemy enemy = player.isCloseToEnemies(230);
        if (enemy != null){
            triggerSpotlight(enemy.getX(), enemy.getY(), 100, "An enemy is near!");
            return;
        }

        // Example: Detect proximity to an enemy
        Collectibles collectibles = player.isCloseToCollectibles(110);
        if (collectibles != null){
            tooltipManager.timer = 0f; // If none of the cases. Reset the timer
            triggerSpotlight(collectibles.getX(), collectibles.getY(), 68,
                    "Collect the " + capitalize(collectibles.getType().toString()) + "!\n" +
                            collectibles.getFunction());
        }

        //tooltipManager.timer = 0f; // If none of the cases. Reset the timer
    }

    /**
     * Triggers a spotlight effect at the specified position and radius, and displays a tooltip message to guide the player.
     * The spotlight effect is drawn to emphasize an important object or event, and a message is shown to the player
     * to provide information about it.
     *
     * <p>If the tooltip timer has reached the maximum allowed duration, the method will not trigger the spotlight effect
     * or display a message.</p>
     *
     * @param x        The x-coordinate of the spotlight center.
     * @param y        The y-coordinate of the spotlight center.
     * @param radius   The radius of the spotlight effect, which defines the area of focus.
     * @param message  The message to be displayed along with the spotlight effect, providing guidance to the player.
     *
     * <p>Note: This method assumes the existence of a TooltipManager instance to handle the display of messages.</p>
     */
    private void triggerSpotlight(float x, float y, float radius, String message) {
        //setPaused(true); // Pause the game
        if (tooltipManager.timer >= TooltipManager.TOOLTIP_DURATION) return;
        renderSpotlightEffect(x, y, radius, 0.8f, 1);
        tooltipManager.show(message); // Display a message
    }

    /**
     * Updates the tutorial progression, managing different stages of the tutorial and guiding the player through them.
     * The tutorial stages include instructions on movement, zoom, pausing, and interacting with key game elements such as the exit and key collection.
     *
     * <p>The method handles the rendering of spotlight effects, displaying messages through tooltips, and checking for user input
     * (e.g., pressing the Enter key to proceed or the Esc key to pause) in order to move through the tutorial stages.</p>
     *
     * @param delta The time in seconds since the last frame, used for updating the tutorial elements over time.
     */private void updateTutorial(float delta){
        switch (currentTutorialStage) {
            case EXIT_ARROW -> {
                renderSpotlightEffect(hudObjectRenderer.getArrowRotatedX(), hudObjectRenderer.getArrowRotatedY(), 20, 1, 1);

                // Show instructions to press Enter
                tooltipManager.show("This arrow indicate where the exit is. \nPress Enter to continue");
                this.pause(false);

                // Check for Enter key to proceed to the next phase of the tutorial
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    currentTutorialStage = TutorialStage.ZOOM;
                    this.resume();
                }
            }
            case ZOOM -> {
                renderSpotlightEffect(0,0,0, 0.8f, 0.5f);
                tooltipManager.show("Use the scroll wheel or '+'/'-' keys to zoom in and out.");
                for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){
                    enemy.pause();
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    currentTutorialStage = TutorialStage.ESC_PAUSE;
                    this.resume();
                }
            }
            case ESC_PAUSE -> {
                renderSpotlightEffect(0,0,0, 0.8f, 0.5f);
                tooltipManager.show("Press 'Esc' to pause the game.\nGot it? Press Enter to continue");
                this.pause(false);

                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    currentTutorialStage = TutorialStage.COMPLETE;
                    this.resume();
                }
            }
            case MOVE, KEY, EXIT, COMPLETE -> {
                checkTutorialTasks();
                if (!isPaused) {
                    checkForSpotlightEvents();
                }
            }
        }

        tooltipManager.update(delta);
    }

    /**
     * Enum representing the various stages of the tutorial in the game. Each stage has an associated order that defines the
     * progression of the tutorial. The stages range from introductory instructions to gameplay-related tasks.
     */
    public enum TutorialStage {
        INTRO       (0),
        EXIT_ARROW  (1),
        ZOOM        (2),
        ESC_PAUSE   (3),
        COMPLETE    (7),
        // v gameplay starts from here v
        MOVE        (4),
        KEY         (5),
        EXIT        (6);

        private final int stageOrder;

        /**
         * Constructor for the TutorialStage enum.
         *
         * @param stageOrder The order in which this tutorial stage occurs.
         */
        TutorialStage(int stageOrder){
            this.stageOrder = stageOrder;
        }

        /**
         * Gets the order of this tutorial stage.
         *
         * @return The stage order as an integer, where a lower number represents an earlier stage.
         */
        public int getStageOrder() {
            return stageOrder;
        }
    }

    private TutorialStage currentTutorialStage = TutorialStage.INTRO;

    /**
     * Manages the display of tooltips in the game. The tooltip system is responsible for showing messages to the player,
     * with a time limit for how long each message remains visible on the screen. The tooltip is hidden after a set duration,
     * or it can be manually hidden based on the tutorial stage or other conditions.
     */
    private class TooltipManager {
        private String message;
        private float timer;
        private static final float TOOLTIP_DURATION = 3f;

        private final SpriteBatch batch = new SpriteBatch();

        /**
         * Displays a new tooltip message. The message is shown on the screen for a limited time.
         * If the message does not contain the word "enemy", the timer is reset.
         *
         * @param message The message to be displayed in the tooltip.
         */
        public void show(String message) {
            this.message = message;
            if (!message.contains("enemy")) this.timer = 0f;
        }

        /**
         * Updates the tooltip's state. The timer increases as time passes, and when the timer exceeds the tooltip duration,
         * the message is hidden. Additionally, if the tutorial has progressed beyond the "MOVE" stage, the message is cleared
         * after the set duration.
         *
         * @param delta The time in seconds since the last frame, used to update the timer.
         */
        public void update(float delta) {
            if (message != null) timer += delta;
            if (currentTutorialStage.getStageOrder() > TutorialStage.MOVE.getStageOrder()
                    && timer > TOOLTIP_DURATION) message = null;
        }
    }

    /**
     * Pauses the game, optionally creating a pause panel.
     * This method is automatically called upon the window to lose the focus
     * (like user switches to another window or minimize the game window).
     * Also, the method is called when the user intends to pause the game
     * (like pressing the pause game button)
     *
     * @param createPausePanel true to create a pause panel, false otherwise
     */
    public void pause(boolean createPausePanel) {
        Gdx.app.log("GameScreen", "Game paused");
        // Stop processing input temporarily
        //Gdx.input.setInputProcessor(null); // Disable input handling during pause
        isPaused = true; // Set the game to "paused"

        if (!isTutorial || currentTutorialStage.getStageOrder() >= TutorialStage.MOVE.getStageOrder()) { // change to pause music
            game.getBackgroundMusic().pause();
            game.getPauseMusic().play();
        }

        player.pause();
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){
            enemy.pause();
        }

        if (createPausePanel) createPausePanel(); // Show the pause panel

        if (!isTutorial)
            Gdx.input.setInputProcessor(stage1); // Set input processor to stage1 (pause menu)
        else Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /**
     * Overloading method of {@link GameScreen#pause(boolean)} which defaults to creating a pause panel
     */
    @Override
    public void pause() { // Overloading method
        pause(true);
    }

    /**
     * Resumes the game, restoring input processing and gameplay state.
     * This method is automatically called upon the window to regain the focus
     * (like user switches back to the game window)
     * Also, the method is called when user intends to resume the game
     * (like pressing the resume game button)
     */
    @Override
    public void resume() {
        Gdx.app.log("GameScreen", "Game resumed");

        Gdx.input.setInputProcessor(inputMultiplexer);
        isPaused = false; // Set the game to unpaused

        //isEscKeySpotlightActive = false; // for the tutorial

        game.getBackgroundMusic().play();

        game.getPauseMusic().pause();

        player.resume();
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){
            enemy.resume();
        }
        stage1.clear(); // Clear the pause panel from the screen
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        font.dispose();
        //shapeRenderer.dispose();
        // i think we shouldn't even dispose the shapeRenderer, right? (else the program will exit unexpectedly)
        //mapRenderer.dispose();
        hudObjectRenderer.dispose();
        // disposing all disposables (such as Stage, Skin, Texture ... etc)
        stage1.dispose();
        shader.dispose();
    }

    public Key getKey() {
        return key;
    }

    public Array<Collectibles> getCollectibles() {
        return collectibles;
    }

    public Array<Portal> getPortals() {
        return portals;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isTutorial() {
        return isTutorial;
    }

    public TutorialStage getCurrentTutorialStage() {
        return currentTutorialStage;
    }

    public float getCountdownTimer() {
        return timer;
    }
}
