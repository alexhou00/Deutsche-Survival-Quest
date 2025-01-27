package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.base.StaticObject;

/**
 * Represents the Key object to be collected to go to the exit and advance to the next level.
 * The key can be collected by the player and is rendered on the screen if not collected.
 */
public class Key extends StaticObject {

    private boolean isCollected;
    private MazeRunnerGame game;

    /**
     * Constructs a Key object with specified position, size, hitbox dimensions,
     * and display size on screen.
     *
     * @param x               the x-coordinate of the key
     * @param y               the y-coordinate of the key
     * @param width           the width of the key
     * @param height          the height of the key
     * @param hitboxWidth     the width of the key's hitbox
     * @param hitboxHeight    the height of the key's hitbox
     * @param widthOnScreen   the width of the key's representation on the screen
     * @param heightOnScreen  the height of the key's representation on the screen
     */
    public Key(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, MazeRunnerGame game) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.isCollected = false;
        this.game = game;
    }

    public boolean collisionWithPlayer(Player player) {
        return this.getHitbox().overlaps(player.getHitbox());
    }

    /**
     * Marks the key as collected and prints a message to the console.
     * Note that the key should be checked if collected at first by checking if touching player in some other places
     */
    public void collect() {
        if (this.isCollected) return; // prevent recollecting
        isCollected = true;
        if (game.isMuted()){
            game.getSoundEffectKey().pause();
        }
        if (!game.isMuted()){
            game.getSoundEffectKey().play();
        }
        System.out.println("Key collected!"); // Debug message
    }

    /**
     * Resets the key to its original position and marks it as uncollected.
     */
    public void returnToPosition() {
        setPosition(x, y);
        setCollected(false); // Reset the collected status
        System.out.println("Key returned to original position: (" + x + ", " + y + ")");
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isCollected() {
        return isCollected;
    }

    /**
     * Sets the collected status of the key.
     *
     * @param collected the new collected status of the key
     *                  could be used to reset the status of the key whenever a level restarts
     */
    public void setCollected(boolean collected) {
        isCollected = collected;
    }

    public void renderTheKey(SpriteBatch spriteBatch, Texture keyTexture) {
        if (!isCollected) {
            spriteBatch.draw(
                    keyTexture,
                    getX(),
                    getY(),
                    getWidthOnScreen(),
                    getHeightOnScreen()
            );
        }
    }
}
