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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static de.tum.cit.fop.maze.util.Constants.*;

/**
 * The SelectLevelScreen class represents the screen where the player selects a level to play.
 * It contains UI elements like buttons for each level and a back button. The screen also handles
 * the transition between different screens, such as the main menu and the game screen.
 */
public class SelectLevelScreen implements Screen  {

    private final Stage stage;
    private final MazeRunnerGame game;
    private final Map<String, TextButton> buttons;
    private final Texture backgroundTexture;
    private final GameScreen gameScreen;
    private final String previousScreen;

    /**
     * Constructs the SelectLevelScreen.
     * Initializes the screen with a camera, viewport, background texture, buttons, and the UI table.
     *
     * @param game          The main game instance.
     * @param previousScreen The name of the previous screen to navigate back to.
     * @param gameScreen    The current game screen to return to if the player comes from the pause screen.
     */
    public SelectLevelScreen(MazeRunnerGame game, String previousScreen, GameScreen gameScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.gameScreen = gameScreen;

        var camera = new OrthographicCamera();
        camera.update();
        this.show();

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage (viewport, game.getSpriteBatch());

        backgroundTexture = new Texture("backgrounds/background.png");

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


        table.add(new Label("Select The Level", game.getSkin(), "fraktur")).padBottom(40).row();

        buttons = new LinkedHashMap<>();

        Gdx.app.log("SelectLevelScreen", "screen created ");
        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            buttons.put("Level " + i, new TextButton("Level "+ i, game.getSkin()));
            int level = i;
            buttons.get("Level " + i).addListener(new ChangeListener() {
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.log("SelectLevelScreen", "changed ");
                    Gdx.app.log("SelectLevelScreen", "Level " + level + " selected");
                    game.setGameLevel(level);
                    game.goToGame();
                    game.getPauseMusic().pause();

                }
            });
            buttons.get("Level " + i).addListener(game.getButtonSoundListener());
            table.add(buttons.get("Level " + i)).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();
        }


        TextButton backButton = new TextButton("Back", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Menu".equals(previousScreen)) {
                    game.setScreen(new MenuScreen(game)); // Go back to MenuScreen
                    game.getPauseMusic().pause();
                } else if ("Pause".equals(previousScreen)) {
                    game.setScreen(gameScreen); // Go back to GameScreen (PauseWindow)
                }
            }
        });
        backButton.addListener(game.getButtonSoundListener());
        table.add(backButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).padTop(20).row();


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
     * Called every frame to render the screen. Clears the screen, draws the background,
     * and updates the stage to render all UI components and buttons.
     *
     * @param delta The time in seconds since the last frame. Used to update animations or movements.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.getSpriteBatch().begin();
        game.getSpriteBatch().end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }


    /**
     * Called when the screen is resized. It updates the viewport of the stage to fit the new window dimensions.
     *
     * @param width  The new width of the window.
     * @param height The new height of the window.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
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
        // Dispose of the stage when screen is disposed
        stage.dispose();
    }
}
