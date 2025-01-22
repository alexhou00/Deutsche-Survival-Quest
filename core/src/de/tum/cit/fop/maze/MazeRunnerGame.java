package de.tum.cit.fop.maze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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

    public void setGameLevel(int gameLevel) {
        this.gameLevel = gameLevel;
    }

    private int gameLevel = 1;
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

    private Animation<TextureRegion> heartAnimation;
    private Animation<TextureRegion> coinAnimation;
    private Animation<TextureRegion> staminaPotionAnimation;
    private Animation<TextureRegion> pretzelAnimation;
    private TextureRegion gesundheitskarteRegion;
    private Animation<TextureRegion> portalAnimation;

    Texture backgroundTexture;

    Music backgroundMusic, menuMusic, pauseMusic,  gameOverMusic, victorySoundEffect, victoryMusic;
    Sound soundEffectKey, soundEffectHurt, soundEffectRunning;
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
        //Gdx.app.setLogLevel(Application.LOG_ERROR);

        spriteBatch = new SpriteBatch(); // Create SpriteBatch
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        this.loadAnimation(); // Load character animation

        backgroundTexture = new Texture("backgrounds/background.png");

        // Play some background music
        // Background sound
        //CHANGE BACKGROUND MUSIC
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Bruno_Belotti_-_Nel_giardino_dello_Zar__Polka_Loop.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/010614songidea(copycat).mp3"));
        menuMusic.setLooping(true);

        pauseMusic = Gdx.audio.newMusic(Gdx.files.internal("music/A cup of tea.mp3"));
        pauseMusic.setLooping(true);

        gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("music/No Hope.wav"));
        gameOverMusic.setLooping(true);

        //victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("A cup of tea.mp3"));
        //victoryMusic.setLooping(true);

        victorySoundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/Lively Meadow Victory Fanfare.mp3"));
        soundEffectKey = Gdx.audio.newSound(Gdx.files.internal("sounds/Accept.mp3"));
        soundEffectHurt = Gdx.audio.newSound(Gdx.files.internal("sounds/01._damage_grunt_male.wav"));
        soundEffectRunning = Gdx.audio.newSound(Gdx.files.internal("sounds/running.mp3"));


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
        gameOverMusic.pause();
        pauseMusic.pause();
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
            gameLevel = getGameLevel();
            Gdx.app.log("MazeRunnerGame", "Go to Game, LEVEL: " + gameLevel);
            gameScreen = new GameScreen(this);
            gameOverMusic.pause();
            pauseMusic.play();
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
            backgroundMusic.pause();
            pauseMusic.pause();
            gameOverMusic.play();

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
    private void loadAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("characters/character.png")); // TODO: Redesign our character
        Texture mobGuySheet = new Texture(Gdx.files.internal("characters/ticket_guy.png"));
        Texture objectSheet = new Texture(Gdx.files.internal("original/objects.png"));
        Texture portalSheet = new Texture(Gdx.files.internal("portals/portalRings2.png"));


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

        Array<TextureRegion> heartFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> coinFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> staminaPotionFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> pretzelFrames = new Array<>(TextureRegion.class);

        Array<TextureRegion> portalFrames = new Array<>(TextureRegion.class);


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

        heartFrames.add(new TextureRegion(objectSheet, 2, 51, 11, 11));
        heartFrames.add(new TextureRegion(objectSheet, 2+16, 51, 11, 11));
        heartFrames.add(new TextureRegion(objectSheet, 2+16*2, 51, 11, 11));
        heartFrames.add(new TextureRegion(objectSheet, 2+16*3, 51, 11, 11));

        for (int i=0;i<4;i++)
            coinFrames.add(new TextureRegion(objectSheet, 2+16*i, 66, 11, 11));

        for (int i=0;i<3;i++)
            staminaPotionFrames.add(new TextureRegion(objectSheet, 288+32*i, 64, 32, 32));

        for (int i=0;i<6;i++)
            pretzelFrames.add(new TextureRegion(objectSheet, 128+32*i, 128, 32, 32));

        for (int i=0;i<5;i++)
            portalFrames.add(new TextureRegion(portalSheet, 32 * i, 0, 32, 32));

        gesundheitskarteRegion = new TextureRegion(objectSheet, 224, 96, 32, 32);


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

        heartAnimation = new Animation<>(0.1f, heartFrames);
        coinAnimation = new Animation<>(0.1f, coinFrames);
        staminaPotionAnimation = new Animation<>(0.1f, staminaPotionFrames);
        pretzelAnimation = new Animation<>(0.1f, pretzelFrames);
        portalAnimation = new Animation<>(0.1f, portalFrames);

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

    public Animation<TextureRegion> getHeartAnimation() {
        return heartAnimation;
    }

    public Animation<TextureRegion> getCoinAnimation() {
        return coinAnimation;
    }

    public Animation<TextureRegion>  getStaminaPotionAnimation() {
        return staminaPotionAnimation;
    }

    public Animation<TextureRegion> getPretzelAnimation() {
        return pretzelAnimation;
    }

    public Animation<TextureRegion> getPortalAnimation() { return portalAnimation; }

    public Texture getBackgroundTexture() {
        return backgroundTexture;
    }

    public TextureRegion getGesundheitskarteRegion() {
        return gesundheitskarteRegion;
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

    public Sound getSoundEffectKey() {
        return soundEffectKey;
    }

    public Sound getSoundEffectHurt() {
        return soundEffectHurt;
    }

    public Music getPauseMusic() {
        return pauseMusic;
    }

    public Music getMenuMusic() {
        return menuMusic;
    }

    public Music getVictorySoundEffect() {
        return victorySoundEffect;
    }

    public Sound getSoundEffectRunning(){
        return soundEffectRunning;
    }

    public void checkExitToNextLevel(Player player) {
        if (player.isCenterTouchingTile(Exit.class) && gameScreen.getKey().isCollected()){
            Gdx.app.log("MazeRunnerGame", "Player is at the exit and has the key.");

            if (!gameScreen.isPaused()) {
                gameScreen.setPaused(true);
                gameScreen.createVictoryPanel();
                //this.pause();
                this.getBackgroundMusic().pause();
                this.getPauseMusic().pause();
                this.getVictorySoundEffect().play();
            }

            //gameScreen.isPaused();
            //pauseMusic.pause();
            //victorySoundEffect.play();

            /*gameLevel += 1;
            gameScreen.dispose();
            gameScreen = new GameScreen(this);
            gameScreen.getKey().setCollected(false);
            this.setScreen(gameScreen);

            Gdx.app.log("MazeRunnerGame", "Set Screen to Game Screen");*/
        }
    }

    public void startNextLevel() {
        Gdx.app.log("MazeRunnerGame", "Starting next level: " + (gameLevel));

        // Dispose of the current screen
        if (gameScreen != null) {
            gameScreen.dispose();
        }

        // Create and set the new game screen
        gameScreen = new GameScreen(this);
        setScreen(gameScreen);

        // Reset any necessary states in the new screen
        gameScreen.getKey().setCollected(false);

        // Ensure the game is not paused for the new level
        gameScreen.setPaused(false);
    }
}
