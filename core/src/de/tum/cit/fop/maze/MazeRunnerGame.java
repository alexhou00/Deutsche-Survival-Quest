package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.game_objects.Player;
import de.tum.cit.fop.maze.screens.PauseScreen;
import de.tum.cit.fop.maze.tiles.Exit;
import de.tum.cit.fop.maze.screens.GameOverScreen;
import de.tum.cit.fop.maze.screens.GameScreen;
import de.tum.cit.fop.maze.screens.MenuScreen;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

import java.util.HashMap;
import java.util.Map;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private GameOverScreen gameOverScreen;

    public int getGameLevel() {
        return gameLevel;
    }

    private int gameLevel;
    // Sprite Batch for rendering
    private SpriteBatch spriteBatch;

    // UI Skin
    private Skin skin;

    // Character animation downwards
    private Animation<TextureRegion> characterDownAnimation;
    private Animation<TextureRegion> characterUpAnimation;
    private Animation<TextureRegion> characterLeftAnimation;
    private Animation<TextureRegion> characterRightAnimation;
    private Animation<TextureRegion> characterIdleAnimation;

    private Map<String, Animation<TextureRegion>> mobGuyAnimations;

    Texture backgroundTexture;

    Music backgroundMusic, menuMusic, pauseMusic,  gameOverMusic, victoryMusic, soundEffectKey, soundEffectHurt;
    private boolean isMuted;


    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
    }


    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        this.loadCharacterAnimation(); // Load character animation

        backgroundTexture = new Texture("background.png");

        // Play some background music
        // Background sound
        //CHANGE BACKGROUND MUSIC
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Bruno_Belotti_-_Nel_giardino_dello_Zar__Polka_Loop.mp3")); // TODO: Change this bg music first
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("010614songidea(copycat).mp3"));
        menuMusic.setLooping(true);

        pauseMusic = Gdx.audio.newMusic(Gdx.files.internal("A cup of tea.mp3"));
        pauseMusic.setLooping(true);

        /*gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("A cup of tea.mp3"));
        gameOverMusic.setLooping(true);

        victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("A cup of tea.mp3"));
        victoryMusic.setLooping(true);*/

        soundEffectKey = Gdx.audio.newMusic(Gdx.files.internal("Accept.mp3"));
        soundEffectHurt = Gdx.audio.newMusic(Gdx.files.internal("01._damage_grunt_male.wav"));


        goToMenu(); // Navigate to the menu screen
    }


    public void selectLevel() {

    }

    public void exitGame(){
        Gdx.app.exit();
        System.exit(-1);
    }


    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        if (menuScreen == null) {
            menuScreen = new MenuScreen(this);
        }
        this.setScreen(menuScreen); // Set the current screen to MenuScreen
        backgroundMusic.pause();
        menuMusic.play();

        if (gameScreen != null) {
            gameScreen.dispose(); // Dispose the game screen if it exists
            gameScreen = null;
        }
        if (gameOverScreen != null) {
            gameOverScreen.dispose(); // Dispose the menu screen if it exists
            gameOverScreen = null;
        }
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        // this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen
        if (gameScreen == null) {
            gameLevel = 1; // TODO: this will be changed in the future once we can select our own levels
            gameScreen = new GameScreen(this);
            menuMusic.pause();
            backgroundMusic.play();
        }
        this.setScreen(gameScreen); // Set the current screen to MenuScreen

        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }

        if (gameOverScreen != null) {
            gameOverScreen.dispose(); // Dispose the menu screen if it exists
            gameOverScreen = null;
        }
    }

    public void goToGameOverScreen() {
        Gdx.app.log("MazeRunner", "Navigating to Game Over Screen...");

        try {
            // Check if gameOverScreen is already created
            if (gameOverScreen == null) {
                gameOverScreen = new GameOverScreen(this);
                Gdx.app.log("MazeRunner", "GameOverScreen created.");
            }

            // Set the screen to GameOverScreen
            this.setScreen(gameOverScreen);

            // Dispose of other screens if necessary
            if (gameScreen != null) {
                gameScreen.dispose();
                gameScreen = null;
            }

            if (menuScreen != null) {
                menuScreen.dispose();
                menuScreen = null;
            }

        } catch (Exception e) {
            Gdx.app.log("MazeRunner", "Error while switching to GameOverScreen: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Loads the character animation from the character.png file.
     */
    private void loadCharacterAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png")); // TODO: Redesign our character
        Texture mobGuySheet = new Texture(Gdx.files.internal("mob_guy.png"));


        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4; // for player

        // libGDX internal Array instead of ArrayList because of performance
        Array<TextureRegion> downFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> upFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> leftFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> rightFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> idleFrame = new Array<>(TextureRegion.class);

        Map<String, Array<TextureRegion>> mobGuyFrames = new HashMap<>();
        mobGuyFrames.put("down", new Array<>(TextureRegion.class));
        mobGuyFrames.put("up", new Array<>(TextureRegion.class));
        mobGuyFrames.put("left", new Array<>(TextureRegion.class));
        mobGuyFrames.put("right", new Array<>(TextureRegion.class));

                // Add all frames to the animation
        int framesXOffset = 0; // define how many frames of X to shift to start extracting our character on "character.png"
        for (int col = 0; col < animationFrames; col++) {
            downFrames.add(new TextureRegion(walkSheet, (col + framesXOffset) * frameWidth, 0, frameWidth, frameHeight));
            rightFrames.add(new TextureRegion(walkSheet, (col + framesXOffset) * frameWidth, frameHeight, frameWidth, frameHeight));
            upFrames.add(new TextureRegion(walkSheet, (col + framesXOffset) * frameWidth, frameHeight * 2, frameWidth, frameHeight));
            leftFrames.add(new TextureRegion(walkSheet, (col + framesXOffset) * frameWidth, frameHeight * 3, frameWidth, frameHeight));
        }
        idleFrame.add(new TextureRegion(walkSheet, 0, 0, frameWidth, frameHeight));

        animationFrames = 3;
        int mobFrameSize = 16;
        for (int col = 0; col < animationFrames; col++){
            mobGuyFrames.get("down").add(new TextureRegion(mobGuySheet, (col) * mobFrameSize, 0, mobFrameSize, mobFrameSize));
            mobGuyFrames.get("left").add(new TextureRegion(mobGuySheet, mobFrameSize * animationFrames + (col) * mobFrameSize, 0, mobFrameSize, mobFrameSize));
            mobGuyFrames.get("right").add(new TextureRegion(mobGuySheet, mobFrameSize * animationFrames * 2 + (col) * mobFrameSize, 0, mobFrameSize, mobFrameSize));
            mobGuyFrames.get("up").add(new TextureRegion(mobGuySheet, mobFrameSize * animationFrames * 3 + (col) * mobFrameSize, 0, mobFrameSize, mobFrameSize));
        }



        characterDownAnimation = new Animation<>(0.1f, downFrames);
        characterUpAnimation = new Animation<>(0.1f, upFrames);
        characterLeftAnimation = new Animation<>(0.1f, leftFrames);
        characterRightAnimation = new Animation<>(0.1f, rightFrames);
        characterIdleAnimation = new Animation<>(0.1f, idleFrame);

        mobGuyAnimations = new HashMap<>();
        mobGuyAnimations.put("down", new Animation<>(0.1f, mobGuyFrames.get("down")));
        mobGuyAnimations.put("left", new Animation<>(0.1f, mobGuyFrames.get("left")));
        mobGuyAnimations.put("right", new Animation<>(0.1f, mobGuyFrames.get("right")));
        mobGuyAnimations.put("up", new Animation<>(0.1f, mobGuyFrames.get("up")));

    }

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public void setBackgroundMusic(Music backgroundMusic) {
        this.backgroundMusic = backgroundMusic;
    }

    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }

    public void muteBGM(){
        backgroundMusic.setVolume(0);
    }
    public void normalizeBGM(){
        backgroundMusic.setVolume(1f);
    }

    // Getter methods
    public Skin getSkin() {
        return skin;
    }

    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterDownAnimation;
    }

    public Animation<TextureRegion> getCharacterUpAnimation() {
        return characterUpAnimation;
    }

    public Animation<TextureRegion> getCharacterLeftAnimation() {
        return characterLeftAnimation;
    }

    public Animation<TextureRegion> getCharacterRightAnimation() {
        return characterRightAnimation;
    }

    public Animation<TextureRegion> getCharacterIdleAnimation() {
        return characterIdleAnimation;
    }

    public Map<String, Animation<TextureRegion>> getMobGuyAnimations() {
        return mobGuyAnimations;
    }

    public Texture getBackgroundTexture() {
        return backgroundTexture;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }


    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public MenuScreen getMenuScreen() {
        return menuScreen;
    }

    public GameOverScreen getGameOverScreen() {
        return gameOverScreen;
    }

    public Music getGameOverMusic() {
        return gameOverMusic;
    }

    public Music getVictoryMusic() {
        return victoryMusic;
    }

    public Music getSoundEffectKey() {
        return soundEffectKey;
    }

    public Music getSoundEffectHurt() {
        return soundEffectHurt;
    }

    public Music getPauseMusic() {
        return pauseMusic;
    }

    public Music getMenuMusic() {
        return menuMusic;
    }

    public void checkExitToNextLevel(Player player) {
        if (player.isCenterTouchingTile(Exit.class) && gameScreen.getKey().isCollected()){
            gameLevel += 1;
            gameScreen.dispose();
            gameScreen = new GameScreen(this);
            gameScreen.getKey().setCollected(false);
            this.setScreen(gameScreen);
            Gdx.app.log("MazeRunnerGame", "Set Screen to Game Screen");
        }
    }

}
