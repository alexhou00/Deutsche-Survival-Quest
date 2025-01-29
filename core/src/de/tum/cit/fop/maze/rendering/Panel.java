package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.game_objects.Player;
import de.tum.cit.fop.maze.level.LevelManager;

import static de.tum.cit.fop.maze.util.Position.getWorldCoordinateInPixels;

public class Panel extends Actor{
    private final Table table;
    private final Stage stage;
    private float widthRatio;
    private float heightRatio;
    private final MazeRunnerGame game;

    public Panel(Stage stage, Drawable background, MazeRunnerGame game) {

        table = new Table();
        table.setBackground(background);
        this.stage = stage;
        stage.addActor(table);
        this.widthRatio = 0.8f; // default
        this.heightRatio = 0.6f; // default
        this.game = game;
    }

    public void setSize(float widthRatio, float heightRatio) { // 0~1
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
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
       label.setAlignment(Align.top);
    }

    public void addLabel (Label label, Float padBottom, Panel panel){
        table.add(label).padBottom(padBottom).center().row();
        label.setAlignment(Align.center);

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
                if (!playing && (currentTime - lastPlayTime > cooldown) && !game.isMuted()) {
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


    public static InputListener ifSpaceKeyPressed(Runnable action) {
        return new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (event.getKeyCode() == Input.Keys.SPACE) {
                    action.run();
                    return true;
                }
                return false;
            }
        };
    }


    public void proceedToGame(MazeRunnerGame game, Player player, LevelManager levels) {
        this.getTable().remove(); // Remove the panel and start the game
        game.resume();
        player.setPosition(getWorldCoordinateInPixels(levels.entrance.getTileX()),
                getWorldCoordinateInPixels(levels.entrance.getTileY()));
    }

    public void proceedToNextLevel(MazeRunnerGame game){
        game.setGameLevel(game.getGameLevel() + 1);
        game.getVictorySoundEffect().stop();
        game.startNextLevel();
    }

    public void addVolumeControl(Skin skin, ChangeListener sliderListener, ChangeListener muteListener) {
        Label volumeLabel = new Label("Volume", skin);
        table.add(volumeLabel).padBottom(20).center().row();

        // Slider for volume
        Slider volumeSlider = new Slider(0, 2, 0.01f, false, skin);
        volumeSlider.setValue(1); // Default volume
        volumeSlider.addListener(sliderListener);
        table.add(volumeSlider).padBottom(20).center().row();

        // Mute checkbox
        CheckBox muteCheckbox = new CheckBox("Mute-Unmute", skin);
        muteCheckbox.addListener(muteListener);
        table.add(muteCheckbox).padBottom(20).center().row();
    }

    public void addSlider(String labelText, float minValue, float maxValue, float currentValue, float stepSize, Skin skin, ChangeListener listener) {
        Label label = new Label(labelText, skin);
        table.add(label).padBottom(20).center().row();

        Slider slider = new Slider(minValue, maxValue, stepSize, false, skin);
        slider.setValue(currentValue); // Set the current value of the slider
        slider.addListener(listener);

        // Adjust slider size (thinner height, longer width)
        slider.setHeight(5); // Adjust the height of the slider to make it thinner
        slider.setWidth(Gdx.graphics.getWidth() * 0.6f); // Adjust the width of the slider to make it longer

        table.add(slider).padBottom(20).center().row();
    }

    public void clear() {
        table.clear();
        table.remove();
    }

    public Table getTable() {
        return table;
    }

    public float getWidthRatio() {
        return widthRatio;
    }

    public float getHeightRatio() {
        return heightRatio;
    }

    public void addSlider(Skin skin, ChangeListener listener, float minValue, float maxValue, float currentValue) {
        Slider slider = new Slider(minValue, maxValue, 0.01f, false, skin);
        slider.setValue(currentValue);
        slider.addListener(listener);
        table.add(slider).padBottom(20).center().row();
    }

    public void addSlider(String soundEffectsVolume, int i, int i1, float v, float volume, ChangeListener optionsScreen, int i2) {
    }

    public static NinePatchDrawable getNinePatchDrawableFromPath(FileHandle imageInternalPath, int left, int right, int top, int bottom){
        NinePatch ninePatch = new NinePatch(new TextureRegion(new Texture(imageInternalPath)), left, right, top, bottom);
        return new NinePatchDrawable(ninePatch);
    }
}