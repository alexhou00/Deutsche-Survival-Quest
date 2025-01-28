package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import static de.tum.cit.fop.maze.util.Constants.MAX_PLAYER_LIVES;

/**
 * The ObjectRenderer class handles rendering of static or reusable game objects
 * such as hearts for the HUD or the arrow that points at the exit.
 */
public class ElementRenderer {

    private final Texture texture;
    private final TextureRegion fullHeartRegion, threeQuartersHeartRegion, halfHeartRegion, oneQuarterHeartRegion, emptyHeartRegion, coinRegion;

    private final Sprite arrow;
    private float arrowRotatedX = 0, arrowRotatedY = 0;

    public ElementRenderer(String texturePath) {
        // Load the texture and create a region for the heart
        texture = new Texture(Gdx.files.internal(texturePath));

        fullHeartRegion = extractHeart(texture, 64); // Extract heart from texture
        threeQuartersHeartRegion = extractHeart(texture, 80);
        halfHeartRegion = extractHeart(texture, 96);
        oneQuarterHeartRegion = extractHeart(texture, 112);
        emptyHeartRegion = extractHeart(texture, 128);

        coinRegion = extractCoin(texture, 928);

        TextureRegion arrowRegion = new TextureRegion(texture, 490, 10, 20, 10);
        arrow = new Sprite(arrowRegion);
    }

    private TextureRegion extractHeart(Texture texture, int x) {
        return new TextureRegion(texture, x, 2, 14, 13);
    }

    private TextureRegion extractCoin(Texture texture, int x) {
        return new TextureRegion(texture, x, 0, 14, 13);
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

        if (lives > 0){
            // Draw empty hearts
            for (int i = (int)Math.ceil(lives); i < MAX_PLAYER_LIVES; i++) {
                batch.draw(emptyHeartRegion, startX + i * spacing, startY, 14 * scale, 13 * scale);
            }
        }
    }

    /*public void drawCoins(SpriteBatch batch, int collectedCoins, int totalCoins, float startX, float startY, float spacing, float scale) {
        // Draw collected coins
        for (int i = 0; i < collectedCoins; i++) {
            batch.draw(coinRegion, startX + i * spacing, startY, 14 * scale, 14 * scale);
        }

        // Draw empty coins (optional for total coin display)
        for (int i = collectedCoins; i < totalCoins; i++) {
            batch.draw(coinRegion, startX + i * spacing, startY, 14 * scale, 14 * scale);
        }
    }*/

    /**
     * Draws an arrow sprite at a specific position with a rotation.
     *
     * <p>This method draws an arrow sprite at the specified coordinates, rotating it by the given number of degrees.
     * The arrow's origin and position are adjusted for proper alignment and scaling on the screen. The method uses
     * a {@code SpriteBatch} to draw the arrow with a scaled size and adjusted rotation based on the provided
     * parameters.
     *
     * @param batch the {@code SpriteBatch} used to draw the arrow on the screen
     * @param degrees the rotation of the arrow in degrees (clockwise)
     * @param x the x-coordinate for the position where the arrow should be drawn
     * @param y the y-coordinate for the position where the arrow should be drawn
     */
    public void drawArrow(SpriteBatch batch, float degrees, float x, float y) {
        // drawing a sprite is different from drawing a texture region
        // for drawing a sprite, we use sprite.draw(spriteBatch) instead of spriteBatch.draw(textureRegion)
        float scale = 1.5f; // enlarging the arrow on the screen, but the offset from the origin of the player needs to be adjusted
        arrow.setOrigin(arrow.getWidth() / 2, -45 / scale); // 10,-45/1.5 = 10,-30
        arrow.setPosition(x - arrow.getWidth() / 2, y + 45 / scale); // 10,
        arrow.setRotation(degrees); // we need to rotate the arrow, so it's more convenient to make it a sprite
        arrow.setScale(scale);
        arrow.draw(batch);
        float radiusFromOriginToArrowCenter = arrow.getHeight() / 2 + 45;
        arrowRotatedX = x + radiusFromOriginToArrowCenter * MathUtils.cosDeg((degrees - 270)%360);
        arrowRotatedY = y + radiusFromOriginToArrowCenter * MathUtils.sinDeg((degrees - 270)%360);

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

    public float getArrowRotatedX() {
        return arrowRotatedX;
    }

    public float getArrowRotatedY() {
        return arrowRotatedY;
    }
}