package de.tum.cit.fop.maze;

public abstract class Character {
    private int lives;

    public Character(int lives) {
        this.lives = lives;
    }

    public int getLives() {
        return lives;
    }
    public void setLives(int lives) {
        this.lives = lives;
    }
}
