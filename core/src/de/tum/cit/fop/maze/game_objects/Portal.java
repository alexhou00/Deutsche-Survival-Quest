package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
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
    private Tiles tiles;

    private Player player = null;

    public GameScreen gameScreen;


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
    public Portal(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.elapsedTime = 0;
        this.isActive = false;
        this.tiles = new Tiles();

        // Load the tiled map
        /*String mapFilePath = "path/to/your/map.properties";
        String tileSheetPath = "path/to/your/tileset.png";
        String obstacleSheetPath = "path/to/your/obstacle_sheet.png";

        TiledMap tiledMap = tiles.loadTiledMap(mapFilePath, tileSheetPath, obstacleSheetPath);*/



        // Example of how you might use the tiles in the portal
        /*Position playerPosition = player.getPosition();
        TileType tileTypeAtPlayer = tiles.getTileEnumOnMap(playerPosition.getTileX(), playerPosition.getTileY());

        if (tileTypeAtPlayer == TileType.ENTRANCE) {
            System.out.println("Player is at the entrance.");
        } else if (tileTypeAtPlayer == TileType.EXIT) {
            System.out.println("Player is at the exit.");
        }*/

    }
    public void init(Player player){
        this.player = player;
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

        onPlayerTouch(player);
    }

    /**
     * Checks if the portal is currently active.
     *
     * @return True if the portal is active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    public void onPlayerTouch(Player player) {
        if (isActive) {
            // Teleport the player to the entrance position (assuming tiles.entrance is initialized)
            if (tiles != null && tiles.getEntrance()!= null) {
//                player.setPosition(tiles.getEntrance().getTileX(), tiles.getEntrance().getTileY());
                player.setX(getWorldCoordinateInPixels(tiles.entrance.getTileX()));
                player.setY(getWorldCoordinateInPixels(tiles.entrance.getTileY()));
                System.out.println("Player teleported to entrance position.");
            } else {
                System.out.println("Error: Entrance or Tiles not initialized.");
            }
        }
    }

    public void init(boolean initialState, float initialElapsedTime) {
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
