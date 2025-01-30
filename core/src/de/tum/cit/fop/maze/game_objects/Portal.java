package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.base.StaticObject;
import de.tum.cit.fop.maze.level.LevelManager;

import static de.tum.cit.fop.maze.util.Position.getWorldCoordinateInPixels;

/** The third obstacle, rather than static traps & enemies, it must be something ingenious. Use your imagination and experience in video games.*/
public class Portal extends StaticObject {
    private float elapsedTime; // Tracks time for the portal's state
    private boolean isActive; // Indicates if the portal is active
    private final float activeDuration = 5f; // Duration for which the portal is active
    private final float cycleDuration = 20f; // Total duration of a cycle (inactive + active)
    private final LevelManager levels;

    private Player player = null;
    MazeRunnerGame game;

    /**
     * Constructs a new Portal instance with specified parameters.
     *
     * @param x World x-coordinate of the portal's position.
     * @param y World y-coordinate of the portal's position.
     * @param width The width of the portal.
     * @param height The height of the portal.
     * @param hitboxWidth The width of the portal's hitbox.
     * @param hitboxHeight The height of the portal's hitbox.
     * @param widthOnScreen The width of the portal as displayed on screen.
     * @param heightOnScreen The height of the portal as displayed on screen.
     */
    public Portal(LevelManager levels, float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, MazeRunnerGame game) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.elapsedTime = 0;
        this.isActive = false;
        this.levels = levels;
        this.game = game;
    }

    /**
     * Updates the portal's state based on elapsed time.
     *
     * @param deltaTime Time passed since the last frame.
     */
    public void update(float deltaTime) {
        elapsedTime += deltaTime;

        // If elapsedTime exceeds cycleDuration, reset it to keep the portal in a continuous cycle
        if (elapsedTime >= cycleDuration) {
            elapsedTime -= cycleDuration; // Reset to start a new cycle
        }
        // Portal is active for the first 'activeDuration' seconds, then inactive for the rest
        isActive = elapsedTime < activeDuration;
    }

    /**
     * Checks if the portal is currently active.
     *
     * @return True if the portal is active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Handles the player's interaction with the portal when touched.
     *
     * <p>If the portal is currently active, this method teleports the player to
     * the entrance position defined in the levels configuration. If the levels
     * or entrance are not properly initialized, an error message is logged.</p>
     *
     * @param player The player who touches the portal.
     *               The player's position will be updated if the portal is active.
     */
    public void onPlayerTouch(Player player) {
        if (isActive) {
            if (game.isMuted()){
                game.getSoundEffectTeleport().pause();
            }
            else if (!game.isMuted()){
                game.getSoundEffectTeleport().play(game.getSoundManager().getVolume());
            }

            // Teleport the player to the entrance position (assuming levels.entrance is initialized)
            if (levels != null && levels.getEntrance()!= null) {
                player.setX(getWorldCoordinateInPixels(levels.entrance.getTileX()));
                player.setY(getWorldCoordinateInPixels(levels.entrance.getTileY()));
                Gdx.app.log("Portal", "Player teleported to entrance position.");
            } else {
                Gdx.app.log("Portal", "Error: Entrance or LevelManager not initialized.");
            }
        }
    }


    /**
     * Initializes the portal with the specified player, initial state, and elapsed time.
     *
     * <p>This method associates the portal with a player, sets its initial active state,
     * and configures the elapsed time within its activity cycle. It is useful for setting
     * up the portal when it is created or resetting it during gameplay.</p>
     *
     * @param player           The player object that interacts with the portal.
     * @param initialState     The initial active state of the portal. {@code true} if the portal should be active, {@code false} otherwise.
     * @param initialElapsedTime The elapsed time to initialize the portal's activity cycle. This value is constrained within the range of the cycle duration.
     */
    public void init(Player player, boolean initialState, float initialElapsedTime) {
        //initialize and configure the state of a Portal instance.
        this.player = player;
        //Links the Portal instance to a Player object
        //this enables the portal to access the player's properties or perform actions on the player, like teleporting them to an entrance
        this.isActive = initialState;
        this.elapsedTime = initialElapsedTime % cycleDuration; // Ensure elapsedTime stays within the cycle
        Gdx.app.log("Portals", "Portal initialized: isActive=" + isActive + ", elapsedTime=" + elapsedTime);
    }

    /**
     * Renders the game object on the screen using the specified sprite batch and texture frame.
     * The object is only rendered if it is marked as active.
     *
     * @param batch The {@link SpriteBatch} used to draw the texture frame.
     * @param frame The {@link TextureRegion} representing the image to render.
     */
    public void render(SpriteBatch batch, TextureRegion frame){
        if (isActive) {
            batch.draw(frame, getX() - getWidthOnScreen() / 2, getY() - getHeightOnScreen() / 2, getWidthOnScreen(), getHeightOnScreen());
        }
    }

    @Override
    public Rectangle getHitbox() {
        return super.getHitbox();
    }
}
