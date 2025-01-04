package de.tum.cit.fop.maze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class SpotlightEffect extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture blackTexture;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // @Override
    public void create() {
        batch = new SpriteBatch();

        // Create a black texture for the dimmed background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.7f); // Semi-transparent black
        pixmap.fill();

        blackTexture = new Texture(pixmap);
    }

    public void render(OrthographicCamera camera, float spotlightX, float spotlightY, float spotlightRadius) {
        //Matrix4 matrix = new Matrix4();
        //matrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //batch.setProjectionMatrix(matrix);

        //camera.setToOrtho(false);
        batch.setProjectionMatrix(camera.combined); // IMPORTANT: it has to follow the camera so that things don't get distorted or displaced

        //batch.setTransformMatrix(camera.view);
        //batch.setProjectionMatrix(camera.projection);

        batch.begin();

        // Draw the semi-transparent black overlay
        batch.setColor(0, 0, 0, 1f); // 70% opaque black
        batch.draw(blackTexture, 0, 0, Gdx.graphics.getWidth() * 20, Gdx.graphics.getHeight() * 20);

        // End SpriteBatch before using ShapeRenderer
        batch.end();


        // Enable blending for erasing the spotlight area
        Gdx.gl.glEnable(GL20.GL_BLEND);

        /* {@link https://stackoverflow.com/questions/45973258/libgdx-basic-2d-lighting-dont-know-what-to-do} */
        Gdx.gl.glBlendFunc(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA); // IDK why but this worked

        // Use ShapeRenderer to draw the transparent circle

        shapeRenderer.setProjectionMatrix(camera.combined);  // IMPORTANT: make it follow the camera {@link: https://stackoverflow.com/questions/33703663/understanding-the-libgdx-projection-matrix}

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 0.8f); // White color with almost full alpha

        shapeRenderer.circle(spotlightX, spotlightY, spotlightRadius);
        shapeRenderer.circle(spotlightX, spotlightY, spotlightRadius); // apply twice to make it brighter
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
