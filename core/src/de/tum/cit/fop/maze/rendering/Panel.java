package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.getWorldCoordinateInPixels;

public class Panel extends Actor{
    private final Table table;
    private final Stage stage;
    private final float widthRatio;
    private final float heightRatio;
    private final MazeRunnerGame game;

    public Panel(Stage stage, Drawable background, MazeRunnerGame game, float widthRatio, float heightRatio) {
        table = new ResizeableTable(widthRatio, heightRatio);
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        table.setBackground(background);
        this.stage = stage;
        stage.addActor(table);
        this.game = game;

        table.setPosition(Gdx.graphics.getWidth() * (1-widthRatio)/2, Gdx.graphics.getHeight() * (1-heightRatio)/2);
    }

    public void init() { // 0~1
        float newWidth = Gdx.graphics.getWidth() * widthRatio;
        float newHeight = Gdx.graphics.getHeight() * heightRatio;


        // Set in the middle, "ratio" is the ratio of the length to the entire window
        table.setSize(newWidth, newHeight);
        table.setPosition(Gdx.graphics.getWidth() * (1-widthRatio)/2, Gdx.graphics.getHeight() * (1-heightRatio)/2); // left-bottom corner

        // Update label widths to match new panel width
        for (Cell<?> cell : iterate(table.getCells())) {
            Actor actor = cell.getActor();
            if (actor instanceof Label) {
                cell.width(newWidth * 0.9f); // Adjust width dynamically
            }
        }

        table.invalidate(); // Force table layout update

    }

    public Label addLabel(String text, Skin skin, String styleName, float scale, float padBottom) {
        if (styleName.equals("fraktur"))
            text = text.replaceAll("s$", "\\$"); //replace an "s" at the end of a word with "$" (to show the round S in Fraktur
        Label label = new Label(text, skin, styleName);
        label.getStyle().font.getData().setScale(scale);
        label.setWrap(true);
        label.setWidth(100);
        table.add(label).width(getPanelWidth() * 0.9f).padBottom(padBottom).center().row();
        label.setAlignment(Align.top);
        return label;
    }



    public void addButton(String buttonText, Skin skin, ChangeListener listener) {
        skin.get(Label.LabelStyle.class).font.getData().setScale(1); // set the scale back
        TextButton button = new TextButton(buttonText, skin);
        button.addListener(listener);

        button.addListener(game.getButtonSoundListener());

        table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).center().row();
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

    public static InputListener ifSpaceKeyPressedAndReleased(Runnable action) {
        return new InputListener() {
            private boolean isPressed = false;

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    isPressed = true; // Mark as pressed
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE && isPressed) {
                    isPressed = false; // Reset the flag
                    action.run(); // Run the action only after release
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

    public void addSlider(String labelText, float minValue, float maxValue, float currentValue, float stepSize, Skin skin, String styleName, ChangeListener listener) {
        Label label = new Label(labelText, skin, styleName);
        table.add(label).padBottom(20).center().row();

        Slider slider = new Slider(minValue, maxValue, stepSize, false, skin);
        slider.setValue(currentValue); // Set the current value of the slider
        slider.addListener(listener);

        // Adjust slider size (thinner height, longer width)
        slider.setHeight(5); // Adjust the height of the slider to make it thinner
        slider.setWidth(Gdx.graphics.getWidth() * 0.6f); // Adjust the width of the slider to make it longer

        table.add(slider).width(300).padBottom(20).center().row();
    }

    public void clear() {
        table.clear();
        table.remove();
    }

    public Table getTable() {
        return table;
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

    public float getPanelWidth(){
        return table.getWidth() * Gdx.graphics.getWidth();
    }

    /**
     * Calculates the width of a single letter using the specified font.
     *
     * @param font   The font to use for calculating the width.
     * @param letter The letter whose width is to be calculated.
     * @return The width of the letter.
     */
    public float getLetterWidth(BitmapFont font, String letter){
        GlyphLayout layout = new GlyphLayout(font, letter);
        float adjust = switch (letter.charAt(0)){
            default -> 0;
        };
        return Math.max(layout.width + adjust, layout.width); // width of letter "m"
    }

}