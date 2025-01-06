package de.tum.cit.fop.maze;

import static de.tum.cit.fop.maze.Constants.TILE_SCREEN_SIZE;

public class Position {
    private float x;
    private float y;
    private PositionUnit unit;

    public enum PositionUnit {
        TILES,
        PIXELS
    }

    // Constructor
    public Position(float x, float y, PositionUnit unit) {
        this.x = x;
        this.y = y;
        this.unit = unit;
    }

    // Overloaded constructor defaulting to PIXELS
    public Position(float x, float y) {
        this(x, y, PositionUnit.PIXELS);
    }

    // No-args constructor
    public Position() {
        this(0.0f, 0.0f);
    }

    // Getters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }


    public int getTileX() {
        if (unit == PositionUnit.TILES) {
            return (int) x;
        }
        else{
            throw new IllegalStateException("Position is in PIXELS. Cannot get tile coordinate.");
        }
    }

    public int getTileY() {
        if (unit == PositionUnit.TILES) {
            return (int) y;
        }
        else{
            throw new IllegalStateException("Position is in PIXELS. Cannot get tile coordinate.");
        }
    }

    public PositionUnit getUnit() {
        return unit;
    }

    // Setters
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    // unit should only be changed when converting
    private void setUnit(PositionUnit unit) {
        this.unit = unit;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return x == other.x && y == other.y && unit == other.unit;
    }

    @Override
    public String toString() {
        return String.format("Position(x=%.2f, y=%.2f, unit=%s)", x, y, unit);
    }
}