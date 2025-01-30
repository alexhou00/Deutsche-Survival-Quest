package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class ResizeableTable extends Table {
    private final float widthRatio;
    private final float heightRatio;
    public ResizeableTable(float widthRatio, float heightRatio) {
        super();
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
    }

    public float getWidthRatio() {
        return widthRatio;
    }

    public float getHeightRatio() {
        return heightRatio;
    }
}
