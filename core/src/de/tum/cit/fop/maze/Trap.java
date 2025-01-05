package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

public class Trap extends Obstacle {
    private float damage;

    public Trap(String name, int worldX, int worldY, boolean collision, float width, float height, Rectangle hitbox, float damage) {
        super(name, worldX, worldY, collision, width, height, hitbox);
        this.damage = 0.5f * damage;// idk we can change the damage as we like ig
    }

    public void damagePlayer(Player player) {
        if(this.getHitbox().overlaps(player.getHitbox())) {
            player.loseLives(damage);
            System.out.println("Be careful!! You hit a trap:O");
        }
    }
}
