package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.base.GameObject;

public class Collectibles extends GameObject {

    public enum Type{
        HEART,
        PRETZEL,
        SPEED_BOOST,
        SHIELD,
        COIN,
        STAMINA, // the potion
        GESUNDHEITSKARTE
    }

    private final Type type; // The type of collectible
    private boolean collected; // Whether the collectible has been picked up
    private final TextureRegion textureRegion; // Texture to render the collectible

    private Player player = null;

    Sound soundEffectCollect;
    protected final MazeRunnerGame game;

    private String function = null;

    /**
     * Constructs a new GameObject instance with specified parameters.
     *
     * @param x              World x-coordinate of the object's initial position (origin is the center of the sprite)
     * @param y              World y-coordinate of the object's initial position. (origin is the center of the sprite)
     * @param width          The width of the object.
     * @param height         The height of the object.
     * @param hitboxWidth    The width of the object's hitbox.
     * @param hitboxHeight   The height of the object's hitbox.
     * @param widthOnScreen  The width of the object as displayed on screen.
     * @param heightOnScreen The height of the object as displayed on screen.
     */
    public Collectibles(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, Type type, MazeRunnerGame game) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.type = type;
        this.textureRegion = null; //textureRegion;
        this.collected = false;
        this.game = game;
    }

    /**
     * Initializes the collectable item with a reference to the player and a sound effect.
     *
     * This method sets the player reference, allowing the collectable to interact with the player,
     * and assigns a sound effect that plays when the item is collected.
     *
     * @param player The player who can collect this item.
     * @param soundEffect The sound effect to be played upon collection.
     */
    public void init(Player player, Sound soundEffect) {
        this.player = player;
        this.soundEffectCollect = soundEffect;
    }

    /**
     * Checks if the collectible has been picked up.
     *
     * @return True if collected, false otherwise.
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Marks the collectible as collected.
     */
    public void collect() {
        this.collected = true;
    }

    /**
     * Renders the collectible on the screen.
     *
     * @param batch The SpriteBatch used for rendering.
     */
    public void render(SpriteBatch batch, TextureRegion frame) {
        if (!collected) {
            batch.draw(frame, getX() - getWidthOnScreen() / 2, getY() - getHeightOnScreen() / 2, getWidthOnScreen(), getHeightOnScreen());
        }
    }

    /**
     * Gets the type of this collectible. (e.g., HEART, COIN...)
     *
     * @return The type of collectible.
     */
    public Type getType() {
        return type;
    }

    public String getFunction() {
        switch (this.getType()) {
            case HEART -> function = "It can restore 1 life!";
            case PRETZEL -> function = "It can restore 1.25 lives!";
            case GESUNDHEITSKARTE -> function = "It can restore 1.5 lives!";
            case COIN -> function = "You need coins to get high scores!";
            case STAMINA -> function = "You'll have extra Stamina wheel!";
        }
        return (function!=null) ? function : "";
    }

    /**
     * implement the functionalities of each type of collectibles
     */
    public void update() {
        // Placeholder for looping logic
        if (!isCollected() && isTouching(player)){
            collected = true;
            if (game.isMuted()){
                soundEffectCollect.pause();
            }
            else{
                soundEffectCollect.play(game.getSoundManager().getVolume());
            }

            switch (this.getType()) {
                case HEART:
                    player.setLives(player.getLives() + 1);
                    break;
                case PRETZEL:
                    player.setLives(player.getLives() + 1.25f);
                    function = "It can restore 1.25 lives!";
                    break;
                case GESUNDHEITSKARTE:
                    player.setLives(player.getLives() + 1.5f);
                    function = "It can restore 1.5 lives!";
                    break;
                case COIN:
                    player.setCoins(player.getCoins() + 1);
                    function = "You need coins to get high scores!";
                    break;
                case STAMINA:
                    player.setCurrentStaminaMultiplier(2);
                    player.setStamina(Player.maxStamina * player.getCurrentStaminaMultiplier()); // 100 * 2
                    function = "It will give you an extra Stamina wheel!";
                    break;

            }
        }
    }
}
