package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static de.tum.cit.fop.maze.Constants.MAX_PLAYER_LIVES;

/**
 * The Object class handles rendering of static or reusable game objects such as hearts for the HUD.
 */
public class ObjectRenderer {

    private final Texture texture;
    private final TextureRegion fullHeartRegion, emptyHeartRegion;

    public ObjectRenderer(String texturePath) {
        // Load the texture and create a region for the heart
        texture = new Texture(Gdx.files.internal(texturePath));
        fullHeartRegion = new TextureRegion(texture, 64, 2, 14, 13); // Extract heart from texture
        emptyHeartRegion = new TextureRegion(texture, 128, 2, 14, 13);
    }

    /**
     * Draws hearts on the HUD to represent player lives.
     *
     * @param batch      The SpriteBatch used for rendering.
     * @param lives      Number of lives to render.
     * @param startX     Starting X position.
     * @param startY     Starting Y position.
     * @param spacing    Space between hearts.
     * @param scale      enlarge / shrink scale
     */
    public void drawHearts(SpriteBatch batch, int lives, float startX, float startY, float spacing, float scale) {
        for (int i = 0; i < lives; i++) {
            batch.draw(fullHeartRegion, startX + i * spacing, startY, 14 * scale, 13 * scale);
        }
        for (int i=MAX_PLAYER_LIVES; i>lives; i--) {
            batch.draw(emptyHeartRegion, startX + (i-1) * spacing, startY, 14 * scale, 13 * scale);
        }
    }

    /**
     * Disposes of the texture to free resources.
     */
    public void dispose() {
        texture.dispose();
    }
}