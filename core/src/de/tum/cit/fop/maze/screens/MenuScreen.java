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
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It extends the LibGDX Screen class and sets up the UI components for the menu.
 */
public class MenuScreen implements Screen {

    GameScreen gameScreen;
    private final Stage stage;
    MazeRunnerGame game;
    Texture backgroundTexture;
    SelectLevelScreen selectLevelScreen;
    OptionsScreen optionsScreen;

    Map<String, TextButton> buttons = new LinkedHashMap<>();
    Table table = new Table(); // Create a table for layout

    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game) {
        this.game = game;

        var camera = new OrthographicCamera();
        // camera.zoom = 1.5f; // Set camera zoom for a closer view
        camera.update();

        this.show();

        selectLevelScreen = new SelectLevelScreen(game, "previous screen", gameScreen);
        optionsScreen = new OptionsScreen(game);
        Viewport viewport = new ScreenViewport(camera); // Create a viewport with the camera
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements
        backgroundTexture = new Texture("backgrounds/background.png");

        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        Label title = new Label("Deutsche Survival Quest", game.getSkin(), "fraktur");
        title.getStyle().font.getData().setScale(1);
        table.add(title).padBottom(80).row();

        this.gameScreen = new GameScreen(game);

        buttons.put("startGameButton", new TextButton((game.getGameLevel()<=1?"Start Game":"Continue Game"), game.getSkin()));
        buttons.put("selectLevelButton", new TextButton("Select Level", game.getSkin()));
        buttons.put("optionsButton", new TextButton("Options", game.getSkin()));
        buttons.put("tutorialButton", new TextButton("How to Play", game.getSkin()));
        buttons.put("exitGameButton", new TextButton("Exit Game", game.getSkin()));

        Gdx.app.log("MenuScreen","MenuScreen is created.");

        // Add buttons to the table with padding
        for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
            TextButton button = entry.getValue();
            button.addListener(game.getButtonSoundListener());
            table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();
        }

        // Set up listeners
        buttons.get("startGameButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MenuScreen", "Start Game button pressed");
                game.goToGame(); // Change to the game screen when button is pressed
                game.getPauseMusic().pause();
            }
        });

        buttons.get("selectLevelButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MenuScreen", "Select Level button pressed");
                game.setScreen(new SelectLevelScreen(game, "Menu", gameScreen));
            }
        });

        buttons.get("optionsButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MenuScreen", "Options button pressed");
                optionsScreen.goToOptionsScreen();
            }
        });
        buttons.get("tutorialButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MenuScreen", "\"How to Play\" button pressed");
                game.goToGame(true); // Change to the game screen when button is pressed
                game.getPauseMusic().pause();

            }
        });

        buttons.get("exitGameButton").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MenuScreen", "Exit Game button pressed");
                game.exitGame();
            }
        });

        Gdx.app.log("MenuScreen", "ChangeListener is added.");
    }

    /**
     * The render method is called every frame to update and draw the menu screen.
     * It clears the screen, renders the background texture, and draws the UI elements
     * contained in the stage. It also updates and draws the stage to reflect any changes.
     *
     * @param delta The time in seconds since the last frame. Used to update animations or movements.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen

        // Begin the SpriteBatch and draw the background texture
        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getSpriteBatch().end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    /**
     * Called when the window is resized. It updates the viewport and adjusts button scaling
     * and placement to ensure the UI elements remain properly positioned and scaled
     * according to the new window size.
     *
     * @param width  The new width of the window.
     * @param height The new height of the window.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize

        for (var entry : buttons.entrySet()) {
            var button = entry.getValue();
            button.setTransform(true);
            float windowWidth = Gdx.graphics.getWidth();
            float windowHeight = Gdx.graphics.getHeight();
            button.setScale(1); // Math.max(windowWidth/1920, windowHeight/1080)
            table.center();
            for (var cell : iterate(table.getCells())){
                if (cell.getActor() instanceof TextButton){
                    cell.width(300);
                    cell.center();
                }
            }
        }
    }

    /**
     * Called when the screen is disposed of. It disposes of the stage to free up resources.
     */
    @Override
    public void dispose() {
        // Dispose of the stage when screen is disposed
        stage.dispose();
    }

    /**
     * Called when the screen is shown. Sets the input processor so the stage can receive
     * input events, such as button presses.
     */
    @Override
    public void show() {
        // Set the input processor so the stage can receive input events
        Gdx.input.setInputProcessor(stage);
    }

    // The following methods are part of the Screen interface but are not used in this screen.
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
