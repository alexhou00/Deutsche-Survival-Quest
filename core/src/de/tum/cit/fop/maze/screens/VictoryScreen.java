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

public class VictoryScreen implements Screen {
    private final Stage stage;
    private final MazeRunnerGame game;
    //Texture backgroundTexture;

    public VictoryScreen(MazeRunnerGame game) {
        this.game = game;


        var camera = new OrthographicCamera();
        camera.update();
        this.show();
        Viewport viewport = new ScreenViewport(camera);
        this.stage = new Stage (viewport, game.getSpriteBatch());
        //this.backgroundTexture = new Texture("background.png");

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("YOU WON!", game.getSkin(), "fraktur");
        table.add(title).padBottom(80).row();

        Map<String, TextButton> buttons = new LinkedHashMap<>();
        buttons.put("goToMenuButton", new TextButton("Menu", game.getSkin()));
        buttons.put("exitGameButton", new TextButton("Exit Game", game.getSkin()));

        Gdx.app.log("VictoryScreen","VictoryScreen is created.");

        final float BUTTON_WIDTH = 300f;
        final float BUTTON_PADDING = 10f;

        for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
            TextButton button = entry.getValue();
            table.add(button).width(BUTTON_WIDTH).padBottom(BUTTON_PADDING).row();
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


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void render(float v) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.getSpriteBatch().begin();
        game.getSpriteBatch().end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

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
