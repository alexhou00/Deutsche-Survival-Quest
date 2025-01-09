package de.tum.cit.fop.maze.MapTileObjects;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpeedBoost extends Wall {
    public SpeedBoost(TextureRegion textureRegion) {
        super(textureRegion);
    }

    @Override
    protected void setHitPixmap() {
        Pixmap pixmap = getTilePixmap(this.getTextureRegion());
        hitPixmap = createHitPixmap(this.getTextureRegion(), pixmap);
    }
/*
    public static boolean[][] createHitPixmapForSpeedBoost(TextureRegion textureRegion, Pixmap tilePixmap) {
        int startX = textureRegion.getRegionX();
        int startY = textureRegion.getRegionY();
        int width = textureRegion.getRegionWidth();
        int height = textureRegion.getRegionHeight();

        boolean[][] hitPixmap = new boolean[width][height];

        // Iterate over the pixels in the texture region
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = tilePixmap.getPixel(startX + x, startY + y);
                int alpha = (pixel & 0xFF000000) >>> 24;
                // Extract RGB components
                int red = (pixel & 0x00FF0000) >>> 16;
                int green = (pixel & 0x0000FF00) >>> 8;
                int blue = (pixel & 0x000000FF);
                hitPixmap[x][y] = ((x==0) || (x==width-1) && (red <= 10 && green <= 10 && blue <= 12));
                if (hitPixmap[x][y] ) System.out.println(x + " " + red + " " + green + " " + blue + " " + alpha);
            }
        }

        printHitPixmap(hitPixmap);


        return hitPixmap;
    }*/

    public static boolean[][] createHitPixmap(TextureRegion textureRegion, Pixmap tilePixmap) {
        int startX = textureRegion.getRegionX();
        int startY = textureRegion.getRegionY();
        int width = textureRegion.getRegionWidth();
        int height = textureRegion.getRegionHeight();

        boolean[][] hitPixmap = new boolean[width][height];

        // Iterate over the pixels in the texture region
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = tilePixmap.getPixel(startX + x, startY + y);
                int red = (pixel & 0x00FF0000) >>> 16;
                pixel = tilePixmap.getPixel(startX + x, startY + y);
                int green = (pixel & 0x0000FF00) >>> 8;
                pixel = tilePixmap.getPixel(startX + x, startY + y);
                int blue = (pixel & 0x000000FF);
                pixel = tilePixmap.getPixel(startX + x, startY + y);
                int alpha = (pixel & 0xFF000000) >>> 24; // Extract the alpha channel

                // Collision detected if (alpha > 20) (max. 255)
                hitPixmap[x][y] = ((x<=2) || (x>=width-3)) && (alpha > 20); // (alpha > 20);
                if (hitPixmap[x][y] ) System.out.println(x + " " + y + " " + red + " " + green + " " + blue + " " + alpha);
            }
        }
        printHitPixmap(hitPixmap);
        return hitPixmap;
    }
}
