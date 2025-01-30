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
    TextureRegion keyRegion;
    private final Array<Collectibles> collectibles;
    private final Array<Portal> portals;

    private final SpotlightEffect spotlightEffect;

    private final ShaderProgram shader;
    private final Stage stage1;

    // Show all the variables in the bottom-left corner here
    // Variables to show, stored in a map (LinkedHashMap preserves the order)
    Map<String, Float> variablesToShow = new LinkedHashMap<>();
    InputMultiplexer inputMultiplexer = new InputMultiplexer();

    Animation<TextureRegion> playerAnimation;
    Animation<TextureRegion> enemyAnimation;

    // Timer to track how long the stamina bar should stay visible after refill
    private static final float STAMINA_DISPLAY_TIME = 1f; // Duration to show stamina bar in seconds
    private float staminaTimer = STAMINA_DISPLAY_TIME; // set the timer to max first to prevent from showing at the very beginning

    private boolean isPaused;
    private boolean isTutorial;

    private final int totalCoins; // total maximal number of coins that the player should get

    private final SelectLevelScreen selectLevelScreen;
    private final TooltipManager tooltipManager;

    private Slider volumeSlider;



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

        //game.setMuted(false);
        shapeRenderer = new ShapeRenderer();

        tooltipManager = new TooltipManager();

        // initialize game world elements
        levels = new LevelManager(game);

        isTutorial = false;
        TiledMap tiledMap;
        System.out.println("Game Level upon creation of GameScreen: " + game.getGameLevel());
        switch (game.getGameLevel()) {
            case 1,2,3,4,5 -> tiledMap = levels.loadTiledMap("maps/level-"+game.getGameLevel()+"-map.properties", Gdx.files.internal("tilesets/level"+ game.getGameLevel()+"_tileset.png").path(), Gdx.files.internal("tilesets/level"+ game.getGameLevel()+"_obstacles.png").path());
            case 6 -> tiledMap = levels.loadTiledMap("maps/level-"+game.getGameLevel()+"-map.properties", Gdx.files.internal("tilesets/level"+ game.getGameLevel()+"_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
            case 0 -> {
                isTutorial = true;
                tiledMap = levels.loadTiledMap("maps/level-0-map.properties", Gdx.files.internal("tilesets/level1_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
            }
            //case 3 -> tiledMap = levels.loadTiledMap("maps/level-3-map.properties", Gdx.files.internal("tilesets/level3_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
            //case 4 -> tiledMap = levels.loadTiledMap("maps/level-4-map.properties", Gdx.files.internal("tilesets/level4_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
            default -> tiledMap = levels.loadTiledMap("maps/level-1-map.properties", Gdx.files.internal("tilesets/level1_tileset.png").path(), Gdx.files.internal("tilesets/level1_obstacles.png").path());
        }

//        createIntroPanel();
        createInstructionPanel();

        // Initialize the key. Only after we lod the tiled map, we can access the key's position
        Position keyPosition = levels.getKeyTilePosition().convertTo(PIXELS);
        float keyX = keyPosition.getX();
        float keyY = keyPosition.getY();
        key = new Key(keyX, keyY, TILE_SIZE,TILE_SIZE,10,9,TILE_SCREEN_SIZE, TILE_SCREEN_SIZE, game);
        // After loading the tiles,
        // get the array of tiles from our tile generator: levels.getTileset()
        // and then get the texture region where our key is at
        keyRegion = levels.getTileset()[TileType.KEY.getId()];

        collectibles = new Array<>();
        //System.out.println(Arrays.deepToString(levels.getTileEnumOnMap()));
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
        //Gdx.app.log("Size" ,  horizontalTilesCount + "x" + verticalTilesCount);

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

        generateCollectibles(emptyTiles, Collectibles.Type.HEART, 3, 16, 11, 11, 2.5f);
        generateCollectibles(emptyTiles, Collectibles.Type.PRETZEL, 3, 32,28, 27,72/28f);
        generateCollectibles(emptyTiles, Collectibles.Type.GESUNDHEITSKARTE, 1, 32,27,18,72/28f);

        generateCollectibles(emptyTiles, Collectibles.Type.COIN, 5, 16,11, 11,2.5f);

        generateCollectibles(emptyTiles, Collectibles.Type.STAMINA, 1, 32,16,22, 2.5f);
    }

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
            System.out.println("Portal Position: " + x + ", " + y);
        }
    }


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

    public Position getSectionIndex(Position position, int collectiblesToGenerate) {
        int mapWidth = horizontalTilesCount;
        int mapHeight = verticalTilesCount;
        int sectionSideCount = ((collectiblesToGenerate + 5) / 2); // floor(((x+3)/2)^2)^{2} > x
        int sectionWidth = mapWidth / sectionSideCount;
        int sectionHeight = mapHeight / sectionSideCount;
        return new Position((float) (position.getTileX() / sectionWidth), (float) (position.getTileY() / sectionHeight), TILES);
    }

    public void createInstructionPanel(){
        //Drawable background = new TextureRegionDrawable(new TextureRegion(new Texture("backgrounds/introduction.png")));
        NinePatchDrawable backgroundDrawable = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/introduction.png"),
                86, 86, 98, 98);
        Panel instructionPanel = new Panel(stage1, backgroundDrawable, game, 0.8f, 0.8f);
        instructionPanel.init();

        String levelName = levels.getProperties("levelName"); // test

        instructionPanel.addLabel((levelName.isEmpty()) ? "introduction" : levelName, game.getSkin(), "fraktur", 1, 40);
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

    private String getInstructionsText1() {
        String s = levels.getProperties("instructionsText1");
        return s.replace("\\n", "\n");
    }

    private String getLabelForInstructionsText2(){
        String s = levels.getProperties("instructionsText2");
        return s.replace("\\n", "\n");
    }


    public void createIntroPanel(){
        NinePatchDrawable backgroundDrawable = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/introduction.png"),
                86, 86, 98, 98);
        Panel introPanel = new Panel(stage1, backgroundDrawable, game, 0.8f, 0.8f);
        introPanel.init();

        String levelName = levels.getProperties("levelName");
        introPanel.addLabel((levelName.isEmpty()) ? "Game Instructions" : levelName, game.getSkin(), "fraktur", 1, 40);

        introPanel.addLabel("Move using W, A, S, D keys.", game.getSkin(), "black", 1f, 20);
        introPanel.addLabel("Collect keys to unlock exits.", game.getSkin(), "black", 1f, 20);
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

    public void createPausePanel() {
        //Drawable background = new TextureRegionDrawable(new TextureRegion(new Texture("backgrounds/pause.png")));
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

    public void createOptionPanel() {
        //Drawable background = new TextureRegionDrawable(new TextureRegion(new Texture("backgrounds/introduction.png")));
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

    public void createVictoryPanel() {
        //Drawable background = new TextureRegionDrawable(new TextureRegion(new Texture("backgrounds/victory.png")));
        NinePatchDrawable background = getNinePatchDrawableFromPath(Gdx.files.internal("backgrounds/victory.png"),
                50, 50, 50, 50);
        Panel victoryPanel = new Panel(stage1, background, game, 0.8f, 0.6f);
        victoryPanel.init();
        isPaused = true;

        victoryPanel.addLabel("Victory!", game.getSkin(), "fraktur", 1, 80);

        String grade = calculateScore();
        String scoreText = "Score: " + grade + " (" + player.getCoins() + "/" + totalCoins + ")";
        if (!isTutorial) victoryPanel.addLabel(scoreText, game.getSkin(), "black", 1f, 40);

        System.out.println("Game Level: " + game.getGameLevel());
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
     * Creates a single-pixel Drawable object with a solid color.
     *
     * @param color the color of the drawable pixel
     * @return a Drawable object filled with the specified color
     */
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
            if (currentTutorialStage == TutorialStage.ZOOM) currentTutorialStage = TutorialStage.ESC_PAUSE;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) { // "-" key
            targetZoom += 0.02f;
            if (currentTutorialStage == TutorialStage.ZOOM) currentTutorialStage = TutorialStage.ESC_PAUSE;
        }

        clampZoomLevel(); // Clamp to avoid extreme zoom level

        // Handle Mute
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { // Press 'M' to mute/unmute
            //game.setMuted(!game.isMuted());
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
        // renderText((float) (0 + Math.sin(sinusInput) * 100), (float) (750 + Math.cos(sinusInput) * 100), "Press ESC to go to menu");
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



    private void update(float delta) {
        if (!isPaused) {
            // Update characters
            player.update(delta);

            // Update enemies
            for (ChasingEnemy enemy : iterate(levels.chasingEnemies)) {
                enemy.update(delta);
            }

            // Update timer
           // gameTimer += delta;
        }
    }

    /**
     * Draws a thick border around the tiled map using shapeRenderer
     */
    public void drawMapBorder() {
        // if (mapTiles.isEmpty()) return;

        // Set up ShapeRenderer to match game world projection
        //shapeRenderer.setProjectionMatrix(game.getSpriteBatch().getProjectionMatrix());

        // Begin drawing with filled shapes for the border
        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY); // Set border color

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
        //game.getSpriteBatch().draw(game.getBackgroundTexture(), 0, 0);
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
        //System.out.println("collectable rendered ");

    }
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
            //return;
        // else the key is not collected, render the key:

        /* uncomment this when the key's position should be regularly updated. i.e., the key is dynamic
        //get our key position and render the key there
        Position keyPosition = levels.getKeyTilePosition();
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

    /**
     * Renders all traps on the map.
     */
    private void renderTrap(){
        for (Trap trap : iterate(levels.traps)){ // for (trap : levels.traps){
            trap.draw(game.getSpriteBatch());
        }
    }

    private void spawnPortal() {
        System.out.println("Spawning portal...");
        Array<Position> emptyTiles = getEmptyTiles(levels);
        generatePortals(emptyTiles, 1, 64, 64, 48, 48, 96);
        System.out.println("Portals generated: " + portals.size);
    }

    /**
     * Renders chasing enemies with appropriate animations based on their movement direction.
     */
    private void renderChasingEnemy(){
        //chasingEnemy.draw(game.getSpriteBatch());
        for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){ // for (ChasingEnemy enemy : levels.chasingEnemies)

            /*if (abs(enemy.getVelX()) > abs(enemy.getVelY())){ // x velocity > y velocity -> either left or right
                if (enemy.getVelX() < 0) enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("left");
                else enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("right");
            }
            else { // v_y > v_x
                if (enemy.getVelY() < 0) enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("down");
                else enemyAnimation = levels.getEnemyAnimations(enemy.getEnemyIndex()).get("up");
            }*/
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
        //if (currentStamina >= Player.maxStamina) return;

        Gdx.gl.glLineWidth((int)(5f / camera.zoom)); // Set line width for better visibility
        int staminaRadius = 12;
        float offsetX = -player.getHitboxWidthOnScreen() / 2 - 5;
        float offsetY = player.getHitboxHeightOnScreen() / 2 + 5;
        float staminaX = player.getX() + offsetX;// + staminaRadius / 2;
        float staminaY = player.getY() + offsetY;// - staminaRadius;
        //shapeRenderer.setProjectionMatrix(camera.combined);
        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


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

        //shapeRenderer.end();
    }

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
        variablesToShow.put("player.stamina", player.getStamina());
        //drawVariables(variablesToShow);

        game.getSpriteBatch().end();

        // hudObjectRenderer use another rendering batch, so we have to end the batch first, and start it again
        game.getSpriteBatch().begin();
        hudObjectRenderer.drawHearts(game.getSpriteBatch(), player.getLives(), 20, Gdx.graphics.getHeight() - 26f - 20, 32, 2);

        String coinText = "Coins: " + player.getCoins() + "/" + totalCoins;
        font.draw(game.getSpriteBatch(), coinText, 20, Gdx.graphics.getHeight() - 50);

        String keyStatus = key.isCollected() ? "Key Collected!" : "Find The Key!";
        font.draw(game.getSpriteBatch(), keyStatus, 20, Gdx.graphics.getHeight() - 80);


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

    public void resizePanels(){
        for (var actor : iterate(stage1.getActors())){
            // what is stored in the stage is actually the panels' tables

            /*if (actor instanceof Panel panel){
                Gdx.app.log("Resize", "actor is a panel");
                float widthRatio = (panel).getWidthRatio();
                float heightRatio = (panel).getHeightRatio();
                panel.init(Gdx.graphics.getWidth() * widthRatio,Gdx.graphics.getHeight() * heightRatio);
                panel.setPosition(Gdx.graphics.getWidth() * (1-widthRatio)/2, Gdx.graphics.getHeight() * (1-heightRatio)/2);
            }
            else */
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
        //targetZoom = MathUtils.clamp(targetZoom, 0.8f, 1.3f);
    }

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

    //private String currentTooltipMessage;
    //private float tooltipTimer = 0f;
    //private static final float TOOLTIP_MAX_DURATION = 3f; // Maximum duration in seconds

    private void showTooltip(String message) {
        // Remove any existing tooltip
        Gdx.app.log("Tooltip", "text changed to: " + message);
        tooltipManager.message = message;
    }

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

    private void triggerSpotlight(float x, float y, float radius, String message) {
        //setPaused(true); // Pause the game
        if (tooltipManager.timer >= TooltipManager.TOOLTIP_DURATION) return;
        renderSpotlightEffect(x, y, radius, 0.8f, 1);
        tooltipManager.show(message); // Display a message
    }

    private void updateTutorial(float delta){

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
                    //showTooltip("Use WASD or arrow keys to move.");
                }
            }
            case ZOOM -> {
                renderSpotlightEffect(0,0,0, 0.8f, 0.5f);
                tooltipManager.show("Use the scroll wheel or '+'/'-' keys to zoom in and out.");
                for (ChasingEnemy enemy : iterate(levels.chasingEnemies)){
                    enemy.pause();
                }
                //this.pause(false);

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

        TutorialStage(int stageOrder){
            this.stageOrder = stageOrder;
        }

        public int getStageOrder() {
            return stageOrder;
        }
    }

    private TutorialStage currentTutorialStage = TutorialStage.INTRO;

    private class TooltipManager {
        private String message;
        private float timer;
        private static final float TOOLTIP_DURATION = 3f;

        private final SpriteBatch batch = new SpriteBatch();

        public void show(String message) {
            this.message = message;
            if (!message.contains("enemy")) this.timer = 0f;
        }

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

        //inputMultiplexer.addProcessor(stage1);
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
        //inputMultiplexer.removeProcessor(stage1);
    }

    @Override
    public void show() {
      //  createPausePanel();
       // Gdx.input.setInputProcessor(stage1);
    }

    @Override
    public void hide() {
    }

    // TODO: Remember to dispose of any textures you create when you're done with them to prevent memory leaks.
    @Override
    public void dispose() {
        font.dispose();
        //shapeRenderer.dispose();
        // i think we shouldn't even dispose the shapeRenderer, right? (else the program will exit unexpectedly)
        mapRenderer.dispose();
        hudObjectRenderer.dispose();
        // disposing all disposables (such as Stage, Skin, Texture ... etc)
        stage1.dispose();
        shader.dispose();
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
}
