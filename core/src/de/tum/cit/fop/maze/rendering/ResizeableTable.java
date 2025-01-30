package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * A {@code ResizeableTable} extends the functionality of {@link Table}
 * by adding width and height scaling ratios.
 * This allows the table to be resized proportionally based on the given ratios.
 */
public class ResizeableTable extends Table {
    private final float widthRatio;
    private final float heightRatio;


    /**
     * Constructs a new {@code ResizeableTable} with the specified width and height ratios.
     *
     * @param widthRatio  the ratio by which the width of the table is scaled
     * @param heightRatio the ratio by which the height of the table is scaled
     */
    public ResizeableTable(float widthRatio, float heightRatio) {
        super();
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
    }


    /**
     * Returns the width scaling ratio of the table.
     *
     * @return the width ratio
     */
    public float getWidthRatio() {
        return widthRatio;
    }

    /**
     * Returns the height scaling ratio of the table.
     *
     * @return the height ratio
     */
    public float getHeightRatio() {
        return heightRatio;
    }
}
