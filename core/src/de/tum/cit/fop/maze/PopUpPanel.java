package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class PopUpPanel extends ScreenAdapter {

    private Stage stage;
    //private Window popUpWindow;
    private Skin skin;

    @Override
    public void show() {
        stage = new Stage();

        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        GameIntroduction gameIntroduction = new GameIntroduction("Game Introduction", skin);

        gameIntroduction.show(stage);

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);//stage.getViewport(width, height)
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public static class GameIntroduction extends Dialog {
        public GameIntroduction(String title, Skin skin, String windowStyleName) {
            super(title, skin, windowStyleName);
        }

        public GameIntroduction(String title, Skin skin) {
            super(title, skin);
        }

        public GameIntroduction(String title, WindowStyle windowStyle) {
            super(title, windowStyle);
        }

        {
            text("Welcome TUM student!\n" +
                    "As you arrive in Germany for your studies, you will have to complete some challenges to settle in and start your studies. You will start at the airport, \n" +
                    "then figure out how to use the public transportation, which will be the Deutsche Bahn in this case, complete your city registration, chill in a Brauerei, and of course discover the beautiful Altstadt of Heilbronn:) \n" +
                    "\n" +
                    "During your journey, unfortunately, not everything will be as easy... First of all, you will have to collect your level keys to move on with your journey. Also, you always need to be alert, as there will be some traps, enemies, and surprises set for you to keep you from completing your journey.\n" +
                    "\n" +
                    "Good Luck!!\n" +
                    "\n" +
                    "[Press continue button to access level 1 instructions]\n");

            button("Continue");
        }
        @Override
        protected void result(Object object) {
            System.out.println(object);
        }
    }
}
