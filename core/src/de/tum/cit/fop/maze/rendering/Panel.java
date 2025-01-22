package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.game_objects.Player;
import de.tum.cit.fop.maze.level.Tiles;

import static de.tum.cit.fop.maze.util.Position.getWorldCoordinateInPixels;

public class Panel {
    private final Table table;
    private final Stage stage;

    public Panel(Stage stage, Drawable background) {

        table = new Table();
        table.setBackground(background);
        this.stage = stage;
        stage.addActor(table);
    }

    public void setSize(float widthRatio, float heightRatio) { // 0~1
        // Set in the middle, "ratio" is the ratio of the length to the entire window
        table.setSize(Gdx.graphics.getWidth() * widthRatio, Gdx.graphics.getHeight() * heightRatio);
        table.setPosition(Gdx.graphics.getWidth() * (1-widthRatio)/2, Gdx.graphics.getHeight() * (1-heightRatio)/2); // left-bottom corner
    }

    public void addLabel(String text, Label.LabelStyle style, float padBottom) {
        Label label = new Label(text, style);
        table.add(label).padBottom(padBottom).center().row();
    }

    public void addLabel(String text, Skin skin, float scale, float padBottom) {
        Label label = new Label(text, skin);
        label.getStyle().font.getData().setScale(scale);
        table.add(label).padBottom(padBottom).center().row();
    }

    public void addLabel(String text, Skin skin, String styleName, float scale, float padBottom) {
        Label label = new Label(text, skin, styleName);
        label.getStyle().font.getData().setScale(scale);
        table.add(label).padBottom(padBottom).center().row();
    }

    public void addButton(String buttonText, Skin skin, ChangeListener listener, float padBottom) {
        skin.get(Label.LabelStyle.class).font.getData().setScale(1); // set the scale back
        TextButton button = new TextButton(buttonText, skin);
        button.addListener(listener);

        button.addListener(new ClickListener() {
            boolean playing = false;
            long lastPlayTime = 0; // Store the last play time
            final long cooldown = 350; // Cooldown in milliseconds

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                long currentTime = System.currentTimeMillis();
                if (!playing && (currentTime - lastPlayTime > cooldown)) {
                    Sound sound = Gdx.audio.newSound(Gdx.files.internal("sounds/click-button-131479.mp3"));
                    sound.play(1F);
                    playing = true;
                    lastPlayTime = currentTime;
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                playing = false; // Reset the playing flag when exiting
            }
        });


        table.add(button).padBottom(padBottom).center().row();
    }

    public void addListener(InputListener listener) {
        stage.addListener(listener);
    }

    public void proceedToNextScreen(MazeRunnerGame game, Player player, Tiles tiles) {
        this.getTable().remove(); // Remove the panel and start the game
        game.resume();
        player.setPosition(getWorldCoordinateInPixels(tiles.entrance.getTileX()),
                getWorldCoordinateInPixels(tiles.entrance.getTileY()));
    }

    public void clear() {
        table.clear();
        table.remove();
    }

    public Table getTable() {
        return table;
    }
}