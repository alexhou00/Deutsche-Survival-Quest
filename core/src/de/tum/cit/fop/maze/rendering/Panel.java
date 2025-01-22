package de.tum.cit.fop.maze.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Panel {
    private final Table table;

    public Panel(Stage stage, Drawable background) {

        table = new Table();
        table.setBackground(background);
        stage.addActor(table);
    }

    public void setSize(float widthRatio, float heightRatio) { // 0~1
        // Set in the middle, "ratio" is the ratio of the length to the entire window
        table.setSize(Gdx.graphics.getWidth() * widthRatio, Gdx.graphics.getHeight() * heightRatio);
        table.setPosition(Gdx.graphics.getWidth() * (1-widthRatio)/2, Gdx.graphics.getHeight() * (1-heightRatio)/2); // left-bottom corner
    }

    public void addLabel(String text, Label.LabelStyle style, float padBottom) {
        Label label = new Label(text, style);
        table.add(label).padBottom(padBottom).center().row();
    }

    public void addLabel(String text, Skin skin, float scale, float padBottom) {
        Label label = new Label(text, skin);
        label.getStyle().font.getData().setScale(scale);
        table.add(label).padBottom(padBottom).center().row();
    }

    public void addLabel(String text, Skin skin, String styleName, float scale, float padBottom) {
        Label label = new Label(text, skin, styleName);
        label.getStyle().font.getData().setScale(scale);
        table.add(label).padBottom(padBottom).center().row();
    }

    public void addButton(String buttonText, Skin skin, ChangeListener listener, float padBottom) {
        skin.get(Label.LabelStyle.class).font.getData().setScale(1); // set the scale back
        TextButton button = new TextButton(buttonText, skin);
        button.addListener(listener);
        table.add(button).padBottom(padBottom).center().row();
    }

    public void clear() {
        table.clear();
        table.remove();
    }

    public Table getTable() {
        return table;
    }
}