package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

/** The third obstacle, rather than static traps & enemies, it must be something ingenious. Use your imagination and experience in videogames.*/
public class ElementOfSurprise extends Obstacle{

    public ElementOfSurprise(String name, int worldX, int worldY, boolean collision, float width, float height, Rectangle hitbox) {
        super(name, worldX, worldY, collision, width, height, hitbox);
    }
}
