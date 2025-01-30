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

/**
 * Represents a UI panel in the game.
 * This panel can be used to display game information like stats, scores, etc.
 * It uses a Table to organize UI elements, which are then added to the Stage.
 */
public class Panel extends Actor{
    private final Table table;
    private final Stage stage;
    private float widthRatio;
    private float heightRatio;
    private final MazeRunnerGame game;

    /**
     * Constructs a panel with a background and adds it to the given stage.
     * Sets the default width and height ratios for the panel.
     *
     * @param stage The stage to which the panel will be added
     * @param background The background drawable for the panel
     * @param game The game instance this panel is a part of
     */
    public Panel(Stage stage, Drawable background, MazeRunnerGame game) {

        table = new Table();
        table.setBackground(background);
        this.stage = stage;
        stage.addActor(table);
        this.widthRatio = 0.8f; // default
        this.heightRatio = 0.6f; // default
        this.game = game;
    }

    /**
     * Sets the size of the panel based on the specified width and height ratios.
     * The panel is centered in the middle of the screen.
     *
     * @param widthRatio The width ratio (0 to 1) relative to screen width
     * @param heightRatio The height ratio (0 to 1) relative to screen height
     */
    public void setSize(float widthRatio, float heightRatio) { // 0~1
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;

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

    /**
     * Adds a label to the panel.
     *
     * @param text The text to display on the label
     * @param skin The skin to use for the label
     * @param styleName The style of the label
     * @param scale The scale factor for the font
     * @param padBottom The padding at the bottom of the label
     * @return The created label
     */
    public Label addLabel(String text, Skin skin, String styleName, float scale, float padBottom) {
        Label label = new Label(text, skin, styleName);
        label.getStyle().font.getData().setScale(scale);
        label.setWrap(true);
        label.setWidth(100);
        table.add(label).width(getPanelWidth() * 0.9f).padBottom(padBottom).center().row();
        label.setAlignment(Align.top);
        return label;
    }



    /**
     * Adds a button to the panel with a listener that plays a sound on hover.
     *
     * @param buttonText The text on the button
     * @param skin The skin to use for the button
     * @param listener The listener to handle button click events
     */
    public void addButton(String buttonText, Skin skin, ChangeListener listener) {
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


        table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(BUTTON_PADDING).center().row();
    }

    /**
     * Adds a listener to the stage for input events.
     *
     * @param listener The listener to add to the stage
     */
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


    /**
     * Proceeds to the game and removes the panel from the stage.
     * Sets the player's position at the entrance of the level.
     *
     * @param game The game instance
     * @param player The player instance
     * @param levels The level manager to access the level entrance
     */
    public void proceedToGame(MazeRunnerGame game, Player player, LevelManager levels) {
        this.getTable().remove(); // Remove the panel and start the game
        game.resume();
        player.setPosition(getWorldCoordinateInPixels(levels.entrance.getTileX()),
                getWorldCoordinateInPixels(levels.entrance.getTileY()));
    }

    /**
     * Proceeds to the next level by incrementing the game level and stopping victory sound.
     *
     * @param game The game instance
     */
    public void proceedToNextLevel(MazeRunnerGame game){
        game.setGameLevel(game.getGameLevel() + 1);
        game.getVictorySoundEffect().stop();
        game.startNextLevel();
    }

    /**
     * Adds a slider to the panel with the specified properties.
     *
     * @param labelText The label text for the slider
     * @param minValue The minimum value of the slider
     * @param maxValue The maximum value of the slider
     * @param currentValue The current value of the slider
     * @param stepSize The step size for the slider
     * @param skin The skin to use for the slider
     * @param styleName The style name for the slider
     * @param listener The listener to handle slider changes
     */
    public void addSlider(String labelText, float minValue, float maxValue, float currentValue, float stepSize, Skin skin, String styleName, ChangeListener listener) {
        Label label = new Label(labelText, skin, styleName);
        table.add(label).padBottom(20).center().row();

        Slider slider = new Slider(minValue, maxValue, stepSize, false, skin);
        slider.setValue(currentValue); // Set the current value of the slider
        slider.addListener(listener);

        // Adjust slider size (thinner height, longer width)
        slider.setHeight(5); // Adjust the height of the slider to make it thinner
        slider.setWidth(Gdx.graphics.getWidth() * 0.6f); // Adjust the width of the slider to make it longer

        table.add(slider).padBottom(20).center().row();
    }

    /**
     * Clears all elements from the panel and removes it from the stage.
     */
    public void clear() {
        table.clear();
        table.remove();
    }

    /**
     * Gets the table used to layout the panel's UI elements.
     *
     * @return The table
     */
    public Table getTable() {
        return table;
    }

    public float getWidthRatio() {
        return widthRatio;
    }

    public float getHeightRatio() {
        return heightRatio;
    }


    /**
     * Creates a NinePatchDrawable from a specified image path and patch parameters.
     *
     * @param imageInternalPath The internal path to the image
     * @param left The left padding of the patch
     * @param right The right padding of the patch
     * @param top The top padding of the patch
     * @param bottom The bottom padding of the patch
     * @return The NinePatchDrawable created from the image
     */
    public static NinePatchDrawable getNinePatchDrawableFromPath(FileHandle imageInternalPath, int left, int right, int top, int bottom){
        NinePatch ninePatch = new NinePatch(new TextureRegion(new Texture(imageInternalPath)), left, right, top, bottom);
        return new NinePatchDrawable(ninePatch);
    }

    /**
     * Gets the width of the panel based on the current width ratio.
     *
     * @return The panel's width
     */public float getPanelWidth(){
        Gdx.app.log("Panel", "width: " + widthRatio * Gdx.graphics.getWidth());
        return 0.6f * Gdx.graphics.getWidth();
    }


}