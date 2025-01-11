package de.tum.cit.fop.maze.base;

import de.tum.cit.fop.maze.rendering.SpeechBubble;
import de.tum.cit.fop.maze.screens.GameScreen;

/**
 * Represents a DYNAMIC character in the maze game, such as our player or any moving enemy. <br>
 * This class provides basic properties and behaviors for characters,
 * such as position, velocity, dimensions, and lifecycle methods.
 * It is intended to be extended by specific character.
 */
public abstract class Character extends GameObject {
    protected float lives;
    /** velX and velY stand for velocity X and velocity Y, resp.
     * velocities are horizontal/vertical components of the speed vector
     * speed is the overall magnitude of speed
     * */
    protected float velX, velY, speed;

    private final SpeechBubble speechBubble;

    protected GameScreen gameScreen;

    protected boolean paused;


    /**
     * Constructs a new Character instance with specified parameters.
     *
     * @param x World x-coordinate of the character's initial position (origin is the center of the sprite)
     * @param y World y-coordinate of the character's initial position. (origin is the center of the sprite)
     * @param width The width of the character.
     * @param height The height of the character.
     * @param hitboxWidth The width of the character's hitbox.
     * @param hitboxHeight The height of the character's hitbox.
     * @param widthOnScreen The width of the character as displayed on screen.
     * @param heightOnScreen The height of the character as displayed on screen.
     * @param lives The number of lives the character starts with.
     */
    public Character(float x, float y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives, GameScreen gameScreen) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.lives = lives;
        this.velX = 0;
        this.velY = 0;
        this.speed = 0;
        this.gameScreen = gameScreen;
        this.speechBubble = new SpeechBubble();
    }

    public void update(float delta){
        //speechBubble.update(delta);
    }

    public void pause(){
        paused = true;
    }

    public void resume(){
        paused = false;
    }

    public abstract void hide();

    public abstract void dispose();

    public float getLives() {
        return lives;
    }
    public void setLives(float lives) {
        this.lives = lives;
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

    // Normal Speech Bubble
    public void say(String text) {
        speechBubble.render(gameScreen.game.getSpriteBatch(), text, x, y, getHitboxHeightOnScreen() / 2, SpeechBubble.BubbleType.NORMAL);
    }

    // Normal Speech Bubble
    public void say(String text, boolean typewriterEffect, float timer, float interval) {
        if (typewriterEffect){
            say(text.substring(0, Math.min((int) ((timer/interval)), text.length())));
        }
        else say(text);
    }

    // Multi-edged Speech Bubble for a message out loud
    public void scream(String text) {
        speechBubble.render(gameScreen.game.getSpriteBatch(), text, x, y, getHitboxHeightOnScreen() / 2, SpeechBubble.BubbleType.SCREAM);
    }

    // Cloud-shaped Speech Bubble for thoughts
    public void think(String text) {
        speechBubble.render(gameScreen.game.getSpriteBatch(), text, x, y, getHitboxHeightOnScreen() / 2, SpeechBubble.BubbleType.THOUGHT);
    }
}
