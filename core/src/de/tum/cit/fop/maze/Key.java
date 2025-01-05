package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

public class Key extends Collectibles {

    public Key(String name, int worldX, int worldY, boolean collision, float width, float height, Rectangle hitbox) {
        super("key", worldX, worldY, collision, width, height, hitbox);

    }
}
