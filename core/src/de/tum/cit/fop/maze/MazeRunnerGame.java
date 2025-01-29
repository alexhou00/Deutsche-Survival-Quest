package de.tum.cit.fop.maze;

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
import de.tum.cit.fop.maze.screens.*;
import de.tum.cit.fop.maze.tiles.Exit;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

import java.util.HashMap;
import java.util.Map;

import static de.tum.cit.fop.maze.util.Constants.TOTAL_LEVELS;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class MazeRunnerGame extends Game {
    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private GameOverScreen gameOverScreen;
    private VictoryScreen victoryScreen;
    private Array<Music> musicList;
    private Array<Sound> soundList;
    private final Array<Long> playingSoundIds = new Array<>();  // Track sound instances by their IDs

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
    private Skin skinCraft;
    private Skin skinPlain;

    // Character animation downwards
    public Map<String, Animation<TextureRegion>> characterAnimations;
    private TextureRegion characterIdleRegion;

    private Animation<TextureRegion> heartAnimation;
    private Animation<TextureRegion> coinAnimation;
    private Animation<TextureRegion> staminaPotionAnimation;
    private Animation<TextureRegion> pretzelAnimation;
    private TextureRegion gesundheitskarteRegion;
    private Animation<TextureRegion> portalAnimation;

    Texture backgroundTexture;

    Music backgroundMusic, menuMusic, pauseMusic,  gameOverMusic, victorySoundEffect, victoryMusic, warningMusic;
    Sound soundEffectKey, soundEffectHurt, soundEffectTeleport;
    Music soundEffectRunning, soundEffectPanting;
    private boolean isMuted;

    private float volume = 1.0f; // Default volume
    private boolean muted = false;

    private final Map<String, Sound> sounds = new HashMap<>();
    private final Map<String, Long> soundIds = new HashMap<>();

    private SoundManager soundManager;



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
        skinCraft = new Skin(Gdx.files.internal("new-skin/craft-f-ui.json")); //new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        skinPlain = new Skin(Gdx.files.internal("plain-james/skin/plain-james-ui.json"));
        this.loadAnimation(); // Load character animation

        backgroundTexture = new Texture("backgrounds/background.png");

        musicList = new Array<>();
        soundList = new Array<>();

        soundManager = new SoundManager();

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Bruno_Belotti_-_Nel_giardino_dello_Zar__Polka_Loop.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();
        musicList.add(backgroundMusic);

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/010614songidea(copycat).mp3"));
        menuMusic.setLooping(true);
        musicList.add(menuMusic);

        pauseMusic = Gdx.audio.newMusic(Gdx.files.internal("music/A cup of tea.mp3"));
        pauseMusic.setLooping(true);
        musicList.add(pauseMusic);

        gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("music/No Hope.wav"));
        gameOverMusic.setLooping(true);
        musicList.add(gameOverMusic);

        //victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("A cup of tea.mp3"));
        //victoryMusic.setLooping(true);

        victorySoundEffect = Gdx.audio.newMusic(Gdx.files.internal("sounds/Lively Meadow Victory Fanfare.mp3"));
        musicList.add(victorySoundEffect);

        soundEffectKey = Gdx.audio.newSound(Gdx.files.internal("sounds/Accept.mp3"));
        soundList.add(soundEffectKey);
        soundEffectHurt = Gdx.audio.newSound(Gdx.files.internal("sounds/01._damage_grunt_male.wav"));
        soundList.add(soundEffectHurt);
        soundEffectTeleport = Gdx.audio.newSound(Gdx.files.internal("sounds/teleport.wav"));
        soundList.add(soundEffectTeleport);

        soundEffectRunning = Gdx.audio.newMusic(Gdx.files.internal("sounds/running-14658.mp3"));
        musicList.add(soundEffectRunning);
        warningMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/warning.wav"));
        musicList.add(warningMusic);

        soundEffectPanting = Gdx.audio.newMusic(Gdx.files.internal("sounds/breathing-fast-247451.mp3"));
        musicList.add(soundEffectPanting);

        soundManager.addSoundEffect("key", Gdx.audio.newSound(Gdx.files.internal("sounds/Accept.mp3")));
        soundManager.addSoundEffect("hurt", Gdx.audio.newSound(Gdx.files.internal("sounds/01._damage_grunt_male.wav")));
        soundManager.addSoundEffect("teleport", Gdx.audio.newSound(Gdx.files.internal("sounds/teleport.wav")));

        // Set the initial volume (e.g., 50%)
        float initialVolume = 0.5f;
        for (Map.Entry<String, Long> entry : soundIds.entrySet()) {
            sounds.get(entry.getKey()).setVolume(entry.getValue(), initialVolume); // Set volume for each sound
        }

        goToMenu(); // Navigate to the menu screen
        setVolume(volume);
    }

    public SoundManager getSoundManager(){
        return soundManager;
    }

    /**
     * Sets the volume for all music tracks in the game.
     * This method updates the volume for each music track in the music list
     * by iterating through all music items and applying the given volume value.
     *
     * @param volume The desired volume level to set for all music tracks.
     *               Should be a value between 0.0 (muted) and 1.0 (full volume).
     */
    public void setVolume(float volume) {
        this.volume = volume;

        // Set volume for all music
        for (Music music : musicList) {
            music.setVolume(volume);
        }
    }

    /**
     * Exits the game and terminates the application.
     * This method first calls the LibGDX method to close the application,
     * and then forces a termination of the Java process with {@link System#exit(int)}.
     * The exit code -1 is used to indicate an abnormal termination.
     *
     * @see System#exit(int)
     */
    public void exitGame(){
        Gdx.app.exit();
        System.exit(-1);
    }

    /**
     * Mutes or unmutes all game music.
     *
     * This method sets the volume of all music tracks to 0 (mute) or restores the original volume
     * based on the provided {@code mute} parameter. The volume is adjusted for each music track in
     * {@link #musicList} accordingly.
     *
     * @param mute A boolean indicating whether to mute or unmute the music:
     *             - {@code true} will mute the music (set volume to 0.0f).
     *             - {@code false} will restore the music to the original volume.
     *
     * @see Music#setVolume(float)
     */
    public void muteAll(boolean mute) {
        this.muted = mute;

        Gdx.app.log("Mute", "Muted: " + muted);

        float targetVolume = mute ? 0.0f : volume;
        for (Music music : musicList) {
            music.setVolume(targetVolume);
        }
    }

    /**
     * Retrieves the current volume level.
     *
     * This method returns the volume level of the game music. If the music is muted, it returns 0.
     * Otherwise, it returns the original volume level.
     *
     * @return The current volume level:
     *         - 0 if the music is muted.
     *         - The {@link #volume} value if the music is not muted.
     */
    public float getVolume() {
        return muted ? 0 : volume;
    }

    /**
     * Checks whether the music is muted.
     *
     * This method returns the mute status of the game music. It returns `true` if the music is muted,
     * and `false` if it is not muted.
     *
     * @return {@code true} if the music is muted, {@code false} if the music is not muted.
     */
    public boolean isMuted() {
        return muted;
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
        victorySoundEffect.pause();
        gameOverMusic.pause();

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
    public void goToGame(boolean tutorial) {
        if (!tutorial)
            gameLevel = (getGameLevel() == 0) ? 1 : getGameLevel();
        else
            gameLevel = 0;

        Gdx.app.log("MazeRunnerGame", "Go to Game, LEVEL: " + gameLevel);
        gameScreen = new GameScreen(this);
        gameOverMusic.pause();
        pauseMusic.play();
        menuMusic.pause();
        backgroundMusic.play();

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

    /**
     * Navigates to the main game screen.
     * <p>
     * This method is called when the player selects to start or resume the main game. It calls {@link #goToGame(boolean)}
     * with the argument set to false, which indicates that the player is not entering the tutorial mode but the actual game.
     * </p>
     */
    public void goToGame(){
        goToGame(false);
    }

    /**
     * Navigates to the Game Over Screen and disposes of the current game and menu screens.
     * <p>
     * This method transitions to the {@link GameOverScreen} and ensures that the necessary resources are properly disposed
     * of. It pauses the background music and pause music, then plays the game-over music. If any screens like the
     * {@link GameScreen} or {@link MenuScreen} exist, they are disposed of to free up memory.
     * </p>
     * <p>
     * Any exceptions encountered during the screen transition are caught and logged.
     * </p>
     */
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
     * Navigates to the Victory Screen and disposes of the current game and menu screens.
     * <p>
     * This method handles the transition to the victory screen by setting the current screen to the {@link VictoryScreen}.
     * It also disposes of the current {@link GameScreen}, {@link MenuScreen}, and {@link GameOverScreen}, if they exist,
     * to free up resources and prevent memory leaks. Additionally, it pauses all music tracks.
     * </p>
     */
    public void goToVictoryScreen() {
        Gdx.app.log("MazeRunner", "Navigating to Victory Screen...");

        if (victoryScreen == null) {
            victoryScreen = new VictoryScreen(this);
        }
        this.setScreen(victoryScreen);
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }

        if (gameOverScreen != null) {
            gameOverScreen.dispose(); // Dispose the menu screen if it exists
            gameOverScreen = null;
        }

        menuMusic.pause();
        gameOverMusic.pause();
        backgroundMusic.pause();
    }


    /**
     * Loads the character animation from the character.png file.
     */
    private void loadAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("characters/character.png")); // TODO: Redesign our character
        Texture objectSheet = new Texture(Gdx.files.internal("original/objects.png"));
        Texture portalSheet = new Texture(Gdx.files.internal("portals/portalRings2.png"));

        int playerFrameWidth = 16;
        int playerFrameHeight = 32;

        characterIdleRegion = new TextureRegion(walkSheet, 0, 0, playerFrameWidth, playerFrameHeight);

        characterAnimations = createDirectionalAnimations(walkSheet, false, 0.1f, 0, playerFrameWidth, playerFrameHeight, 4);

        heartAnimation = createAnimation(objectSheet, 0.1f, 2, 51, 11, 11, 4, 16);
        coinAnimation = createAnimation(objectSheet, 0.1f, 2, 66, 11, 11, 4, 16);
        staminaPotionAnimation = createAnimation(objectSheet, 0.1f, 288, 64, 32, 32, 3, 32);
        pretzelAnimation = createAnimation(objectSheet, 0.1f, 128, 128, 32, 32, 6, 32);
        portalAnimation = createAnimation(portalSheet, 0.1f, 0, 0, 32, 32, 5, 32);
        gesundheitskarteRegion = new TextureRegion(objectSheet, 224, 96, 32, 32);
    }

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skinCraft.dispose(); // Dispose the skin
        skinPlain.dispose();

        for (Music music : musicList) {
            music.dispose();
        }

        // Dispose of all sound effects
        for (Sound sound : soundList) {
            sound.dispose();
        }
    }

    /**
     * Mutes the background music by setting its volume to 0.
     * This method is typically used to silence the background music in the game.
     */
    public void muteBGM(){
        backgroundMusic.setVolume(0);
    }

    /**
     * Mutes the background music by setting its volume to 0.
     * This method is typically used to silence the background music in the game.
     */
    public void normalizeBGM(){
        backgroundMusic.setVolume(1f);
    }

    // Getter methods
    public Skin getSkin() {
        return skinCraft; // default skin
    }

    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterAnimations.get("down");
    }

    public Animation<TextureRegion> getCharacterUpAnimation() {
        return characterAnimations.get("up");
    }

    public Animation<TextureRegion> getCharacterLeftAnimation() {
        return characterAnimations.get("left");
    }

    public Animation<TextureRegion> getCharacterRightAnimation() {
        return characterAnimations.get("right");
    }

    public TextureRegion getCharacterIdleRegion() {
        return characterIdleRegion;
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

    public TextureRegion getGesundheitskarteRegion() {
        return gesundheitskarteRegion;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
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


    public Music getVictorySoundEffect() {
        return victorySoundEffect;
    }

    public Music getSoundEffectRunning(){
        return soundEffectRunning;
    }

    public Music getWarningMusic(){
        return warningMusic;
    }

    public Sound getSoundEffectTeleport(){
        return soundEffectTeleport;
    }

    public Music getSoundEffectPanting() {
        return soundEffectPanting;
    }

    /**
     * Checks if the player has reached the exit and if the key has been collected.
     *
     * This method is called to verify if the player has reached the exit tile and
     * possesses the key. If both conditions are met, the level is considered
     * complete, and depending on whether it's the last level or not, the game either
     * proceeds to the victory screen or pauses to show the victory panel.
     *
     * @param player The player object that is being checked for exit conditions.
     */
    public void checkExitToNextLevel(Player player) {
        if (player.isCenterTouchingTile(Exit.class) &&
                gameScreen != null && gameScreen.getKey().isCollected()){
            //Gdx.app.log("MazeRunnerGame", "Player is at the exit and has the key.");
            player.hasReachedExit = true;

            if (gameLevel == TOTAL_LEVELS) {
                goToVictoryScreen();
            }

            else if (!gameScreen.isPaused()) {
                gameScreen.setPaused(true);
                gameScreen.createVictoryPanel();
                //this.pause();
                this.getBackgroundMusic().pause();
                this.getPauseMusic().pause();
                this.getVictorySoundEffect().play();
            }

        }
    }

    /**
     * Starts the next level of the game by transitioning to a new game screen.
     *
     * This method is responsible for transitioning from the current level to the next
     * by disposing of the current game screen, creating a new one, and resetting
     * necessary states (such as the key collection status). It also ensures that the
     * game is not paused for the new level.
     *
     * @see GameScreen The screen displayed during gameplay.
     */
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

    /**
     * Creates directional animations from a sprite sheet for a character or object.
     * The sprite sheet is assumed to have frames laid out either in a single row
     * (if `inOneRow` is true) or with each direction in separate rows (if `inOneRow` is false).
     * The method extracts the appropriate texture regions, builds animations for
     * each direction (down, up, left, right), and returns a map of animations.
     *
     * @param spriteSheet The texture containing all the sprite frames.
     * @param inOneRow A boolean indicating whether all frames are on the same row
     *                 (true) or each direction has its own row (false).
     * @param frameDuration The duration of each frame in the animation.
     * @param startY The Y offset on the sprite sheet where the frames begin.
     * @param frameWidth The width of each frame in the sprite sheet.
     * @param frameHeight The height of each frame in the sprite sheet.
     * @param totalFrames The total number of frames for each direction.
     *
     * @return A map where the key is a direction ("down", "up", "left", "right")
     *         and the value is the corresponding animation for that direction.
     */
    public static Map<String, Animation<TextureRegion>> createDirectionalAnimations(
            Texture spriteSheet,
            boolean inOneRow, // are all the frames on the same row? Or is one row for one direction
            float frameDuration,
            int startY,         // The Y offset on the sprite sheet where the row begins
            int frameWidth,
            int frameHeight,
            int totalFrames
    ) {
        // 1. Create frames map
        Map<String, Array<TextureRegion>> framesMap = new HashMap<>();
        framesMap.put("down", new Array<>(TextureRegion.class));
        framesMap.put("up", new Array<>(TextureRegion.class));
        framesMap.put("left", new Array<>(TextureRegion.class));
        framesMap.put("right", new Array<>(TextureRegion.class));

        // 2. Populate with texture slices
        if (inOneRow) { // like extracting the enemy
            for (int col = 0; col < totalFrames; col++) {
                framesMap.get("down").add(new TextureRegion(spriteSheet, (col) * frameWidth, startY, frameWidth, frameHeight));
                framesMap.get("left").add(new TextureRegion(spriteSheet, frameWidth * totalFrames + (col) * frameWidth, startY, frameWidth, frameHeight));
                framesMap.get("right").add(new TextureRegion(spriteSheet, frameWidth * totalFrames * 2 + (col) * frameWidth, startY, frameWidth, frameHeight));
                framesMap.get("up").add(new TextureRegion(spriteSheet, frameWidth * totalFrames * 3 + (col) * frameWidth, startY, frameWidth, frameHeight));
            }
        }
        else{ // like extracting the player
            for (int col = 0; col < totalFrames; col++) {
                framesMap.get("down").add(new TextureRegion(spriteSheet, (col) * frameWidth, startY, frameWidth, frameHeight));
                framesMap.get("right").add(new TextureRegion(spriteSheet, (col) * frameWidth, startY + frameHeight, frameWidth, frameHeight));
                framesMap.get("up").add(new TextureRegion(spriteSheet, (col) * frameWidth, startY + frameHeight * 2, frameWidth, frameHeight));
                framesMap.get("left").add(new TextureRegion(spriteSheet, (col) * frameWidth, startY + frameHeight * 3, frameWidth, frameHeight));
            }
        }

        // 3. Build animations
        Map<String, Animation<TextureRegion>> animations = new HashMap<>();
        animations.put("down", new Animation<>(frameDuration, framesMap.get("down")));
        animations.put("left", new Animation<>(frameDuration, framesMap.get("left")));
        animations.put("right", new Animation<>(frameDuration, framesMap.get("right")));
        animations.put("up", new Animation<>(frameDuration, framesMap.get("up")));

        return animations;
    }

    /**
     * Creates an animation from a sequence of frames on a sprite sheet.
     * The frames are extracted based on the given parameters such as the starting
     * position on the sheet, frame dimensions, and the number of frames to extract.
     *
     * @param sheet The texture containing the sprite sheet with all frames.
     * @param frameDuration The duration each frame is displayed in the animation.
     * @param startX The X offset on the sprite sheet where the frames begin.
     * @param startY The Y offset on the sprite sheet where the frames begin.
     * @param frameWidth The width of each frame in the sprite sheet.
     * @param frameHeight The height of each frame in the sprite sheet.
     * @param frameCount The total number of frames to be included in the animation.
     * @param xIncrement The horizontal increment (in pixels) between each frame on the sprite sheet.
     *
     * @return An `Animation<TextureRegion>` object that represents the animation
     *         created from the specified frames.
     */
    public static Animation<TextureRegion> createAnimation(
            Texture sheet,
            float frameDuration,
            int startX,
            int startY,
            int frameWidth,
            int frameHeight,
            int frameCount,
            int xIncrement
    ) {
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        for (int i = 0; i < frameCount; i++) {
            frames.add(new TextureRegion(sheet,
                    startX + i * xIncrement,
                    startY,
                    frameWidth,
                    frameHeight));
        }
        return new Animation<>(frameDuration, frames);
    }
}
