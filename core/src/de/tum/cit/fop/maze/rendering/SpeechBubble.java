package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import static de.tum.cit.fop.maze.util.Constants.getWorldWidth;

/**
 * A class to represent a speech bubble with customizable text, appearance, and duration.
 */
public class SpeechBubble {
    private final Texture texture;
    private final TextureRegion speechTailRegion;
    private final TextureRegion screamTailRegion;
    private final TextureRegion thoughtTailRegion;

    private final TextureRegion[] speechTailRegions;
    private final TextureRegion[][] speechCorners;

    private final float scale;
    private final float[] letterWidth;

    private final BitmapFont font;

    private boolean visible; // Flag to control visibility
    private float visibleDuration; // Duration to show the bubble
    private float elapsedTime; // Tracks the elapsed time

    private static final int SMALL_LETTER_AE = 228;
    private static final int SMALL_LETTER_OE = 246;
    private static final int SMALL_LETTER_UE = 252;
    private static final int SMALL_LETTER_SS = 223;
    private static final int CAPITAL_LETTER_AE = 196;
    private static final int CAPITAL_LETTER_OE = 214;
    private static final int CAPITAL_LETTER_UE = 220;

    /**
     * Constructs a SpeechBubble with the necessary textures, font, and configurations.
     */
    public SpeechBubble() {
        this.texture = new Texture(Gdx.files.internal("original/objects.png"));
        this.speechTailRegion = new TextureRegion(texture, 192, 224, 16, 24);
        this.screamTailRegion = new TextureRegion(texture, 192 + 16, 224, 16, 24);
        this.thoughtTailRegion = new TextureRegion(texture, 192 + 16 * 2, 224, 16, 24);

        this.speechTailRegions = new TextureRegion[3];
        for (int i=0;i<3;i++){
            speechTailRegions[i] = new TextureRegion(texture, 192 + 16 * i, 224, 16, 24);
        }
        this.speechCorners = new TextureRegion[3][3];
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                speechCorners[i][j] = new TextureRegion(texture, 148+13*j,228+13*i,14,14);
            }
        }

        this.scale = 3f;

        this.font = new BitmapFont();
        this.font.setColor(Color.BLACK); // Set font color to match the speech bubble style
        font.getData().setScale(scale/2);

        this.letterWidth = new float[256]; // extended-ascii to include letters like ä, ö, ü
        for (int i = ' '; i<='ü'; i++){
            letterWidth[i] = getLetterWidth(font, Character.toString((char) i));
        }
    }

    /**
     * Renders a speech bubble with the specified text, position, and type.
     *
     * @param batch   The SpriteBatch used for rendering.
     * @param text    The text to display inside the speech bubble.
     * @param x       The x-coordinate for the bubble.
     * @param y       The y-coordinate for the bubble.
     * @param yOffset The vertical offset for positioning the bubble.
     * @param type    The type of the speech bubble (NORMAL, SCREAM, THOUGHT).
     */
    public void render(SpriteBatch batch, String text, float x, float y, float yOffset, BubbleType type) {
        if (!visible) return; // Do not render if the bubble is not visible

        TextureRegion tailRegion = switch (type) {
            case NORMAL -> speechTailRegion;
            case SCREAM -> screamTailRegion;
            case THOUGHT -> thoughtTailRegion;
        };

        TextureRegion[] tailRegions = switch (type) {
            case NORMAL -> speechTailRegions;
            default -> speechTailRegions;
        };

        String[] parts = text.split("\\r?\\n"); // split new lines '\n' but UNIX and Windows both compatible

        // Calculate dimensions for the bubble
        float textWidth = 0; // find max
        for (String part : parts){
            float width = getTextWidth(part);  //xt.length() * mWidth;//font.getRegion().getRegionWidth();
            if (width > textWidth){
                textWidth = width;
            }
        }
        float textHeight = font.getLineHeight() * parts.length + (parts.length - 1) * 9;
        float paddingX = 8 * scale;
        float paddingY = 10 * scale;
        float bubbleWidth = (textWidth + paddingX * 2);
        float bubbleHeight = (textHeight + paddingY * 2);

        float tailX = x - tailRegion.getRegionWidth() / 2f * scale;
        x -= bubbleWidth / 3; // align bubble at the 1/3 position //at the center
        y += yOffset + 12 * scale + 6;

        // Render the 9-patch
        float cornerSize = 14 * scale;

        textWidth = Math.max(textWidth, cornerSize * 2);
        bubbleWidth = (textWidth + paddingX * 2);
        float centerWidth = bubbleWidth - 2 * cornerSize;
        float centerHeight = bubbleHeight - 2 * cornerSize;

        x = MathUtils.clamp(x, 0, getWorldWidth()-bubbleWidth);
        tailX = MathUtils.clamp(tailX, x+cornerSize, getWorldWidth()-tailRegion.getRegionWidth() * scale -cornerSize);

        // Top row
        batch.draw(speechCorners[0][0], x, y + centerHeight + cornerSize, cornerSize, cornerSize);
        batch.draw(speechCorners[0][1], x + cornerSize, y + centerHeight + cornerSize, centerWidth, cornerSize);
        batch.draw(speechCorners[0][2], x + cornerSize + centerWidth, y + centerHeight + cornerSize, cornerSize, cornerSize);

        // Middle row
        batch.draw(speechCorners[1][0], x, y + cornerSize, cornerSize, centerHeight);
        batch.draw(speechCorners[1][1], x + cornerSize, y + cornerSize, centerWidth, centerHeight);
        batch.draw(speechCorners[1][2], x + cornerSize + centerWidth, y + cornerSize, cornerSize, centerHeight);

        // Bottom row
        batch.draw(speechCorners[2][0], x, y, cornerSize, cornerSize);
        batch.draw(speechCorners[2][1], x + cornerSize, y, centerWidth, cornerSize);
        batch.draw(speechCorners[2][2], x + cornerSize + centerWidth, y, cornerSize, cornerSize);

        // Render the tail
        if (tailX < x + 15 * scale){
            tailRegion = tailRegions[1];
        }
        else if (tailX > x + bubbleWidth - tailRegion.getRegionWidth() * scale - 15 * scale){
            tailRegion = tailRegions[2];
        }
        batch.draw(tailRegion, tailX, y - 12 * scale, tailRegion.getRegionWidth() * scale, tailRegion.getRegionHeight() * scale);

        // Render the text
        font.getData().setScale(scale/2); // Scale the font to match the enlarged bubble
        font.draw(batch, text, x + (bubbleWidth-textWidth) / 2, y + bubbleHeight - paddingY);
        font.getData().setScale(1f); // Reset the font scale after rendering
    }

    /**
     * Updates the state of the speech bubble, controlling visibility based on elapsed time.
     *
     * @param delta The time in seconds since the last update.
     */
    public void update(float delta) {
        if (visible) {
            elapsedTime += delta;
            if (elapsedTime >= visibleDuration) {
                hide(); // Automatically hide the bubble after the duration
            }
        }
    }

    public enum BubbleType {
        NORMAL, SCREAM, THOUGHT
    }

    /**
     * Calculates the width of a single letter using the specified font.
     *
     * @param font   The font to use for calculating the width.
     * @param letter The letter whose width is to be calculated.
     * @return The width of the letter.
     */
    public float getLetterWidth(BitmapFont font, String letter){
        GlyphLayout layout = new GlyphLayout(font, letter);
        float adjust = switch (letter.charAt(0)){
            case 'l','i','j','I','!',',','.' -> -1;
            case 'f' -> -0.5f;
            case 'a','e','_' -> 0.5f;
            case 'm','w' -> 1;
            default -> 0;
        };
        return Math.max(layout.width + adjust * scale, layout.width); // width of letter "m"
    }

    /**
     * Calculates the total width of a text string based on the widths of individual characters.
     *
     * @param text The text string whose width is to be calculated.
     * @return The total width of the text string.
     */
    public float getTextWidth(String text){
        float length = 0; //scale * 3; // 5 is the minimum, so we add base of it
        for (char c : text.toCharArray()){
            try{
                length += letterWidth[c]; // if (c<='ü')
            }
            catch (ArrayIndexOutOfBoundsException e){
                //Gdx.app.log("SpeechBubble", "Unsupported letter: " + c);
                length += letterWidth['m'];
            }
        }
        return length;
    }

    /**
     * Makes the speech bubble visible for the specified duration.
     *
     * @param duration The duration (in seconds) for which the bubble will be visible.
     */
    public void show(float duration) {
        this.visible = true;
        this.visibleDuration = duration;
        this.elapsedTime = 0; // Reset elapsed time
    }

    /**
     * Hides the speech bubble, making it invisible.
     */
    public void hide() {
        this.visible = false;
    }

    /**
     * Checks whether the speech bubble is currently visible.
     *
     * @return {@code true} if the bubble is visible, {@code false} otherwise.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Gets the elapsed time since the bubble was shown.
     *
     * @return The elapsed time in seconds.
     */
    public float getElapsedTime() {
        return elapsedTime;
    }

}