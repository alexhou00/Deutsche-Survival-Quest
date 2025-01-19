package de.tum.cit.fop.maze.util;

import static de.tum.cit.fop.maze.util.Constants.TILE_SCREEN_SIZE;

/**
 * Represents a position in the game world, with support for both tile and pixel units.
 */
public class Position {
    private float x;
    private float y;
    private PositionUnit unit;

    public enum PositionUnit {
        TILES,
        PIXELS
    }

    /**
     * Constructs a new Position with the specified coordinates and unit.
     *
     * @param x    The x-coordinate.
     * @param y    The y-coordinate.
     * @param unit The unit of the coordinates (TILES or PIXELS).
     */
    public Position(float x, float y, PositionUnit unit) {
        this.x = x;
        this.y = y;
        this.unit = unit;
    }

    /**
     * Constructs a new Position with the specified coordinates
     * overloading the constructor that create the position in PIXELS by default.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    // Overloaded constructor defaulting to PIXELS
    public Position(float x, float y) {
        this(x, y, PositionUnit.PIXELS);
    }

    /**
     * Constructs a new Position at (0, 0) in PIXELS.
     */
    // No-args constructor
    public Position() {
        this(0.0f, 0.0f);
    }

    // Getters
    /**
     * Returns the x-coordinate of the position.
     *
     * @return The x-coordinate.
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the position.
     *
     * @return The y-coordinate.
     */
    public float getY() {
        return y;
    }


    /**
     * Returns the x-coordinate in tile units.
     *
     * @return The x-coordinate as a tile index, and it must be an integer.
     * @throws IllegalStateException If the position is in PIXELS.
     */
    public int getTileX() {
        if (unit == PositionUnit.TILES) {
            return (int) x;
        }
        else{
            throw new IllegalStateException("Position is in PIXELS. Cannot get tile coordinate.");
        }
    }

    /**
     * Returns the y-coordinate in tile units.
     *
     * @return The y-coordinate as a tile index, and it must be an integer.
     * @throws IllegalStateException If the position is in PIXELS.
     */
    public int getTileY() {
        if (unit == PositionUnit.TILES) {
            return (int) y;
        }
        else{
            throw new IllegalStateException("Position is in PIXELS. Cannot get tile coordinate.");
        }
    }

    /**
     * Returns the unit of the position.
     *
     * @return The unit of the position.
     */
    public PositionUnit getUnit() {
        return unit;
    }

    // Setters
    /**
     * Sets the x-coordinate of the position.
     *
     * @param x The new x-coordinate.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the position.
     *
     * @param y The new y-coordinate.
     */
    public void setY(float y) {
        this.y = y;
    }

    // unit should only be changed when converting
    private void setUnit(PositionUnit unit) {
        this.unit = unit;
    }

    /**
     * Converts the position to the specified unit.
     *
     * @param targetUnit The target unit to convert to.
     * @return A new Position object with the converted coordinates.
     */
    // Helper method to convert between units
    public Position convertTo(PositionUnit targetUnit) {

        float newX = this.x, newY = this.y;

        if (this.unit.equals(targetUnit)) {
            return new Position(newX, newY, this.unit);
        }

        // tile position to world coordinates in pixels
        else if (this.unit.equals(PositionUnit.TILES) && targetUnit.equals(PositionUnit.PIXELS)) {
            newX = (this.x + 0.5f) * TILE_SCREEN_SIZE;
            newY = (this.y + 0.5f) * TILE_SCREEN_SIZE;
        }

        // world coordinates in pixels to tile position
        else if (this.unit.equals(PositionUnit.PIXELS) && targetUnit.equals(PositionUnit.TILES)) {
            newX = this.x / TILE_SCREEN_SIZE;
            newY = this.y / TILE_SCREEN_SIZE;
        }

        return new Position(newX, newY, targetUnit);
    }

    /**
     * Converts a tile coordinate to a world coordinate in pixels.
     *
     * @param tileCoordinate The tile coordinate.
     * @return The corresponding world coordinate in pixels.
     */
    public static float getWorldCoordinateInPixels(int tileCoordinate){
        return (tileCoordinate + 0.5f) * TILE_SCREEN_SIZE;
    }

    /**
     * Checks if this position is equal to another object.
     *
     * @param obj The object to compare to.
     * @return {@code true} if the positions are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position other)) return false;
        return x == other.x && y == other.y && unit == other.unit;
    }

    /**
     * Returns a string representation of the position.
     *
     * @return A string representing the position.
     */
    @Override
    public String toString() {
        return String.format("Position(x=%.2f, y=%.2f, unit=%s)", x, y, unit);
    }
}