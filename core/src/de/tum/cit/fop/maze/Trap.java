package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

public class Trap extends GameObject {
    private float damage;

    public Trap(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float damage) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.damage = 0.5f * damage;
    }

    public void damagePlayer(Player player) {
        if(this.getHitbox().overlaps(player.getHitbox())) {
            player.loseLives(damage);
            System.out.println("Be careful!! You hit a trap:O");
        }
    }
}
