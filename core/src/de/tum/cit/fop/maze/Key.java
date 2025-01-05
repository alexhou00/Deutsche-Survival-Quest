package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Key extends GameObject {

    private boolean isCollected;

    public Key(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.isCollected = false;
    }

    public boolean collisionWithPlayer(Player player) {
        if (this.getHitbox().overlaps(player.getHitbox())) {
            return true;
        }
        return false;
    }

    public void collect(Player player) {
        if (!isCollected && this.getHitbox().overlaps(player.getHitbox())) {
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
                    getX(),
                    getY(),
                    getWidthOnScreen(),
                    getHeightOnScreen()
            );
        }
    }
}
