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

    long keyCollectSoundId, hurtSoundId, runningSoundId, teleportSoundId;

    private final Map<String, Sound> sounds = new HashMap<>();
    private final Map<String, Long> soundIds = new HashMap<>();



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
        skinCraft = new Skin(Gdx.files.internal("craft/craftacular-ui.json")); // Load UI skin
        skinPlain = new Skin(Gdx.files.internal("plain-james/skin/plain-james-ui.json"));
        this.loadAnimation(); // Load character animation
        

        backgroundTexture = new Texture("backgrounds/background.png");

        musicList = new Array<>();
        soundList = new Array<>();

        // Play some background music
        // Background sound
        //CHANGE BACKGROUND MUSIC
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
        soundEffectRunning = Gdx.audio.newMusic(Gdx.files.internal("sounds/running-14658.mp3"));
        musicList.add(soundEffectRunning);
        warningMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/warning.wav"));
        musicList.add(warningMusic);
        soundEffectTeleport = Gdx.audio.newSound(Gdx.files.internal("sounds/teleport.wav"));
        soundList.add(soundEffectTeleport);
        soundEffectPanting = Gdx.audio.newMusic(Gdx.files.internal("sounds/breathing-fast-247451.mp3"));
        musicList.add(soundEffectPanting);

        // Play all sounds and store their IDs
        for (Map.Entry<String, Sound> entry : sounds.entrySet()) {
            long soundId = entry.getValue().play(); // Play the sound
            soundIds.put(entry.getKey(), soundId);  // Store the sound ID
        }

        // Set the initial volume (e.g., 50%)
        float initialVolume = 0.5f;
        for (Map.Entry<String, Long> entry : soundIds.entrySet()) {
            sounds.get(entry.getKey()).setVolume(entry.getValue(), initialVolume); // Set volume for each sound
        }


        goToMenu(); // Navigate to the menu screen
        setVolume(volume);
    }

    public void setVolume(float volume) {
        this.volume = volume;

        // Set volume for all music
        for (Music music : musicList) {
            music.setVolume(volume);
        }

        // Set volume for all sound effects
        for (Map.Entry<String, Long> entry : soundIds.entrySet()) {
            sounds.get(entry.getKey()).setVolume(entry.getValue(), volume);
        }
    }

    // Method to set volume for sound effects
    public void setSoundEffectVolume(float volume) {
        for (Map.Entry<String, Long> entry : soundIds.entrySet()) {
            sounds.get(entry.getKey()).setVolume(entry.getValue(), volume);  // Adjust sound effect volume
        }
    }


    public void selectLevel() {

    }

    public void exitGame(){
        Gdx.app.exit();
        System.exit(-1);
    }

    /*public void setVolume(float volume) {
        this.volume = volume;

        for (Music music : musicList) {
            music.setVolume(volume);
        }
    }*/

    public long playSound(Sound sound) {
        long soundId = sound.play(); // Set volume based on mute state
        playingSoundIds.add(soundId); // Add soundId to the tracking list
        return soundId; // Return the sound ID for tracking
    }


    public void muteAll(boolean mute) {
        this.muted = mute;

        Gdx.app.log("Mute", "Muted: " + muted);

        float targetVolume = mute ? 0.0f : volume;
        for (Music music : musicList) {
            music.setVolume(targetVolume);
        }
    }

    public float getVolume() {
        return muted ? 0 : volume;
    }

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

    // Increase the volume by 0.1 for all music and sounds
    public void increaseVolume() {
        if (!muted) {
            volume = Math.min(1.0f, volume + 0.1f); // Increase volume, clamp to max 1.0
            updateVolumes(); // Apply the new volume to all music and sound effects
        }
    }

    // Decrease the volume by 0.1 for all music and sounds
    public void decreaseVolume() {
        if (!muted) {
            volume = Math.max(0.0f, volume - 0.1f); // Decrease volume, clamp to min 0.0
            updateVolumes(); // Apply the new volume to all music and sound effects
        }
    }

    // Update the volume of all music and sound effects
    private void updateVolumes() {
        // Apply volume to all music tracks
        for (Music music : musicList) {
            music.setVolume(volume); // Set volume for each music
        }

        // Apply volume to all sound effects
        for (Sound sound : soundList) {
           //muteall sound.setVolume(soundEffectHurt, 0.1f); // Set volume for each sound effect
        }
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame(boolean tutorial) {
        // this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen

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

    public void goToGame(){
        goToGame(false);
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
        //createAnimation(walkSheet, 0.1f, 0, 0, playerFrameWidth, playerFrameHeight, 1, playerFrameWidth);

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

    public void muteBGM(){
        backgroundMusic.setVolume(0);
    }
    public void normalizeBGM(){
        backgroundMusic.setVolume(1f);
    }

    public enum skinType{
        CRAFTACULAR,
        PLAIN_JAMES
    }

    // Getter methods
    public Skin getSkin() {
        return skinPlain;
    }

    public Skin getSkin(skinType type) {
        switch (type) {
            case PLAIN_JAMES -> {
                return skinPlain;
            }
            case CRAFTACULAR -> {
                return skinCraft;
            }
        }
        return null;
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

    public Texture getBackgroundTexture() {
        return backgroundTexture;
    }

    public TextureRegion getGesundheitskarteRegion() {
        return gesundheitskarteRegion;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }


    /*public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }*/
    public GameScreen getGameScreen() {
        return gameScreen;
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

    public SelectLevelScreen getSelectLevelScreen() {
        return new SelectLevelScreen(this);
    }

    public void checkExitToNextLevel(Player player) {
        if (player.isCenterTouchingTile(Exit.class) &&
                gameScreen != null && gameScreen.getKey().isCollected()){
            //Gdx.app.log("MazeRunnerGame", "Player is at the exit and has the key.");
            player.hasReachedExit = true;

            if (gameLevel == TOTAL_LEVELS) {
                goToVictoryScreen();
            }

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

    /*public void toggleMute() {
        // Toggle mute
        isMuted = !isMuted;
        if (isMuted) {
            setVolume(0f); // Mute the volume
        } else {
            setVolume(volume); // Restore previous volume
        }
    }

    /public void setVolume(float newVolume) {
        // Set the volume for the game (you can adjust music, sound, etc.)
        if (newVolume == 0f) {
            //volume = newVolume;
        }

        for (Music music : allMusicObjects) {
            music.setVolume(newVolume);
        }

        for (Sound sound : allSoundObjects) {
            sound.setVolume(newVolume);
        }
    }*/

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
