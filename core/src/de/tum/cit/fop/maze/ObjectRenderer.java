package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static de.tum.cit.fop.maze.Constants.MAX_PLAYER_LIVES;

/**
 * The ObjectRenderer class handles rendering of static or reusable game objects
 * such as hearts for the HUD or the arrow that points at the exit.
 */
public class ObjectRenderer {

    private final Texture texture;
    private final TextureRegion fullHeartRegion, threeQuartersHeartRegion, halfHeartRegion, oneQuarterHeartRegion, emptyHeartRegion;
    private final TextureRegion arrowRegion;

    private final Sprite arrow;

    public ObjectRenderer(String texturePath) {
        // Load the texture and create a region for the heart
        texture = new Texture(Gdx.files.internal(texturePath));

        fullHeartRegion = extractHeart(texture, 64); // Extract heart from texture
        threeQuartersHeartRegion = extractHeart(texture, 80);
        halfHeartRegion = extractHeart(texture, 96);
        oneQuarterHeartRegion = extractHeart(texture, 112);
        emptyHeartRegion = extractHeart(texture, 128);

        arrowRegion = new TextureRegion(texture, 490, 10, 20, 10);
        arrow = new Sprite(arrowRegion);
    }

    private TextureRegion extractHeart(Texture texture, int x) {
        return new TextureRegion(texture, x, 2, 14, 13);
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
    public void drawHearts(SpriteBatch batch, float lives, float startX, float startY, float spacing, float scale) {
        int livesInt = (int) lives;
        float livesDecimal = lives - livesInt;

        // Draw full hearts
        for (int i = 0; i < livesInt; i++) {
            batch.draw(fullHeartRegion, startX + i * spacing, startY, 14 * scale, 13 * scale);
        }

        // Draw partial heart if needed
        if (livesDecimal > 0) {
            TextureRegion partialHeart;
            if (livesDecimal >= 0.75) {
                partialHeart = threeQuartersHeartRegion;
            } else if (livesDecimal >= 0.5) {
                partialHeart = halfHeartRegion;
            } else {
                partialHeart = oneQuarterHeartRegion;
            }
            batch.draw(partialHeart, startX + livesInt * spacing, startY, 14 * scale, 13 * scale);
        }


        // Draw empty hearts
        for (int i = (int)Math.ceil(lives); i < MAX_PLAYER_LIVES; i++) {
            batch.draw(emptyHeartRegion, startX + i * spacing, startY, 14 * scale, 13 * scale);
        }
    }

    public void drawArrow(SpriteBatch batch, float degrees, float x, float y) {
        float scale = 1.5f;
        arrow.setOrigin(arrow.getWidth() / 2, -45 / scale);
        arrow.setPosition(x - arrow.getWidth() / 2, y + 45 / scale);
        arrow.setRotation(degrees);
        arrow.setScale(scale);
        arrow.draw(batch);
    }

    /**
     * Disposes of the texture to free resources.
     */
    public void dispose() {
        texture.dispose();
    }


    public Sprite getArrow() {
        return arrow;
    }
}