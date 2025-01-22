package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.base.GameObject;
import de.tum.cit.fop.maze.base.StaticObject;
import de.tum.cit.fop.maze.level.Tiles;
import de.tum.cit.fop.maze.screens.GameScreen;
import de.tum.cit.fop.maze.tiles.Entrance;

import java.awt.*;

import static de.tum.cit.fop.maze.util.Constants.TILE_SCREEN_SIZE;
import static de.tum.cit.fop.maze.util.Position.getWorldCoordinateInPixels;

/** The third obstacle, rather than static traps & enemies, it must be something ingenious. Use your imagination and experience in videogames.*/
public class Portal extends StaticObject {
    private float elapsedTime; // Tracks time for the portal's state
    private boolean isActive; // Indicates if the portal is active
    private final float activeDuration = 5f; // Duration for which the portal is active
    private final float cycleDuration = 20f; // Total duration of a cycle (inactive + active)
    private final Tiles tiles;

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
    public Portal(Tiles tiles, float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, MazeRunnerGame game) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.elapsedTime = 0;
        this.isActive = false;
        this.tiles = tiles;
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
     * the entrance position defined in the tiles configuration. If the tiles
     * or entrance are not properly initialized, an error message is logged.</p>
     *
     * @param player The player who touches the portal.
     *               The player's position will be updated if the portal is active.
     */
    public void onPlayerTouch(Player player) {
        if (isActive) {
            game.getSoundEffectTeleport().play();
            // Teleport the player to the entrance position (assuming tiles.entrance is initialized)
            if (tiles != null && tiles.getEntrance()!= null) {
                player.setX(getWorldCoordinateInPixels(tiles.entrance.getTileX()));
                player.setY(getWorldCoordinateInPixels(tiles.entrance.getTileY()));
                Gdx.app.log("Portal", "Player teleported to entrance position.");
            } else {
                Gdx.app.log("Portal", "Error: Entrance or Tiles not initialized.");
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
        System.out.println("Portal initialized: isActive=" + isActive + ", elapsedTime=" + elapsedTime);
    }

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
