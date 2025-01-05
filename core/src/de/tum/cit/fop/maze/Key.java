package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Key extends Collectibles {

    private boolean isCollected;

    public Key(String name, int worldX, int worldY, boolean collision, float width, float height, Rectangle hitbox) {
        super("key", worldX, worldY, collision, width, height, hitbox);
        this.isCollected = false;
    }

    public boolean collisionWithPlayer(Player player) {
        if (this.getHitbox().overlaps(player.getHitbox())) {
            return true;
        }
        return false;
    }

    public void collect() {
        //to prevent multiple collections
        if (!isCollected) {
            isCollected = true;
            System.out.println("Key collected!");
        }
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void renderTheKey(SpriteBatch spriteBatch, Texture keyTexture) {
        if (!isCollected) {
            spriteBatch.draw(
                    keyTexture,
                    getWorldX(),
                    getWorldY(),
                    getWidthOnScreen(),
                    getHeightOnScreen()
            );
        }
    }


}
