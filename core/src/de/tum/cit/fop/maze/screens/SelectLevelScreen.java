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

import static de.tum.cit.fop.maze.util.Constants.TOTAL_LEVELS;

public class SelectLevelScreen implements Screen  {

    private final Stage stage;
    MazeRunnerGame game;
    private final Map<String, TextButton> buttons;
    Texture backgroundTexture;
    MenuScreen menuScreen;
    GameScreen gameScreen;
    GameOverScreen gameOverScreen;

    public SelectLevelScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        camera.update();
        this.show();

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage (viewport, game.getSpriteBatch());

        backgroundTexture = new Texture("backgrounds/background.png");

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


        table.add(new Label("Select Your Level", game.getSkin(), "title")).padBottom(80).row();

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
            table.add(buttons.get("Level " + i)).padTop(5).row();
        }
    }

    public void goToSelectLevelScreen() {
        // this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen
        if (gameScreen == null) {
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

        if (gameScreen != null){
            gameScreen.dispose();
            game.setScreen(this);
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
        game.getSpriteBatch().end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

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
