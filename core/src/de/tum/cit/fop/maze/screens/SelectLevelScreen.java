package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
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
import java.util.Map;

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

        // used to detect mouse scrolls

        table.add(new Label("Deutsche Survival Quest", game.getSkin())).padBottom(80).row();
//        TextButton Level1 = new TextButton("Level 1", game.getSkin());
//        table.add(Level1).width(300).row();
//        TextButton Level2 = new TextButton("Level 2", game.getSkin());
//        table.add(Level2).width(300).row();
//        TextButton Level3 = new TextButton("Level 3", game.getSkin());
//        table.add(Level3).width(300).row();
//        TextButton Level4 = new TextButton("Level 4", game.getSkin());
//        table.add(Level4).width(300).row();
//        TextButton Level5 = new TextButton("Level 5", game.getSkin());
//        table.add(Level5).width(300).row();
//        TextButton Level6 = new TextButton("Level 6", game.getSkin());
//        table.add(Level6).width(300).row();
//        Level1.addListener(new ChangeListener() {
//            public void changed(ChangeEvent event, Actor actor) {
//                System.out.println("button is pressed");
//                game.setGameLevel(1);
//                game.setScreen(new GameScreen(game));
//            }
//        });
//        Level2.addListener(new ChangeListener() {
//            public void changed(ChangeEvent event, Actor actor) {
//                System.out.println("button is pressed");
//                game.setGameLevel(2);
//                game.setScreen(new GameScreen(game));
//            }
//        });
//        Level3.addListener(new ChangeListener() {
//            public void changed(ChangeEvent event, Actor actor) {
//                System.out.println("button is pressed");
//                game.setGameLevel(3);
//                game.setScreen(new GameScreen(game));
//            }
//        });
buttons = new HashMap<String, TextButton>();
buttons.put("Level 1", new TextButton("Level 1", game.getSkin()));
buttons.put("Level 2", new TextButton("Level 2", game.getSkin()));
buttons.put("Level 3", new TextButton("Level 3", game.getSkin()));
buttons.put("Level 4", new TextButton("Level 4", game.getSkin()));
buttons.put("Level 5", new TextButton("Level 5", game.getSkin()));
buttons.put("Level 6", new TextButton("Level 6", game.getSkin()));

Gdx.app.log("SelectLevelScreen", "screen created ");
 for (Map.Entry<String, TextButton> entry : buttons.entrySet()) {
     TextButton button = entry.getValue();
     table.add(entry.getValue()).padBottom(80).row();
 }

 buttons.get("Level 1").addListener(new ChangeListener() {
     public void changed(ChangeEvent event, Actor actor) {
         Gdx.app.log("SelectLevelScreen", "changed ");
         game.setGameLevel(1);
         game.goToGame();
     }
 });
 buttons.get("Level 2").addListener(new ChangeListener() {
     public void changed(ChangeEvent event, Actor actor) {
         Gdx.app.log("SelectLevelScreen", "changed ");
        game.setGameLevel(2);
         game.goToGame();


     }
 });
 buttons.get("Level 3").addListener(new ChangeListener() {
     public void changed(ChangeEvent event, Actor actor) {
         Gdx.app.log("SelectLevelScreen", "changed ");
         game.setGameLevel(3);
         game.goToGame();
     }
 });
 buttons.get("Level 4").addListener(new ChangeListener() {
     public void changed(ChangeEvent event, Actor actor) {
         Gdx.app.log("SelectLevelScreen", "changed ");
         game.setGameLevel(4);
         game.goToGame();
     }
 });
 buttons.get("Level 5").addListener(new ChangeListener() {
     public void changed(ChangeEvent event, Actor actor) {
         Gdx.app.log("SelectLevelScreen", "changed ");
         game.setGameLevel(5);
         game.goToGame();
     }
 });
 buttons.get("Level 6").addListener(new ChangeListener() {
     public void changed(ChangeEvent event, Actor actor) {
         Gdx.app.log("SelectLevelScreen", "changed ");
         game.setGameLevel(6);
         game.goToGame();
     }
 });
 table.add(buttons.get("Level 1")).padTop(5).row();
 table.add(buttons.get("Level 2")).padTop(5).row();
 table.add(buttons.get("Level 3")).padTop(5).row();
 table.add(buttons.get("Level 4")).padTop(5).row();
 table.add(buttons.get("Level 5")).padTop(5).row();
 table.add(buttons.get("Level 6")).padTop(5).row();
 table.padTop(-800);




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
