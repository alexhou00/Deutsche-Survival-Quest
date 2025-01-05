package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;

/** static obstacle */
public class Obstacle {
    // TODO: make it extend GameObject
    protected String name;
    protected int worldX, worldY;
    protected boolean collision;
    protected float width, height;
    protected Rectangle hitbox;

    public Obstacle(String name, int worldX, int worldY, boolean collision, float width, float height, Rectangle hitbox) {
        this.name = name;
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = collision;
        this.width = width;
        this.height = height;
        this.hitbox = hitbox;
        
    }
    
    public String getName() {
        return name;
    }
    public int getWorldX() {
        return worldX;
    }
    public int getWorldY() {
        return worldY;
    }
    public boolean isCollision() {
        return collision;
    }
    public float getWidth() {
        return width;
    }
    public float getHeight() {
        return height;
    }
    public Rectangle getHitbox() {
        return hitbox;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }
    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }
    public void setCollision(boolean collision) {
        this.collision = collision;
    }
    public void setWidth(float width) {
        this.width = width;
    }
    public void setHeight(float height) {
        this.height = height;
    }
    public void setHitbox(Rectangle hitbox) {
        this.hitbox = hitbox;
    }

    //collision
    public boolean isCollision(Rectangle playerHitbox) {
        return hitbox.overlaps(playerHitbox);
    }
}
