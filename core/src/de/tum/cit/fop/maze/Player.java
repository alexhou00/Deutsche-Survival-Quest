package de.tum.cit.fop.maze;

public class Player extends Character{
    private boolean hasKey;
    //private boolean hasMoved;


    public Player(int lives, boolean hasKey) {
        super(lives);
        this.hasKey = hasKey;
    }

    //getter and setter
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
