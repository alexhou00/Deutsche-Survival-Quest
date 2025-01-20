package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.base.GameObject;

public class Collectibles extends GameObject {

    public enum Type{
        HEART,
        PRETZEL,
        SPEED_BOOST,
        SHIELD,
        COIN,
        STAMINA
    }

    private final Type type; // The type of collectible
    private boolean collected; // Whether the collectible has been picked up
    private final TextureRegion textureRegion; // Texture to render the collectible

    private Player player = null;

    Sound soundEffectCollect;

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
    public Collectibles(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, Type type) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.type = type;
        this.textureRegion = null; //textureRegion;
        this.collected = false;
    }

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

    /**
     * implement the functionalities of each type of collectibles
     */
    public void update() {
        // Placeholder for looping logic
        if (!isCollected() && isTouching(player)){
            collected = true;
            soundEffectCollect.play();
            switch (this.getType()) {
                case HEART:
                    player.setLives(player.getLives() + 1);
                    break;
                case PRETZEL:
                    player.setLives(player.getLives() + 1.25f);
                    break;
                case COIN:
                    player.setCoins(player.getCoins() + 1);
                    break;
                case STAMINA:
                    player.setCurrentStaminaMultiplier(2);
                    player.setStamina(Player.maxStamina * player.getCurrentStaminaMultiplier()); // 100 * 2
                    break;

            }
        }
    }
}
