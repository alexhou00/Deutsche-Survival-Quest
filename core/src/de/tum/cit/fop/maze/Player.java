package de.tum.cit.fop.maze;

public class Player extends Character{
    private int lives;
    private boolean hasKey;
    //private boolean hasMoved;


    public Player(int lives) {
        this.lives = lives;
        this.hasKey = false;
        //this.hasMoved = false;
    }


    //getter and setter
    public int getLives() {
        return lives;
    }
    public void setLives(int lives) {
        this.lives = lives;
    }
    public boolean hasKey() {
        return hasKey;
    }
    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }
    /*public boolean hasMoved() {
        return hasMoved;
    }
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }*/






}
