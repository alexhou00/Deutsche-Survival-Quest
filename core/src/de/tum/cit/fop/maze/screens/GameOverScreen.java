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

import static de.tum.cit.fop.maze.util.Constants.*;

public class GameOverScreen implements Screen {

    private final Stage stage;
    MazeRunnerGame game;
    Texture backgroundTexture;
    private final Map<String, TextButton> buttons;

    /**
     * Constructs a new GameOverScreen for the MazeRunner game.
     *
     * <p>This constructor sets up the screen's user interface, including the camera, viewport, stage,
     * background texture, title label, and buttons for restarting or exiting the game. It also defines
     * listeners for the buttons to handle user input. When the restart button is pressed, the game restarts,
     * and when the exit button is pressed, the application exits.
     *
     * @param game the MazeRunner game instance used for accessing game-related functionality such as
     *             restarting the game and accessing the sprite batch
     */
    public GameOverScreen(MazeRunnerGame game) {
        this.game = game;

        var camera = new OrthographicCamera();
        camera.update();

        this.show();

        Viewport viewport = new ScreenViewport(camera); // Create a viewport with the camera
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        backgroundTexture = new Texture("backgrounds/game_over.png");

        Table table = new Table(); // Create a table for layout
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        Label title = new Label("Game Over", game.getSkin(), "fraktur-white");
        title.getStyle().font.getData().setScale(1);
        table.add(title).padBottom(80).row();

        // Add buttons for restarting or exiting

        buttons = new LinkedHashMap<>();
        buttons.put("restartButton", new TextButton("Restart Level", game.getSkin()));
        buttons.put("exitGameButton", new TextButton("Exit Game", game.getSkin()));

        Gdx.app.log("GameOverScreen", "GameOverScreen is created.");

        // Add buttons to the table with padding
        for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
            TextButton button = entry.getValue();
            button.addListener(game.getButtonSoundListener());
            table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();
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

    /**
     * Renders the GameOverScreen, including the background, labels, and UI elements (such as buttons).
     *
     * <p>This method is responsible for clearing the screen, drawing the background texture, rendering the
     * debugging label, and updating the stage to ensure UI elements like buttons are visible and
     * interactive. The {@code SpriteBatch} is used to draw the background and the label, while the stage is
     * drawn for UI updates.
     *
     * <p>It also ensures {@code SpriteBatch} is started and ended properly
     * before and after rendering elements.
     *
     * @param delta the time elapsed since the last frame, used for any time-based calculations
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen

        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getSpriteBatch().end();


        // Draw a simple label for debugging
        game.getSpriteBatch().begin();//BEGİN priteBatch EVERYTİME YOU WANNA DRAW
        game.getSpriteBatch().end();//YOU NEED TO END EVERYTHİME YOU WANNA FİNİSH PUTTİNG STH ON THE SECREEN

        // Update and draw the stage (buttons should be visible)
        Gdx.input.setInputProcessor(stage);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

    /**
     * Displays the GameOverScreen and sets up the input processor for the screen.
     *
     * <p>This method is called to show the GameOver screen and make it the active screen for handling user input.
     * It sets the input processor to the current stage, ensuring that input events such as button presses
     * are handled by the GameOver screen. It also logs that the GameOverScreen is now being displayed (for debugging).
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage); // Set the input processor to the GameOver screen
        Gdx.app.log("GameOverScreen", "GameOverScreen is now showing.");
    }

    /**
     * Resizes the viewport of the GameOverScreen when the window size changes.
     *
     * <p>This method is called when the window or screen size changes. It updates the viewport of the stage
     * to match the new dimensions, ensuring that UI elements (such as buttons) are correctly positioned
     * and scaled for the new screen size.
     *
     * @param i the new width of the window or screen
     * @param i1 the new height of the window or screen
     */
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
