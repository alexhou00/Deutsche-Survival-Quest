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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.*;

import static de.tum.cit.fop.maze.util.Constants.*;

/**
 * The VictoryScreen class represents the screen displayed when the player successfully completes the game.
 * It shows a victory message and provides options to either return to the main menu or exit the game.
 */
public class VictoryScreen implements Screen {
    private final Stage stage;
    private final MazeRunnerGame game;
    Texture backgroundTexture;

    /**
     * Constructs the VictoryScreen.
     * Initializes the screen with a background, buttons for menu navigation, and listeners for button actions.
     *
     * @param game The main game instance.
     */
    public VictoryScreen(MazeRunnerGame game) {
        this.game = game;
        backgroundTexture = new Texture("backgrounds/victorybackground.png");

        var camera = new OrthographicCamera();
        camera.update();
        this.show();
        Viewport viewport = new ScreenViewport(camera);
        this.stage = new Stage (viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("YOU WON!", game.getSkin(), "fraktur");
        table.add(title).padBottom(160).row();

        Map<String, TextButton> buttons = new LinkedHashMap<>();
        buttons.put("goToMenuButton", new TextButton("Menu", game.getSkin()));
        buttons.put("exitGameButton", new TextButton("Exit Game", game.getSkin()));

        Gdx.app.log("VictoryScreen","VictoryScreen is created.");


        for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
            TextButton button = entry.getValue();
            button.addListener(game.getButtonSoundListener());
            table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();
        }

        buttons.get("goToMenuButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("VictoryScreen", "goToMenu button pressed");
                game.goToMenu();
            }
        });

        buttons.get("exitGameButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("VictoryScreen", "Exit Game button pressed");
                game.exitGame();
            }
        });

    }


    /**
     * Called when the screen is shown. Sets the input processor to the stage so it can capture
     * input events such as button presses.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Called every frame to render the victory screen. Clears the screen, draws the background,
     * updates the stage, and renders the UI elements.
     *
     * @param v The time in seconds since the last frame. Used to update animations or movements.
     */
    @Override
    public void render(float v) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getSpriteBatch().end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Called when the screen is resized. It updates the viewport of the stage to fit the new window dimensions.
     *
     * @param i   The new width of the window.
     * @param i1  The new height of the window.
     */
    @Override
    public void resize(int i, int i1) {
        stage.getViewport().update(i, i1, true);

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
