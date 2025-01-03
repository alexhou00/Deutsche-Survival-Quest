package de.tum.cit.fop.maze;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;

public abstract class Character extends InputAdapter {
    protected float lives;
    protected float x, y, velX, velY, speed;
    protected float width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen;
    float hitboxWidthOnScreen;
    float hitboxHeightOnScreen;
    protected Rectangle rectangle;

    public Character(int x, int y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives) {
        this.lives = lives;
        this.rectangle = new Rectangle();
        this.velX = 0;
        this.velY = 0;
        this.speed = 0;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.widthOnScreen = widthOnScreen;
        this.heightOnScreen = heightOnScreen;
        // Actual size of the non-transparent part shown on the screen
        this.hitboxWidthOnScreen = (float) widthOnScreen * hitboxWidth / width;
        this.hitboxHeightOnScreen = (float) heightOnScreen * hitboxHeight / height;

    }

    abstract void update(float delta);

    abstract void pause();

    abstract void resume();

    abstract void hide();

    abstract void dispose();

    public float getLives() {
        return lives;
    }
    public void setLives(float lives) {
        this.lives = lives;
    }

    public float getX() {
        return x;
    }

    public float getOriginX(){
        return x - widthOnScreen / 2;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public float getOriginY(){
        return y - heightOnScreen / 2;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getHitboxWidth() {
        return hitboxWidth;
    }

    public void setHitboxWidth(float hitboxWidth) {
        this.hitboxWidth = hitboxWidth;
    }

    public float getHitboxHeight() {
        return hitboxHeight;
    }

    public void setHitboxHeight(float hitboxHeight) {
        this.hitboxHeight = hitboxHeight;
    }

    public float getWidthOnScreen() {
        return widthOnScreen;
    }

    public void setWidthOnScreen(float setWidthOnScreen) {
        this.widthOnScreen = setWidthOnScreen;
    }

    public float getHeightOnScreen() {
        return heightOnScreen;
    }

    public void setHeightOnScreen(float heightOnScreen) {
        this.heightOnScreen = heightOnScreen;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }
}
