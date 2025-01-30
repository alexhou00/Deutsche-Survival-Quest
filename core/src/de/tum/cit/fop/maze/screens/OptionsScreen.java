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

import static de.tum.cit.fop.maze.util.Constants.*;

/**
 * Represents the Options screen in the MazeRunner game. This screen allows the user to adjust settings such as
 * music volume, sound effects volume, and mute/unmute options. It also provides navigation back to the main menu.
 */
public class OptionsScreen implements Screen {

    private final Stage stage;
    private final MazeRunnerGame game;
    private final Map<String, TextButton> buttons;
    private final Texture backgroundTexture;
    private final Skin skin;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private GameOverScreen gameOverScreen;
    private OptionsScreen optionsScreen;

    // Sliders for music and sound
    private final Slider musicSlider;
    private final Slider soundSlider;

    /**
     * Constructs the OptionsScreen for the MazeRunner game.
     *
     * @param game The game instance used to control the game state and settings.
     */
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
        buttons.put("Mute", new TextButton("Mute", skin));
        buttons.put("Back", new TextButton("Back", skin));

        // Add header label
        table.add(new Label("Options", skin, "fraktur")).padBottom(80).row();

        // Volume Slider for Music
        musicSlider = new Slider(0f, 1f, 0.05f, false, skin);
        musicSlider.setValue(game.getVolume());
        table.add(new Label("Music Volume", skin, "black")).padBottom(10).row();
        table.add(musicSlider).width(300).padBottom(20).row();

        // Volume Slider for Sound Effects
        soundSlider = new Slider(0f, 1f, 0.05f, false, skin);
        soundSlider.setValue(game.getVolume());
        table.add(new Label("Sound Effects Volume", skin, "black")).padBottom(10).row();
        table.add(soundSlider).width(300).padBottom(20).row();

        // Add buttons (Mute, Back) under the sliders
        buttons.get("Mute").addListener(game.getButtonSoundListener());
        buttons.get("Back").addListener(game.getButtonSoundListener());
        table.add(buttons.get("Mute")).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();
        table.add(buttons.get("Back")).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).row();

        // Add listener to musicSlider
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = musicSlider.getValue();
                game.setVolume(volume);
                Gdx.app.log("OptionsScreen", "Music Volume changed: " + volume);
            }
        });

        // Add listener to soundSlider
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = soundSlider.getValue();
                game.getSoundManager().setVolume(volume);
                Gdx.app.log("OptionsScreen", "Sound Effects Volume changed: " + volume);
            }
        });

        // Mute Button Listener
        buttons.get("Mute").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean isMuted = !game.isMuted();
                game.muteAll(isMuted);
                buttons.get("Mute").setText((isMuted) ? "Unmute" : "Mute");
                Gdx.app.log("OptionsScreen", "Mute toggled");
            }
        });

        // Back Button Listener
        buttons.get("Back").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu(); // Call your method to navigate back to the appropriate screen
            }
        });
    }

    /**
     * Navigates to the options screen, ensuring that the previous screen (if any) is disposed of.
     */
    public void goToOptionsScreen() {
        if (optionsScreen == null) {
            game.setScreen(this); // Set current screen to OptionsScreen
        }

        // Handle screen disposal
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }

        if (gameOverScreen != null) {
            gameOverScreen.dispose();
            gameOverScreen = null;
        }

        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }

    /**
     * Called when the screen is shown. This sets the input processor to the stage.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Renders the options screen, including background and UI elements.
     *
     * @param delta The time elapsed since the last render call.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0);
        game.getSpriteBatch().end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Resizes the viewport to fit the new screen dimensions.
     *
     * @param width  The new width of the screen.
     * @param height The new height of the screen.
     */@Override
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
        stage.dispose();
        backgroundTexture.dispose();
    }
}
