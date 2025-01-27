package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.rendering.Panel;

import java.util.HashMap;
import java.util.Map;

public class OptionsScreen implements Screen {

    private final Stage stage;
    private final MazeRunnerGame game;
    private final Map<String, TextButton> buttons;
    private Texture backgroundTexture;
    private Skin skin;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private GameOverScreen gameOverScreen;
    private OptionsScreen optionsScreen;

    public OptionsScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        camera.update();

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());
        backgroundTexture = new Texture("backgrounds/background.png");
        skin = game.getSkin();  // Assuming the skin is already set in your game

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        buttons = new HashMap<>();
        buttons.put("Volume", new TextButton("Volume", skin));
        buttons.put("Mute", new TextButton("Mute / Unmute", skin));
        buttons.put("Back", new TextButton("Back", skin));

        // Add buttons to the table
        table.add(new Label("Options", skin, "title")).padBottom(80).row();

        for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
            table.add(entry.getValue()).width(300).padBottom(20).row();
        }

        // Volume Button Listener
        buttons.get("Volume").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Open Volume control slider or modify game volume
                game.setVolume(0.5f);  // Example: Set volume to 50%
                Gdx.app.log("OptionsScreen", "Volume changed");
            }
        });

        // Mute Button Listener
        buttons.get("Mute").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean isMuted = !game.isMuted();
                game.muteAll(isMuted);
                Gdx.app.log("OptionsScreen", "Mute toggled");
            }
        });

        // Back Button Listener
        buttons.get("Back").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Return to the previous screen (e.g., Main Menu)
                game.setScreen(new MenuScreen(game)); // Example: Going back to MenuScreen
            }
        });
    }

    public void goToOptionsScreen() {
        if (optionsScreen == null) {
            // TODO: this will be changed in the future once we can select our own levels
            game.setScreen(this);
        }
        // Set the current screen to MenuScreen

        if (menuScreen != null) {
            menuScreen.dispose(); // Dispose the menu screen if it exists
            menuScreen = null;
        }

        if (gameOverScreen != null) {
            gameOverScreen.dispose(); // Dispose the menu screen if it exists
            gameOverScreen = null;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0);
        game.getSpriteBatch().end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Handle pause functionality (if needed)
    }

    @Override
    public void resume() {
        // Handle resume functionality (if needed)
    }

    @Override
    public void hide() {
        // Clean up when screen is hidden
    }

    @Override
    public void dispose() {
        // Dispose resources (textures, etc.)
        stage.dispose();
        backgroundTexture.dispose();
    }
}
