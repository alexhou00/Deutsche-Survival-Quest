package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;

    private float sinusInput = 0f;

    float spriteX = 0f;
    float spriteY = 0f;
    private boolean isMoving;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 0.75f;

        // Get the font from the game's skin
        font = game.getSkin().getFont("font");


        isMoving = false;

    }

    public boolean getIsMoving() {
        return isMoving;
    }

    private void handleInput() {
        float speed = 4f;
        // float delta = Gdx.graphics.getDeltaTime();
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);


        if (rightPressed || leftPressed || upPressed || downPressed) {
            isMoving = true;
            if (rightPressed) {
                spriteX += speed;
            }
            if (leftPressed) {
                spriteX -= speed;
            }
            if (upPressed) {
                spriteY += speed;
            }
            if (downPressed) {
                spriteY -= speed;
            }
        }
        else{
            isMoving = false;
        }


        /*if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }*/
    }

    // Screen interface methods with necessary functionality
    @Override
    public void render(float delta) {
        // Check for escape key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }



        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        camera.update(); // Update the camera

        // Move text in a circular path to have an example of a moving object
        sinusInput += delta;
        float textX = (float) (camera.position.x + Math.sin(sinusInput) * 100);
        float textY = (float) (camera.position.y + Math.cos(sinusInput) * 100);



        handleInput();

        // Set up and begin drawing with the sprite batch
        game.getSpriteBatch().setProjectionMatrix(camera.combined);

        game.getSpriteBatch().begin(); // Important to call this before drawing anything

        // Render the text
        font.draw(game.getSpriteBatch(), "Press ESC to go to menu", textX, textY);

        // Draw the character next to the text :) / We can reuse sinusInput here
        game.getSpriteBatch().draw(
                game.getCharacterDownAnimation().getKeyFrame(sinusInput, true),
                spriteX, //textX - 96,
                spriteY, // textY - 64,
                64,
                128
        );

        game.getSpriteBatch().end(); // Important to call this after drawing everything
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    // Additional methods and logic can be added as needed for the game screen
}
