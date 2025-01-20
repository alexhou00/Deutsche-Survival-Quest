package de.tum.cit.fop.maze.game_objects;

import de.tum.cit.fop.maze.base.StaticObject;

/** The third obstacle, rather than static traps & enemies, it must be something ingenious. Use your imagination and experience in videogames.*/
public class Portal extends StaticObject {
    private float elapsedTime; // Tracks time for the portal's state
    private boolean isActive; // Indicates if the portal is active
    private final float activeDuration = 5f; // Duration for which the portal is active
    private final float cycleDuration = 20f; // Total duration of a cycle (inactive + active)

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
    }

    /**
     * Updates the portal's state based on elapsed time.
     *
     * @param deltaTime Time passed since the last frame.
     */
    public void update(float deltaTime) {
        elapsedTime += deltaTime;

        if (elapsedTime >= cycleDuration) {
            elapsedTime -= cycleDuration; // Reset the cycle
        }

        isActive = elapsedTime >= (cycleDuration - activeDuration);
    }

    /**
     * Checks if the portal is currently active.
     *
     * @return True if the portal is active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /*/**
     * Handles interaction when the player touches the portal.
     *
     * @param player The player interacting with the portal.
     * @param startPosition The position to which the player should be transported.
     */
   /* public void onPlayerTouch(Player player) {
        if (isActive) {
            player.setPosition(startPosition); // Transport player back to the start position
            player.loseKey(); // Remove the key from the player
        }
    }*/
}
