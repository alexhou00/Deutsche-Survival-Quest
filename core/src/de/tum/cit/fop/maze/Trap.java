package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Trap extends GameObject {
    private float damage;
    private TextureRegion trapTexture;

    public Trap(TextureRegion textureRegion, float x, float y, int width, int height, int hitboxWidth, int hitboxHeight,
                float widthOnScreen, float heightOnScreen, float damage) {
        super(x, y, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen);
        this.damage = 0.5f * damage; // Optional multiplier
        this.trapTexture = textureRegion; // new TextureRegion(new Texture(Gdx.files.internal("objects.png")),1,165,31,26); // Path to the trap image texture
    }

    // Render the trap using SpriteBatch
    public void draw(SpriteBatch batch) {
        batch.draw(trapTexture, x - widthOnScreen / 2, y - heightOnScreen / 2, widthOnScreen, heightOnScreen);
    }
/*
    // Dispose the texture to avoid memory leaks
    public void dispose() {
        if (trapTexture != null) {

        }
    }*/

    public float getDamage() {
        return damage;
    }

}
