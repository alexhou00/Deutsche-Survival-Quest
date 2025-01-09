package de.tum.cit.fop.maze;

import de.tum.cit.fop.maze.BaseClasses.GameObject;

/**
 * Represents a generic STATIC game object with common properties and behaviors.
 * <p>
 * This class provides a foundation for other game objects
 * such as traps the key. It includes properties
 * like position, size, and hitbox, as well as methods for
 * collision detection and accessing object dimensions.
 */

public abstract class StaticObject extends GameObject {
    /**
     * Constructs a new StaticObject instance with specified parameters.
     *
     * @param x World x-coordinate of the object's initial position (origin is the center of the sprite)
     * @param y World y-coordinate of the object's initial position. (origin is the center of the sprite)
     * @param width The width of the object.
     * @param height The height of the object.
     * @param hitboxWidth The width of the object's hitbox.
     * @param hitboxHeight The height of the object's hitbox.
     * @param widthOnScreen The width of the object as displayed on screen.
     * @param heightOnScreen The height of the object as displayed on screen.
     */
    public StaticObject(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
    }
}


