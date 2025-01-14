/*package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.Actor;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.screens.GameScreen;

public class PausePanel {
    private final Table table;  // The main container for the panel
    private final Stage stage; // The stage where this panel is added
    private final MazeRunnerGame game; // Reference to the game instance
    private final TextButton resumeButton;
    //private final TextButton levelsButton;
    private final TextButton exitButton;
    private final GameScreen gameScreen;

    public PausePanel(Stage stage, MazeRunnerGame game, GameScreen gameScreen) {
        this.stage = stage;
        this.game = game;
        this.gameScreen = gameScreen;

        // Create the table and set its layout
        table = new Table();
        table.setFillParent(true);
        table.setBackground(createSolidColorDrawable(Color.DARK_GRAY));

        // Add a title
        Label title = new Label("Paused", game.getSkin(), "title");
        title.setAlignment(Align.center);
        title.setFontScale(1.5f);
        table.add(title).padBottom(20).row();

        // Add resume button
        resumeButton = new TextButton("Resume Game", game.getSkin());
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                gameScreen.resumeGame();
            }
        });
        table.add(resumeButton).width(200).padBottom(10).row();

        // Add levels button
        /*levelsButton = new TextButton("Levels", game.getSkin());
        levelsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameScreen.goToLevelSelection();
            }
        });
        table.add(levelsButton).width(200).padBottom(10).row();*/

        // Add exit button
        /*exitButton = new TextButton("Exit Game", game.getSkin());
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();;
            }
        });
        table.add(exitButton).width(200).padBottom(10).row();

        // Initially hide the table
        table.setVisible(false);

        // Add the table to the stage
        stage.addActor(table);
    }

    // Show the pause panel
    public void show() {
        table.setVisible(true);
        Gdx.input.setInputProcessor(stage); // Set the stage for input handling
    }

    // Hide the pause panel
    public void hide() {
        table.setVisible(false);
        gameScreen.resumeInputProcessor(); // Reset input processor to the game stage
    }

    private Drawable createSolidColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return new Image(new Texture(pixmap)).getDrawable();
    }
}*/
