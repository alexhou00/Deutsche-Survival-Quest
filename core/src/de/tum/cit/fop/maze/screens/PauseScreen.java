package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.LinkedHashMap;
import java.util.Map;

public class PauseScreen implements Screen {
    private final Stage stage;
    private final MazeRunnerGame game;
    private final Texture backgroundTexture;
    private final Map<String, TextButton> buttons;

    /**
     * Constructor for PauseScreen. Sets up the stage and UI elements for pausing the game.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public PauseScreen(MazeRunnerGame game) {
        this.game = game;

        // Camera and stage setup
        OrthographicCamera camera = new OrthographicCamera();
        camera.update();

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        // Background texture
        backgroundTexture = new Texture("pause_background.png");

        // Create UI table
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a label as a title
        table.add(new Label("Game Paused", game.getSkin(), "title")).padBottom(80).row();

        // Button dimensions
        final float BUTTON_WIDTH = 300f;
        final float BUTTON_PADDING = 10f;

        // Define buttons
        buttons = new LinkedHashMap<>();
        buttons.put("resumeGameButton", new TextButton("Resume Game", game.getSkin()));
        buttons.put("selectLevelButton", new TextButton("Select Level", game.getSkin()));
        buttons.put("exitGameButton", new TextButton("Exit Game", game.getSkin()));
        buttons.put("OptionsButton", new TextButton("Options Menu", game.getSkin()));

        // Add buttons to the table
        for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
            TextButton button = entry.getValue();
            table.add(button).width(BUTTON_WIDTH).padBottom(BUTTON_PADDING).row();
        }

        // Set up listeners
       /* buttons.get("resumeGameButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("PauseScreen", "Resume Game button pressed");
                game.resumeGame(); // Resume the game
            }
        });*/

        /*buttons.get("selectLevelButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("PauseScreen", "Select Level button pressed");
                game.goToLevelSelection(); // Navigate to level selection screen
            }
        });*/

        buttons.get("exitGameButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("PauseScreen", "Exit Game button pressed");
                Gdx.app.exit(); // Exit the game
            }
        });




        Gdx.app.log("PauseScreen", "Pause screen created.");

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen

        // Draw background
        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getSpriteBatch().end();

        // Update and draw the stage
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}


