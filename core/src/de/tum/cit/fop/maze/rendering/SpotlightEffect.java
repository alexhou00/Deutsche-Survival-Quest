package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import static de.tum.cit.fop.maze.util.Constants.*;

/**
 * The {@code SpotlightEffect} class creates a spotlight effect <br>
 * A semi-transparent black overlay is drawn, with a circular spotlight area
 * to highlight specific parts of the screen.
 */
public class SpotlightEffect extends ApplicationAdapter {
    private final SpriteBatch batch;
    private final Texture blackTexture;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    /**
     * Constructor for SpotlightEffect.
     * Initializes the resources needed for the spotlight effect.
     */
    public SpotlightEffect() {
        batch = new SpriteBatch(); /* the spotlight effect has to use another independent batch,
        or otherwise it will be chaotic */

        // Create a black texture for the dimmed background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888); // width and height are in pixels, so 1x1 pixel here
        pixmap.setColor(0, 0, 0, 0.7f); // Semi-transparent black
        pixmap.fill();

        blackTexture = new Texture(pixmap);
    }

    /**
     * Renders the spotlight effect.
     *
     * @param camera           The {@link OrthographicCamera} used for rendering.
     * @param spotlightX       The x-coordinate of the spotlight center.
     * @param spotlightY       The y-coordinate of the spotlight center.
     * @param spotlightRadius  The radius of the spotlight circle.
     * @param secondSpotlightScale The spotlight will be applied twice.
     *                             This is the scaling factor
     *                             used for the second time
     *                             to create a brighter inner spotlight area to create a cartoon-like effect.
     */
    public void render(OrthographicCamera camera, float spotlightX, float spotlightY, float spotlightRadius, float secondSpotlightScale) {
        batch.setProjectionMatrix(camera.combined); // IMPORTANT: it has to follow the camera so that things don't get distorted or displaced

        // begin the batch to draw the black overlay
        batch.begin();

        // Draw the semi-transparent black overlay
        batch.setColor(0, 0, 0, 1f); // // Fully opaque black (opacity already defined in `blackTexture`)
        // enlarge it by 100x of the world size and place it in the center of the world
        batch.draw(blackTexture, getWorldWidth() * -50, getWorldHeight() * -50, getWorldWidth() * 100, getWorldHeight() * 100);

        // End SpriteBatch before using ShapeRenderer
        batch.end();


        // Enable blending for erasing the spotlight area
        Gdx.gl.glEnable(GL20.GL_BLEND);

        /* uses a blend function that works for creating a spotlight effect
        this blend function ensures the spotlight area becomes transparent
        see https://stackoverflow.com/questions/45973258/libgdx-basic-2d-lighting-dont-know-what-to-do,
        in OP's code */
        Gdx.gl.glBlendFunc(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA); // IDK why but this blend function worked

        // Use ShapeRenderer to draw the transparent circle
        shapeRenderer.setProjectionMatrix(camera.combined);  // IMPORTANT:
        // a batch/renderer has to follow the camera, or else things might get distorted after resizing a screen
        // https://stackoverflow.com/questions/33703663/understanding-the-libgdx-projection-matrix

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 0.8f); // White color with almost full alpha

        shapeRenderer.circle(spotlightX, spotlightY, spotlightRadius);
        shapeRenderer.circle(spotlightX, spotlightY, spotlightRadius * secondSpotlightScale); // apply twice to make it brighter
        shapeRenderer.end();

        // Reset blending mode
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        batch.dispose();
        blackTexture.dispose();
    }
}
