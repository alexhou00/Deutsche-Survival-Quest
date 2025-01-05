package de.tum.cit.fop.maze;

public class Enemy extends Character {
    /**
     * Constructs a new Enemy instance with specified parameters.
     *
     * @param x              World x-coordinate of the character's initial position (origin is the center of the sprite)
     * @param y              World y-coordinate of the character's initial position. (origin is the center of the sprite)
     * @param width          The width of the character.
     * @param height         The height of the character.
     * @param hitboxWidth    The width of the character's hitbox.
     * @param hitboxHeight   The height of the character's hitbox.
     * @param widthOnScreen  The width of the character as displayed on screen.
     * @param heightOnScreen The height of the character as displayed on screen.
     * @param lives          The number of lives the character starts with.
     */
    public Enemy(int x, int y, int width, int height, int hitboxWidth, int hitboxHeight, float widthOnScreen, float heightOnScreen, float lives) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives);
    }

    @Override
    void update(float delta) {

    }

    @Override
    void pause() {

    }

    @Override
    void resume() {

    }

    @Override
    void hide() {

    }

    @Override
    void dispose() {

    }
}
