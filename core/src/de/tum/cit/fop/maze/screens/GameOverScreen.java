package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.LinkedHashMap;
import java.util.Map;

public class GameOverScreen implements Screen {

    private final Stage stage;
    MazeRunnerGame game;
    Texture backgroundTexture;
    private final Map<String, TextButton> buttons;

    /**
     * Constructor for GameOverScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameOverScreen(MazeRunnerGame game) {
        this.game = game;

        var camera = new OrthographicCamera();
        camera.update();

        this.show();

        Viewport viewport = new ScreenViewport(camera); // Create a viewport with the camera
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        backgroundTexture = new Texture("backgrounds/background.png");

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        Label title = new Label("Game Over", game.getSkin(), "title");
        title.getStyle().font.getData().setScale(0.75f);
        table.add(title).padBottom(80).row();

        // Add buttons for restarting or exiting the game
        final float BUTTON_WIDTH = 300f;
        final float BUTTON_PADDING = 10f; // Vertical padding

        buttons = new LinkedHashMap<>();
        buttons.put("restartButton", new TextButton("Restart Game", game.getSkin()));
        buttons.put("exitGameButton", new TextButton("Exit Game", game.getSkin()));

        Gdx.app.log("GameOverScreen", "GameOverScreen is created.");

        // Add buttons to the table with padding
        for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
            TextButton button = entry.getValue();
            table.add(button).width(BUTTON_WIDTH).padBottom(BUTTON_PADDING).row();
        }


        buttons.get("restartButton").addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("GameOverScreen", "Restart Game button pressed");
                game.goToGame(); // Restart the game
            }
        });

        buttons.get("exitGameButton").addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("GameOverScreen", "Exit Game button pressed");
                Gdx.app.exit();
                System.exit(-1);
            }
        });

        Gdx.app.log("GameOverScreen", "ChangeListener is added.");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen

        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getSpriteBatch().end();


        // Draw a simple label for debugging
        game.getSpriteBatch().begin();//BEGİN priteBatch EVERYTİME YOU WANNA DRAW
        //BitmapFont font = new BitmapFont();
        //font.draw(game.getSpriteBatch(), "Game Over", Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2);
        game.getSpriteBatch().end();//YOU NEED TO END EVERYTHİME YOU WANNA FİNİSH PUTTİNG STH ON THE SECREEN

        // Update and draw the stage (buttons should be visible)
        //stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage); // Set the input processor to the GameOver screen
        Gdx.app.log("GameOverScreen", "GameOverScreen is now showing.");
    }

    @Override
    public void resize(int i, int i1) {
        stage.getViewport().update(i, i1, true); // Update the stage viewport on resize

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();

    }
}
